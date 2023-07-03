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

import models.subscription.{ContactInformation, IndividualDetails, OrganisationDetails, ResponseDetail}
import play.api.Logging

class SDESMetaDataService extends Logging {
  def compileMetaData(subscriptionDetails: ResponseDetail): Map[String, String] = {
    val primaryContact = transformContactInformation(subscriptionDetails.primaryContact, "primaryContact")
    val secondaryContact = subscriptionDetails.secondaryContact.fold(Map.empty[String, String])(sc => transformContactInformation(sc, "secondaryContact"))
    val tradingName = subscriptionDetails.tradingName.map(name => Map("/properties/requestAdditionalDetail/tradingName" -> name))

    tradingName.getOrElse(Map.empty) ++ Map(
      "/properties/requestAdditionalDetail/subscriptionID" -> subscriptionDetails.subscriptionID,
      "/properties/requestAdditionalDetail/isGBUser" -> subscriptionDetails.isGBUser.toString
    ) ++ primaryContact ++ secondaryContact
  }

  private def transformContactInformation(contactInformation: ContactInformation, contactType: String): Map[String, String] = {
    contactInformation.contactType match {
      case individual: IndividualDetails => transformIndividual(individual, contactType)
      case organisation: OrganisationDetails =>
        Map("/properties/requestAdditionalDetail/primaryContact/organisationDetails/organisationName" -> organisation.organisationName) //TODO: change this key once we know the actual value
    }
  }

  private def transformIndividual(individual: IndividualDetails, contactType: String): Map[String, String] = {
    val firstName = Some(Map(s"/properties/requestAdditionalDetail/$contactType/individualDetails/firstName" -> individual.firstName))
    val middleName = individual.middleName.map(middleName => Map(s"/properties/requestAdditionalDetail/$contactType/individualDetails/middleName" -> middleName))
    val lastName = Some(Map(s"/properties/requestAdditionalDetail/$contactType/individualDetails/lastName" -> individual.lastName))

    Seq(firstName, middleName, lastName).flatten.foldLeft(Map.empty[String, String])(_ ++ _)
  }
}
