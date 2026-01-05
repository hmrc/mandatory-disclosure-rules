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

package services

import base.SpecBase
import connectors.SDESConnector
import models.error.ReadSubscriptionError
import models.sdes.*
import models.submission.*
import models.subscription.{ContactInformation, OrganisationDetails, ResponseDetail}
import models.upscan.UploadId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.inject.bind
import repositories.submission.FileDetailsRepository
import services.submission.SDESService
import services.subscription.SubscriptionService
import tasks.StaleFileTask
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class SDESServiceSpec extends SpecBase with MockitoSugar with ScalaCheckDrivenPropertyChecks with BeforeAndAfterEach {

  val correlationIdRegex                               = """[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}"""
  val mockSubscriptionService: SubscriptionService     = mock[SubscriptionService]
  val mockSDESConnector: SDESConnector                 = mock[SDESConnector]
  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]
  val mockStaleFileTask                                = mock[StaleFileTask]

  override def beforeEach(): Unit =
    reset(
      mockSDESConnector,
      mockFileDetailsRepository,
      mockSubscriptionService
    )

  "SDESService" - {
    val application = applicationBuilder()
      .overrides(
        bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
        bind[SDESConnector].toInstance(mockSDESConnector),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[StaleFileTask].toInstance(mockStaleFileTask)
      )

    val sdesService = application.injector().instanceOf[SDESService]

    val messageSpec       = MessageSpecData("x9999", MDR401, 2, "OECD1", MultipleNewInformation)
    val checksum          = "1234"
    val fileSize          = 12345L
    val uploadId          = UploadId("uploadId")
    val submissionDetails = SubmissionDetails("test.xml", uploadId, "MDR1", fileSize, "http://localhost/", checksum, messageSpec)

    "filenotify" - {
      "must call sdes connector then create file details record and return a valid correlationID for a response of NO_CONTENT(204)" in {
        val responseDetail = ResponseDetail("subscriptionID",
                                            Some("tradingName"),
                                            isGBUser = true,
                                            ContactInformation(OrganisationDetails("orgName"), "email@test.com", None, None),
                                            None
        )
        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(responseDetail)))
        when(mockSDESConnector.fileReady(any[FileTransferNotification])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(NO_CONTENT)))

        when(mockFileDetailsRepository.insert(any[FileDetails])).thenReturn(Future.successful(true))

        val result = sdesService.fileNotify(submissionDetails)

        whenReady(result) { result =>
          result.isRight mustBe true
          result match {
            case Right(convID) => convID.value.matches(correlationIdRegex) mustBe true
            case _             => fail("Invalid Result")
          }

          verify(mockSDESConnector, times(1)).fileReady(any[FileTransferNotification])(any[HeaderCarrier], any[ExecutionContext])
          verify(mockFileDetailsRepository, times(1)).insert(any[FileDetails])
        }
      }

      "must return a Left with an Exception for response statues other than 204" in {
        val responseDetail = ResponseDetail("subscriptionID",
                                            Some("tradingName"),
                                            isGBUser = true,
                                            ContactInformation(OrganisationDetails("orgName"), "email@test.com", None, None),
                                            None
        )
        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(responseDetail)))
        when(mockSDESConnector.fileReady(any[FileTransferNotification])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Left(HttpResponse.apply(INTERNAL_SERVER_ERROR, ""))))

        val result = sdesService.fileNotify(submissionDetails)

        whenReady(result) { result =>
          result.isRight mustBe false

          verify(mockSDESConnector, times(1)).fileReady(any[FileTransferNotification])(any[HeaderCarrier], any[ExecutionContext])
          verify(mockFileDetailsRepository, times(0)).insert(any[FileDetails])
        }
      }

      "must return a Left with an Exception when getContactInformation errors" in {
        val responseDetail: ResponseDetail = ResponseDetail("subscriptionID",
                                                            Some("tradingName"),
                                                            isGBUser = true,
                                                            ContactInformation(OrganisationDetails("orgName"), "email@test.com", None, None),
                                                            None
        )
        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Left(ReadSubscriptionError(500))))

        val result = sdesService.fileNotify(submissionDetails)

        whenReady(result) { result =>
          result.isRight mustBe false
          result.left.get.getMessage mustEqual "Error retrieving subscription details"

          verifyNoInteractions(mockSDESConnector)
          verifyNoInteractions(mockFileDetailsRepository)
        }
      }
    }

  }
}
