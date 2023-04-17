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

package controllers.validation

import base.SpecBase
import controllers.auth.{FakeIdentifierAuthAction, IdentifierAuthAction}
import models.submission.{MDR401, MessageSpecData, MultipleNewInformation}
import models.upscan.UpscanURL
import models.validation._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, POST, _}
import services.validation.UploadedXmlValidationEngine

import scala.concurrent.Future

class SubmissionValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockUploadSubmissionValidationEngine: UploadedXmlValidationEngine = mock[UploadedXmlValidationEngine]
  val messageSpecData: MessageSpecData                                  = MessageSpecData("XBC99999999999", MDR401, 2, MultipleNewInformation)

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[UploadedXmlValidationEngine].toInstance(mockUploadSubmissionValidationEngine),
        bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
      )
      .build()

  "UploadSubmissionValidationController" - {

    "must return 200 and a sequence of errors when a validation error occurs" in {

      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[String]()))
        .thenReturn(Future.successful(SubmissionValidationFailure(ValidationErrors(Seq(GenericError(1, Message("xml.enter.an.element")))))))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url).withJsonBody(Json.toJson(UpscanURL("someUrl")))
      val result  = route(application, request).value

      status(result) mustBe OK

    }

    "must return 200 and Validation success object " in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[String]()))
        .thenReturn(Future.successful(SubmissionValidationSuccess(messageSpecData)))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url).withJsonBody(Json.toJson(UpscanURL("someUrl")))
      val result  = route(application, request).value

      status(result) mustBe OK
    }

    "must return 400 and a bad request when validation fails" in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[String]()))
        .thenReturn(Future.successful(InvalidXmlError("")))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url).withJsonBody(Json.toJson(UpscanURL("someUrl")))
      val result  = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }

  }

}
