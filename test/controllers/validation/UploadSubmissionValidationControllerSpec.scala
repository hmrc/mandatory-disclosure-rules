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

package controllers.validation

import base.SpecBase
import controllers.upscan.routes
import models.validation.{
  UploadSubmissionValidationFailure,
  UploadSubmissionValidationInvalid,
  UploadSubmissionValidationResult,
  UploadSubmissionValidationSuccess,
  ValidationErrors
}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.POST
import repositories.upscan.UploadSessionRepository
import services.upscan.UploadProgressTracker
import services.validation.UploadSubmissionValidationEngine
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.{status, _}
import schemas.{DAC6XMLSchema, XMLSchema}

import scala.concurrent.{ExecutionContext, Future}

class UploadSubmissionValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockUploadSubmissionValidationEngine = mock[UploadSubmissionValidationEngine]
  val mockXMLSChema                        = mock[DAC6XMLSchema]

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[UploadSubmissionValidationEngine].toInstance(mockUploadSubmissionValidationEngine),
        bind[XMLSchema].toInstance(mockXMLSChema)
      )
      .build()

  "UploadSubmissionValidationController" - {

    "must return 200 and a sequence of errors when a validation error occurs" in {

      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(UploadSubmissionValidationFailure(ValidationErrors(Seq("Error1", "Error2"))))))

      val request = FakeRequest(POST, routes.UploadSubmissionValidationController.validateUploadSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe OK

    }

    "must return 200 and Validation success object " in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(UploadSubmissionValidationSuccess(true))))

      val request = FakeRequest(POST, routes.UploadSubmissionValidationController.validateUploadSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe OK
    }

    "must return 400 and a bad request when validation fails" in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(UploadSubmissionValidationInvalid())))

      val request = FakeRequest(POST, routes.UploadSubmissionValidationController.validateUploadSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }

    "must return 400 and a bad request when None returns from validation engine" in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.UploadSubmissionValidationController.validateUploadSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }
  }

}
