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

package controllers

import base.SpecBase
import models.sdes.NotificationCallback
import models.sdes.NotificationType.{FileProcessed, FileProcessingFailure, FileReady, FileReceived}
import models.submission.{ConversationId, FileDetails, ReportType, SingleNewInformation, TransferFailure}
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, status, writeableOf_AnyContentAsJson, POST}
import repositories.submission.FileDetailsRepository

import java.time.LocalDateTime
import scala.concurrent.Future

class SDESCallbackControllerSpec extends SpecBase with BeforeAndAfterEach {
  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]

  override def beforeEach(): Unit = {
    reset(mockFileDetailsRepository)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[FileDetailsRepository].toInstance(mockFileDetailsRepository)
    )
    .build()

  "SDESCallbackController" - {
    "must return Ok for a failure and update the fileRespositoryDatabase appropriately" in {

      val sdesResponse = NotificationCallback(FileProcessingFailure, "test.xml", "ci1234", Some("Error"))
      val fileDetails =
        FileDetails(
          ConversationId("ci1234"),
          "subscriptionId",
          "messageRefId",
          Some(SingleNewInformation),
          TransferFailure,
          "file1.xml",
          LocalDateTime.now(),
          LocalDateTime.now()
        )
      when(mockFileDetailsRepository.updateStatus("ci1234", TransferFailure)).thenReturn(Future.successful(Some(fileDetails)))

      val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
        .withJsonBody(Json.toJson(sdesResponse))

      val result = route(application, request).value

      status(result) mustEqual OK
      verify(mockFileDetailsRepository, times(1)).updateStatus("ci1234", TransferFailure)
    }
    "must return Ok for FileReady status" in {
      val sdesResponse = NotificationCallback(FileReady, "test.xml", "ci1234", Some("Error"))

      val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
        .withJsonBody(Json.toJson(sdesResponse))

      val result = route(application, request).value

      status(result) mustEqual OK
    }
    "must return Ok for FileReceived status" in {
      val sdesResponse = NotificationCallback(FileReceived, "test.xml", "ci1234", Some("Error"))

      val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
        .withJsonBody(Json.toJson(sdesResponse))

      val result = route(application, request).value

      status(result) mustEqual OK
    }
    "must return Ok for FileProcessed status" in {
      val sdesResponse = NotificationCallback(FileProcessed, "test.xml", "ci1234", Some("Error"))

      val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
        .withJsonBody(Json.toJson(sdesResponse))

      val result = route(application, request).value

      status(result) mustEqual OK
    }
  }
}
