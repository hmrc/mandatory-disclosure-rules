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
import models.sdes.NotificationType.{FileProcessed, FileProcessingFailure, FileReady, FileReceived}
import models.sdes.{NotificationCallback, SHA256}
import models.submission._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, status, writeableOf_AnyContentAsJson, POST}
import repositories.submission.FileDetailsRepository
import services.EmailService
import utils.DateTimeFormatUtil

import java.time.LocalDateTime
import scala.concurrent.Future

class SDESCallbackControllerSpec extends SpecBase with BeforeAndAfterEach with TableDrivenPropertyChecks {
  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]
  val mockEmailService: EmailService                   = mock[EmailService]

  override def beforeEach(): Unit = {
    reset(mockFileDetailsRepository)
    reset(mockEmailService)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
      bind[EmailService].toInstance(mockEmailService)
    )
    .build()

  "SDESCallbackController" - {

    val conversationId = "ci1234"
    val fileDetailsPending =
      FileDetails(
        ConversationId(conversationId),
        "subscriptionId",
        "messageRefId",
        Some(SingleNewInformation),
        Pending,
        "file1.xml",
        LocalDateTime.now(),
        LocalDateTime.now()
      )

    val sdesResponse =
      NotificationCallback(FileProcessingFailure, "test.xml", SHA256, "checksum", conversationId, Some(LocalDateTime.now), Some("Something went wrong"))

    "FileProcessingFailure callback" - {

      "if everything works as intended" - {

        "must return Ok, set the file status to RejectedSDESVirus, and send an error email if the failure reason contains 'virus'" in {
          val sdesVirusCallback = sdesResponse.copy(failureReason = Some("There was a virus in the file"))

          when(mockFileDetailsRepository.findByConversationId(ConversationId(conversationId)))
            .thenReturn(Future.successful(Some(fileDetailsPending)))

          when(mockFileDetailsRepository.updateStatus(conversationId, RejectedSDESVirus))
            .thenReturn(Future.successful(Some(fileDetailsPending.copy(status = RejectedSDESVirus))))

          val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
            .withJsonBody(Json.toJson(sdesVirusCallback))

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFileDetailsRepository, times(1)).updateStatus(conversationId, RejectedSDESVirus)
          verify(mockEmailService, times(1)).sendAndLogEmail(
            ArgumentMatchers.eq(fileDetailsPending.subscriptionId),
            ArgumentMatchers.eq(DateTimeFormatUtil.displayFormattedDate(fileDetailsPending.submitted)),
            ArgumentMatchers.eq(fileDetailsPending.messageRefId),
            ArgumentMatchers.eq(false),
            ArgumentMatchers.eq(ReportType.getMessage(fileDetailsPending.reportType))
          )(any())
        }

        "must return Ok, set the file status to RejectedSDES, and send an error email for any other failure reason" in {
          when(mockFileDetailsRepository.findByConversationId(ConversationId(conversationId)))
            .thenReturn(Future.successful(Some(fileDetailsPending)))

          when(mockFileDetailsRepository.updateStatus(conversationId, RejectedSDES))
            .thenReturn(Future.successful(Some(fileDetailsPending.copy(status = RejectedSDES))))

          val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
            .withJsonBody(Json.toJson(sdesResponse))

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFileDetailsRepository, times(1)).updateStatus(conversationId, RejectedSDES)
          verify(mockEmailService, times(1)).sendAndLogEmail(
            ArgumentMatchers.eq(fileDetailsPending.subscriptionId),
            ArgumentMatchers.eq(DateTimeFormatUtil.displayFormattedDate(fileDetailsPending.submitted)),
            ArgumentMatchers.eq(fileDetailsPending.messageRefId),
            ArgumentMatchers.eq(false),
            ArgumentMatchers.eq(ReportType.getMessage(fileDetailsPending.reportType))
          )(any())
        }

      }

      "if we are unable to update the file status in the db" - {

        "must return InternalServerError and take no further action" in {
          when(mockFileDetailsRepository.findByConversationId(ConversationId(conversationId)))
            .thenReturn(Future.successful(Some(fileDetailsPending)))

          when(mockFileDetailsRepository.updateStatus(conversationId, RejectedSDES))
            .thenReturn(Future.successful(None))

          val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
            .withJsonBody(Json.toJson(sdesResponse))

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          verifyZeroInteractions(mockEmailService)
        }

      }

      "if the file is not already in a Pending state" - {

        "must return Ok and take no further action" in {
          val fileDetailsAccepted = fileDetailsPending.copy(status = Accepted)

          when(mockFileDetailsRepository.findByConversationId(ConversationId(conversationId)))
            .thenReturn(Future.successful(Some(fileDetailsAccepted)))

          val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
            .withJsonBody(Json.toJson(sdesResponse))

          val result = route(application, request).value

          status(result) mustEqual OK
          verifyZeroInteractions(mockEmailService)
        }

      }

      "if we can not find any file information in the db" - {

        "we must return Ok and take no further action" in {
          when(mockFileDetailsRepository.findByConversationId(ConversationId(conversationId)))
            .thenReturn(Future.successful(None))

          val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
            .withJsonBody(Json.toJson(sdesResponse))

          val result = route(application, request).value

          status(result) mustEqual OK
          verifyZeroInteractions(mockEmailService)
        }

      }

    }

    "All other valid callbacks which do not affect the file status" - {
      val notificationTypes = Table(
        ("Name", "Object"),
        ("FileReady", FileReady),
        ("FileReceived", FileReceived),
        ("FileProcessed", FileProcessed)
      )

      forAll(notificationTypes) { (name, notificationType) =>
        s"must return Ok for a $name callback" in {
          val sdesResponse = NotificationCallback(notificationType, "test.xml", SHA256, "checksum", "ci1234", Some(LocalDateTime.now), Some("Error"))

          val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
            .withJsonBody(Json.toJson(sdesResponse))

          val result = route(application, request).value

          status(result) mustEqual OK
          verifyZeroInteractions(mockEmailService)
        }
      }
    }

    "Invalid callback (invalid json)" - {

      "must return InternalServer error" in {
        val request = FakeRequest(POST, routes.SDESCallbackController.callback.url)
          .withJsonBody(Json.parse("""{"name":"value"}"""))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        verifyZeroInteractions(mockEmailService)
      }

    }
  }
}
