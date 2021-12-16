/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.submission

import models.submission.SubmissionMetaData
import models.subscription._
import javax.inject.Inject
import scala.xml._

class TransformService @Inject() () {

//TODO DAC6 need to update to MDR
  def addSubscriptionDetailsToSubmission(
    submissionFile: NodeSeq,
    subscriptionDetails: ResponseDetail,
    metaData: SubmissionMetaData
  ): NodeSeq =
    <DAC6UKSubmissionInboundRequest xmlns:dac6="urn:eu:taxud:dac6:v1"
                                    xmlns:eis="http://www.hmrc.gov.uk/dac6/eis"
                                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                    xsi:schemaLocation="http://www.hmrc.gov.uk/dac6/eis DCT06_EIS_UK_schema.xsd">
      <requestCommon>
        <receiptDate>{metaData.submissionTime}</receiptDate>
        <regime>DAC</regime>
        <conversationID>{metaData.conversationID.replace("govuk-tax-", "")}</conversationID>
        <schemaVersion>1.0.0</schemaVersion>
      </requestCommon>
      <requestDetail>
        {addNameSpaceDefinitions(submissionFile)}
      </requestDetail>
      <requestAdditionalDetail>
        {transformSubscriptionDetails(subscriptionDetails, metaData.fileName)}
      </requestAdditionalDetail>
    </DAC6UKSubmissionInboundRequest>

  def addNameSpaceDefinitions(submissionFile: NodeSeq): NodeSeq =
    for (node <- submissionFile) yield node match {
      case elem: Elem => //TODO Namespace binding
        elem.copy(scope = NamespaceBinding("xsi", "http://www.w3.org/2001/XMLSchema-instance", NamespaceBinding("dac6", "urn:ukdac6:v0.1", TopScope)))
    }

  def transformSubscriptionDetails(
    subscriptionDetails: ResponseDetail,
    fileName: Option[String]
  ): NodeSeq =
    Seq(
      fileName.map(name => <fileName>{name}</fileName>),
      Some(<subscriptionID>{subscriptionDetails.subscriptionID}</subscriptionID>),
      subscriptionDetails.tradingName.map(tradingName => <tradingName>{tradingName}</tradingName>),
      Some(<isGBUser>{subscriptionDetails.isGBUser}</isGBUser>),
      Some(<primaryContact>
          {transformContactInformation(subscriptionDetails.primaryContact.contactInformation.head)}
        </primaryContact>),
      subscriptionDetails.secondaryContact.map(sc => <secondaryContact>
          {transformContactInformation(sc.contactInformation.head)}
        </secondaryContact>)
    ).filter(_.isDefined).map(_.get)

  def transformContactInformation(
    contactInformation: ContactInformation
  ): NodeSeq = {
    val nodes = contactInformation match {
      case contactIndividual: ContactInformationForIndividual =>
        Seq(
          contactIndividual.phone.map(phone => <phoneNumber>{phone}</phoneNumber>),
          contactIndividual.mobile.map(mobile => <mobileNumber>{mobile}</mobileNumber>),
          Some(<emailAddress>{contactIndividual.email}</emailAddress>),
          Some(<individualDetails>
                {transformIndividual(contactIndividual.individual)}
              </individualDetails>)
        )
      case contactOrganisation: ContactInformationForOrganisation =>
        Seq(
          contactOrganisation.phone.map(phone => <phoneNumber>{phone}</phoneNumber>),
          contactOrganisation.mobile.map(mobile => <mobileNumber>{mobile}</mobileNumber>),
          Some(<emailAddress>{contactOrganisation.email}</emailAddress>),
          Some(<organisationDetails>
                <organisationName>{contactOrganisation.organisation.organisationName}</organisationName>
              </organisationDetails>)
        )
    }

    nodes.filter(_.isDefined).map(_.get)
  }

  def transformIndividual(individual: IndividualDetails): NodeSeq =
    Seq(
      Some(<firstName>{individual.firstName}</firstName>),
      individual.middleName.map(middle => <middleName>{middle}</middleName>),
      Some(<lastName>{individual.lastName}</lastName>)
    ).filter(_.isDefined).map(_.get)
}
