/*
 * Copyright 2023 HM Revenue & Customs
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

import models.submission.ConversationId
import models.subscription.{ContactInformation, IndividualDetails, OrganisationDetails, ResponseDetail}
import play.api.Logging

import java.time.LocalDateTime

class SDESMetaDataService extends Logging {
  def compileMetaData(subscriptionDetails: ResponseDetail,
                      correlationID: ConversationId,
                      submissionTime: LocalDateTime,
                      fileName: String
  ): Map[String, String] = {
    val primaryContact   = transformContactInformation(subscriptionDetails.primaryContact, "primaryContact")
    val secondaryContact = subscriptionDetails.secondaryContact.fold(Map.empty[String, String])(sc => transformContactInformation(sc, "secondaryContact"))
    val tradingName      = subscriptionDetails.tradingName.map(name => Map("requestAdditionalDetail/tradingName" -> name))

    tradingName.getOrElse(Map.empty) ++ Map(
      "requestCommon/conversationID"           -> correlationID.value,
      "requestCommon/receiptDate"              -> submissionTime.toString,
      "requestCommon/regime"                   -> "MDR",
      "requestCommon/schemaVersion"            -> "1.0.0",
      "requestAdditionalDetail/fileName"       -> fileName,
      "requestAdditionalDetail/subscriptionID" -> subscriptionDetails.subscriptionID,
      "requestAdditionalDetail/isGBUser"       -> subscriptionDetails.isGBUser.toString
    ) ++ primaryContact ++ secondaryContact
  }

  private def transformContactInformation(contactInformation: ContactInformation, contactType: String): Map[String, String] = {
    val contactName = contactInformation.contactType match {
      case individual: IndividualDetails => transformIndividual(individual, contactType)
      case organisation: OrganisationDetails =>
        Map(
          s"requestAdditionalDetail/$contactType/organisationDetails/organisationName" -> organisation.organisationName
        )
    }
    val phoneNumber  = contactInformation.phone.map(phone => Map(s"requestAdditionalDetail/$contactType/phoneNumber" -> phone))
    val mobileNumber = contactInformation.mobile.map(mobile => Map(s"requestAdditionalDetail/$contactType/mobileNumber" -> mobile))
    val email        = Map(s"requestAdditionalDetail/$contactType/emailAddress" -> contactInformation.email)

    contactName ++ email ++ phoneNumber.getOrElse(Map.empty) ++ mobileNumber.getOrElse(Map.empty)
  }

  private def transformIndividual(individual: IndividualDetails, contactType: String): Map[String, String] = {
    val firstName = Some(Map(s"requestAdditionalDetail/$contactType/individualDetails/firstName" -> individual.firstName))
    val middleName =
      individual.middleName.map(middleName => Map(s"requestAdditionalDetail/$contactType/individualDetails/middleName" -> middleName))
    val lastName = Some(Map(s"requestAdditionalDetail/$contactType/individualDetails/lastName" -> individual.lastName))

    Seq(firstName, middleName, lastName).flatten.foldLeft(Map.empty[String, String])(_ ++ _)
  }
}
