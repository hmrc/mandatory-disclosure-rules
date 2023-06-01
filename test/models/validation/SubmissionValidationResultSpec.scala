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

package models.validation

import base.SpecBase
import models.submission.{MDR401, MessageSpecData, MultipleNewInformation}
import play.api.libs.json.{JsSuccess, Json}

class SubmissionValidationResultSpec extends SpecBase {

  val lineNo                             = 1
  val mdrBodyCount                       = 2
  val messageSpecData: MessageSpecData   = MessageSpecData("XBC99999999999", MDR401, mdrBodyCount, "OECD1", MultipleNewInformation)
  val validationErrors: ValidationErrors = ValidationErrors(Seq(GenericError(lineNo, Message("details"))))
  val saxException                       = "SAXException message"

  "SubmissionValidationResult" - {

    "reads" - {

      "should parse JSON to corresponding SubmissionValidationResult objects" in {
        val successJson   = Json.obj("messageSpecData" -> messageSpecData)
        val successResult = Json.fromJson[SubmissionValidationResult](successJson)
        successResult mustBe JsSuccess(SubmissionValidationSuccess(messageSpecData))

        val failureJson   = Json.obj("validationErrors" -> validationErrors)
        val failureResult = Json.fromJson[SubmissionValidationResult](failureJson)
        failureResult mustBe JsSuccess(SubmissionValidationFailure(validationErrors))

        val invalidXmlJson   = Json.obj("saxException" -> saxException)
        val invalidXmlResult = Json.fromJson[SubmissionValidationResult](invalidXmlJson)
        invalidXmlResult mustBe JsSuccess(InvalidXmlError(saxException))
      }

      "should return JsError for invalid JSON" in {
        val invalidJson = Json.obj("invalidField" -> "value")
        val result      = Json.fromJson[SubmissionValidationResult](invalidJson)
        result.isError mustBe true
      }
    }

    "writes" - {

      "should convert SubmissionValidationResult objects to JSON" in {
        val successResult: SubmissionValidationResult = SubmissionValidationSuccess(messageSpecData)
        val successJson                               = Json.obj("messageSpecData" -> messageSpecData)
        Json.toJson(successResult) mustBe successJson

        val failureResult: SubmissionValidationResult = SubmissionValidationFailure(validationErrors)
        val failureJson                               = Json.obj("validationErrors" -> validationErrors)
        Json.toJson(failureResult) mustBe failureJson

        val invalidXmlResult: SubmissionValidationResult = InvalidXmlError(saxException)
        val invalidXmlJson                               = Json.obj("saxException" -> saxException)
        Json.toJson(invalidXmlResult) mustBe invalidXmlJson
      }
    }
  }

}
