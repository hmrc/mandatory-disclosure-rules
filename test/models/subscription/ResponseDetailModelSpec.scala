/*
 * Copyright 2025 HM Revenue & Customs
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

package models.subscription

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

class ResponseDetailModelSpec extends SpecBase {

  "ResponseDetail" - {
    "must serialise and de-serialise ResponseDetail" in {

      val responseDetailJson: String =
        """{
          |"subscriptionID": "111111111",
          |"tradingName": "",
          |"isGBUser": true,
          |"primaryContact": [
          |{
          |"email": "",
          |"phone": "",
          |"mobile": "",
          |"individual": {
          |"lastName": "Last",
          |"firstName": "First"
          |}
          |}
          |],
          |"secondaryContact": [
          |{
          |"email": "",
          |"organisation": {
          |"organisationName": ""
          |}
          |}
          |]
          |}""".stripMargin

      val expectedJson =
        """{"subscriptionID":"111111111","tradingName":"","isGBUser":true,"primaryContact":{"individual":
          |{"firstName":"First","lastName":"Last"},"email":"","phone":"","mobile":""},"secondaryContact"
          |:{"organisation":{"organisationName":""},"email":""}}""".stripMargin
      val json: JsValue =
        Json.parse(responseDetailJson)
      val responseDetail = json.as[ResponseDetail]
      Json.toJson(responseDetail) mustBe Json.parse(expectedJson)
    }

    "must throw exception for Empty primary Contact List when serialise and de-serialise ResponseDetail" in {

      val responseDetailJson: String =
        """{
          |"subscriptionID": "111111111",
          |"tradingName": "",
          |"isGBUser": true,
          |"primaryContact": [
          |],
          |"secondaryContact": [
          |{
          |"email": "",
          |"organisation": {
          |"organisationName": ""
          |}
          |}
          |]
          |}""".stripMargin

      val json: JsValue =
        Json.parse(responseDetailJson)
      assertThrows[IllegalArgumentException] {
        json.as[ResponseDetail]
      }

    }
  }
}
