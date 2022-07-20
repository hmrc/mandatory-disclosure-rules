/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.upscan

import base.SpecBase
import models.upscan._
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.upscan.UpScanSessionRepository
import services.upscan.UploadProgressTracker

import java.util.UUID
import scala.concurrent.Future

class UploadFormControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockUploadSessionRepository: UpScanSessionRepository =
    mock[UpScanSessionRepository]

  val mockUploadProgressTracker: UploadProgressTracker =
    mock[UploadProgressTracker]

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[UploadProgressTracker].toInstance(mockUploadProgressTracker),
        bind[UpScanSessionRepository].toInstance(mockUploadSessionRepository)
      )
      .build()

  "requestUpload" - {
    "must return Ok when valid details sent" in {

      val uploadId    = UploadId(UUID.randomUUID().toString)
      val identifiers = UpscanIdentifiers(uploadId, Reference("xxxx"))

      val request =
        FakeRequest(POST, routes.UploadFormController.requestUpload.url).withJsonBody(Json.toJson(identifiers))

      when(mockUploadProgressTracker.requestUpload(identifiers.uploadId, identifiers.fileReference)).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual OK
    }
    "must return Bad_Request when invalid details sent" in {

      val json = Json.parse("""{"aString": "a"}""")
      val request =
        FakeRequest(POST, routes.UploadFormController.requestUpload.url).withJsonBody(json)

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
    }
  }

  "getDetails" - {
    "must return ok with status" in {

      val uploadDetails = UploadSessionDetails(
        ObjectId.get(),
        UploadId("123"),
        Reference("123"),
        InProgress
      )

      when(mockUploadSessionRepository.findByUploadId(UploadId("uploadID")))
        .thenReturn(Future.successful(Some(uploadDetails)))

      val request =
        FakeRequest(GET, routes.UploadFormController.getDetails("uploadID").url)

      val result = route(application, request).value

      status(result) mustEqual OK
    }

    "must return 404 when none is returned" in {

      when(mockUploadSessionRepository.findByUploadId(UploadId("uploadID")))
        .thenReturn(Future.successful(None))

      val request =
        FakeRequest(GET, routes.UploadFormController.getDetails("uploadID").url)

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND
    }
  }

  "getStatus" - {
    "must return ok with status" in {

      when(mockUploadProgressTracker.getUploadResult(UploadId("uploadID")))
        .thenReturn(Future.successful(Some(InProgress)))

      val request =
        FakeRequest(GET, routes.UploadFormController.getStatus("uploadID").url)

      val result = route(application, request).value

      status(result) mustEqual OK
    }

    "must return 404 when none is returned" in {

      when(mockUploadProgressTracker.getUploadResult(UploadId("uploadID")))
        .thenReturn(Future.successful(None))

      val request =
        FakeRequest(GET, routes.UploadFormController.getStatus("uploadID").url)

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND
    }
  }
}
