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

package repositories.submission

import base.SpecBase
import config.AppConfig
import metrics.MetricsService
import models.submission._
import models.xml.{FileErrorCode, FileErrors, ValidationErrors}
import play.api.Configuration
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileDetailsRepositorySpec extends SpecBase with DefaultPlayMongoRepositorySupport[FileDetails] {

  lazy val config = app.injector.instanceOf[Configuration]
  lazy val metricsService = app.injector.instanceOf[MetricsService]

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  override lazy val repository = new FileDetailsRepository(mongoComponent, mockAppConfig, metricsService)

  val dateTimeNow: LocalDateTime = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
  val someSubmittedFile: FileDetails = {
    FileDetails(ConversationId("conversationId123456"),
      "subscriptionId",
      "messageRefId",
      Some(SingleNewInformation),
      Pending,
      "file1.xml",
      dateTimeNow,
      dateTimeNow
    )
  }

  "findStaleSubmissions" - {
    "retrieve a stale pending submission" in {
      val oldPendingFile = someSubmittedFile.copy(
        submitted = dateTimeNow.minusDays(1),
        name = "oldfile.xml",
        _id = ConversationId("conversationId777777"
        ))
      val oldRejectedFile = someSubmittedFile.copy(
        status = RejectedSDES,
        submitted = dateTimeNow.minusDays(1),
        name = "oldishfile.xml",
        _id = ConversationId("conversationId777778"
        ))

      val result: Future[Seq[FileDetails]] = for {
        _ <- repository.insert(someSubmittedFile)
        _ <- repository.insert(oldPendingFile)
        _ <- repository.insert(oldRejectedFile)
        res <- repository.findStaleSubmissions()
      } yield res

      whenReady(result) {
        _ mustBe List(oldPendingFile)
      }
    }
  }
  "Insert" - {
    "must insert FileDetails" in {
      val res = repository.insert(someSubmittedFile)
      whenReady(res) { result =>
        result mustBe true
      }
    }

    "must read FileDetails by SubscriptionId" in {
      val insert = repository.insert(someSubmittedFile)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findBySubscriptionId("subscriptionId")
      whenReady(res) { result =>
        result mustBe Seq(someSubmittedFile)
      }
    }

    "must read FileDetails by ConversationId" in {
      val insert = repository.insert(someSubmittedFile)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findByConversationId(ConversationId("conversationId123456"))
      whenReady(res) { result =>
        result mustBe Some(someSubmittedFile)
      }
    }

    "must read FileDetails by ConversationId doesn't exists" in {
      val insert = repository.insert(someSubmittedFile)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findByConversationId(ConversationId("conversationId12345678"))
      whenReady(res) { result =>
        result mustBe None
      }
    }

    "must update FileDetails status to Accepted by ConversationId" in {
      val insert = repository.insert(someSubmittedFile)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.updateStatus("conversationId123456", Accepted)
      whenReady(res) { result =>
        result must matchPattern {
          case Some(
          FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Some(SingleNewInformation), Accepted, "file1.xml", _, _)
          ) =>
        }
      }
    }

    "must update FileDetails status to Rejected by ConversationId" in {
      val insert = repository.insert(someSubmittedFile)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.updateStatus("conversationId123456",
        Rejected(ValidationErrors(Some(Seq(FileErrors(FileErrorCode.FailedSchemaValidation, Some("details")))), None))
      )

      whenReady(res) { result =>
        result must matchPattern {
          case Some(
          FileDetails(ConversationId("conversationId123456"),
          "subscriptionId",
          "messageRefId",
          Some(SingleNewInformation),
          Rejected(ValidationErrors(Some(Seq(FileErrors(FileErrorCode.FailedSchemaValidation, Some("details")))), None)),
          "file1.xml",
          _,
          _
          )
          ) =>
        }
      }
    }

    "must read FileStatus by ConversationId" in {
      val insert = repository.insert(someSubmittedFile)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findStatusByConversationId(ConversationId("conversationId123456"))
      whenReady(res) { result =>
        result mustBe Some(Pending)
      }
    }

  }


}
