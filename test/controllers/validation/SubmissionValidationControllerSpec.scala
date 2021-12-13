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
import models.validation._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, status, _}
import services.validation.SubmissionValidationEngine

import scala.concurrent.Future

class SubmissionValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockUploadSubmissionValidationEngine = mock[SubmissionValidationEngine]

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[SubmissionValidationEngine].toInstance(mockUploadSubmissionValidationEngine)
      )
      .build()

  "UploadSubmissionValidationController" - {

    "must return 200 and a sequence of errors when a validation error occurs" in {

      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]()))
        .thenReturn(Future.successful(Some(SubmissionValidationFailure(ValidationErrors(Seq(GenericError(1, "Error2")))))))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe OK

    }

    "must return 200 and Validation success object " in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]()))
        .thenReturn(Future.successful(Some(SubmissionValidationSuccess(true))))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe OK
    }

    "must return 400 and a bad request when validation fails" in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]()))
        .thenReturn(Future.successful(Some(SubmissionValidationInvalid())))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }

    "must return 400 and a bad request when None returns from validation engine" in {
      when(mockUploadSubmissionValidationEngine.validateUploadSubmission(any[Option[String]]()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission().url)
      val result  = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }
  }

}
