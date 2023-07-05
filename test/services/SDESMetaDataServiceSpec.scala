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

package services

import base.SpecBase
import models.submission.ConversationId
import models.subscription.{ContactInformation, IndividualDetails, OrganisationDetails, ResponseDetail}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import services.submission.SDESMetaDataService

import java.time.LocalDateTime

class SDESMetaDataServiceSpec extends SpecBase with MockitoSugar with ScalaCheckDrivenPropertyChecks with BeforeAndAfterEach {

  val sdesMetaDataService = app.injector.instanceOf[SDESMetaDataService]
  val conversationID      = ConversationId("conversationID")
  val dateTime            = LocalDateTime.of(2022, 1, 1, 2, 1, 0, 0)
  val fileName            = "fileName"

  "SDESMetaDataServiceSpec" - {
    "compileMetaData" - {
      "Must provide metaData for an individual with 1 set of contact details" in {
        val responseDetail = ResponseDetail(
          "subscriptionID",
          Some("tradingName"),
          isGBUser = true,
          ContactInformation(IndividualDetails("firstName", Some("middleName"), "lastName"), "email@test.com", Some("1234567"), Some("12345678")),
          None
        )

        val result = sdesMetaDataService.compileMetaData(responseDetail, conversationID, dateTime, fileName)

        result mustBe Map(
          "/properties/requestCommon/conversationID"                                        -> "conversationID",
          "/properties/requestCommon/receiptDate"                                           -> "2022-01-01T02:01",
          "/properties/requestCommon/schemaVersion"                                         -> "1.0.0",
          "/properties/requestCommon/regime"                                                -> "MDR",
          "/properties/requestAdditionalDetail/primaryContact/phoneNumber"                  -> "1234567",
          "/properties/requestAdditionalDetail/primaryContact/emailAddress"                 -> "email@test.com",
          "/properties/requestAdditionalDetail/primaryContact/mobileNumber"                 -> "12345678",
          "/properties/requestAdditionalDetail/primaryContact/individualDetails/firstName"  -> "firstName",
          "/properties/requestAdditionalDetail/primaryContact/individualDetails/middleName" -> "middleName",
          "/properties/requestAdditionalDetail/primaryContact/individualDetails/lastName"   -> "lastName",
          "/properties/requestAdditionalDetail/fileName"                                    -> "fileName",
          "/properties/requestAdditionalDetail/tradingName"                                 -> "tradingName",
          "/properties/requestAdditionalDetail/subscriptionID"                              -> "subscriptionID",
          "/properties/requestAdditionalDetail/isGBUser"                                    -> "true"
        )
      }

      "Must provide metaData for an individual with 2 sets of contact details" in {
        val responseDetail = ResponseDetail(
          "subscriptionID",
          Some("tradingName"),
          isGBUser = true,
          ContactInformation(IndividualDetails("firstName", Some("middleName"), "lastName"), "email@test.com", Some("1234567"), Some("12345678")),
          Some(ContactInformation(IndividualDetails("firstName2", Some("middleName2"), "lastName2"), "email2@test.com", Some("21234567"), Some("212345678")))
        )

        val result = sdesMetaDataService.compileMetaData(responseDetail, conversationID, dateTime, fileName)

        result mustBe Map(
          "/properties/requestCommon/conversationID"                                          -> "conversationID",
          "/properties/requestCommon/receiptDate"                                             -> "2022-01-01T02:01",
          "/properties/requestCommon/schemaVersion"                                           -> "1.0.0",
          "/properties/requestCommon/regime"                                                  -> "MDR",
          "/properties/requestAdditionalDetail/primaryContact/phoneNumber"                    -> "1234567",
          "/properties/requestAdditionalDetail/primaryContact/emailAddress"                   -> "email@test.com",
          "/properties/requestAdditionalDetail/primaryContact/mobileNumber"                   -> "12345678",
          "/properties/requestAdditionalDetail/primaryContact/individualDetails/firstName"    -> "firstName",
          "/properties/requestAdditionalDetail/primaryContact/individualDetails/middleName"   -> "middleName",
          "/properties/requestAdditionalDetail/primaryContact/individualDetails/lastName"     -> "lastName",
          "/properties/requestAdditionalDetail/secondaryContact/phoneNumber"                  -> "21234567",
          "/properties/requestAdditionalDetail/secondaryContact/emailAddress"                 -> "email2@test.com",
          "/properties/requestAdditionalDetail/secondaryContact/mobileNumber"                 -> "212345678",
          "/properties/requestAdditionalDetail/secondaryContact/individualDetails/firstName"  -> "firstName2",
          "/properties/requestAdditionalDetail/secondaryContact/individualDetails/middleName" -> "middleName2",
          "/properties/requestAdditionalDetail/secondaryContact/individualDetails/lastName"   -> "lastName2",
          "/properties/requestAdditionalDetail/fileName"                                      -> "fileName",
          "/properties/requestAdditionalDetail/tradingName"                                   -> "tradingName",
          "/properties/requestAdditionalDetail/subscriptionID"                                -> "subscriptionID",
          "/properties/requestAdditionalDetail/isGBUser"                                      -> "true"
        )
      }
      "Must provide metaData for an organisation with 1 set of contact details" in {
        val responseDetail = ResponseDetail(
          "subscriptionID",
          Some("tradingName"),
          isGBUser = true,
          ContactInformation(OrganisationDetails("organisationName"), "email@test.com", Some("1234567"), Some("12345678")),
          None
        )

        val result = sdesMetaDataService.compileMetaData(responseDetail, conversationID, dateTime, fileName)

        result mustBe Map(
          "/properties/requestCommon/conversationID"                                                -> "conversationID",
          "/properties/requestCommon/receiptDate"                                                   -> "2022-01-01T02:01",
          "/properties/requestCommon/schemaVersion"                                                 -> "1.0.0",
          "/properties/requestCommon/regime"                                                        -> "MDR",
          "/properties/requestAdditionalDetail/primaryContact/phoneNumber"                          -> "1234567",
          "/properties/requestAdditionalDetail/primaryContact/emailAddress"                         -> "email@test.com",
          "/properties/requestAdditionalDetail/primaryContact/mobileNumber"                         -> "12345678",
          "/properties/requestAdditionalDetail/primaryContact/organisationDetails/organisationName" -> "organisationName",
          "/properties/requestAdditionalDetail/fileName"                                            -> "fileName",
          "/properties/requestAdditionalDetail/tradingName"                                         -> "tradingName",
          "/properties/requestAdditionalDetail/subscriptionID"                                      -> "subscriptionID",
          "/properties/requestAdditionalDetail/isGBUser"                                            -> "true"
        )
      }

      "Must provide metaData for an individual with 2 sets of contact details" in {
        val responseDetail = ResponseDetail(
          "subscriptionID",
          Some("tradingName"),
          isGBUser = true,
          ContactInformation(OrganisationDetails("firstName"), "email@test.com", Some("1234567"), Some("12345678")),
          Some(ContactInformation(OrganisationDetails("firstName2"), "email2@test.com", Some("21234567"), Some("212345678")))
        )

        val result = sdesMetaDataService.compileMetaData(responseDetail, conversationID, dateTime, fileName)

        result mustBe Map(
          "/properties/requestCommon/conversationID" -> "conversationID",
          "/properties/requestCommon/receiptDate" -> "2022-01-01T02:01",
          "/properties/requestCommon/schemaVersion" -> "1.0.0",
          "/properties/requestCommon/regime" -> "MDR",
          "/properties/requestAdditionalDetail/primaryContact/phoneNumber" -> "1234567",
          "/properties/requestAdditionalDetail/primaryContact/emailAddress" -> "email@test.com",
          "/properties/requestAdditionalDetail/primaryContact/mobileNumber" -> "12345678",
          "/properties/requestAdditionalDetail/primaryContact/organisationDetails/organisationName" -> "firstName2",
          "/properties/requestAdditionalDetail/secondaryContact/phoneNumber" -> "21234567",
          "/properties/requestAdditionalDetail/secondaryContact/emailAddress" -> "email2@test.com",
          "/properties/requestAdditionalDetail/secondaryContact/mobileNumber" -> "212345678",
          "/properties/requestAdditionalDetail/secondaryContact/individualDetails/firstName" -> "firstName2",
          "/properties/requestAdditionalDetail/secondaryContact/individualDetails/middleName" -> "middleName2",
          "/properties/requestAdditionalDetail/secondaryContact/individualDetails/lastName" -> "lastName2",
          "/properties/requestAdditionalDetail/fileName" -> "fileName",
          "/properties/requestAdditionalDetail/tradingName" -> "tradingName",
          "/properties/requestAdditionalDetail/subscriptionID" -> "subscriptionID",
          "/properties/requestAdditionalDetail/isGBUser" -> "true"
        )
      }
    }

  }
}
