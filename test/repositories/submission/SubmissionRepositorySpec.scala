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

package repositories.submission

import base.SpecBase
import models.submission.{Accepted, ConversationId, FileError, Pending, Rejected, SubmissionDetails}
import play.api.Configuration
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
class SubmissionRepositorySpec extends SpecBase with DefaultPlayMongoRepositorySupport[SubmissionDetails] {

  lazy val config              = app.injector.instanceOf[Configuration]
  override lazy val repository = new SubmissionRepository(mongoComponent, config)

  val dateTimeNow: LocalDateTime = LocalDateTime.now()
  val submissionDetails: SubmissionDetails =
    SubmissionDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Pending, "file1.xml", dateTimeNow, dateTimeNow)

  "Insert" - {
    "must insert SubmissionDetails" in {
      val res = repository.insert(submissionDetails)
      whenReady(res) { result =>
        result mustBe true
      }
    }

    "must read SubmissionDetails by SubscriptionId" in {
      val insert = repository.insert(submissionDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findBySubscriptionId("subscriptionId")
      whenReady(res) { result =>
        result mustBe Seq(submissionDetails)
      }
    }

    "must read SubmissionDetails by ConversationId" in {
      val insert = repository.insert(submissionDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findByConversationId("conversationId123456")
      whenReady(res) { result =>
        result mustBe Seq(submissionDetails)
      }
    }

    "must update SubmissionDetails status to Accepted by ConversationId" in {
      val insert = repository.insert(submissionDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.updateStatus("conversationId123456", Accepted)
      whenReady(res) { result =>
        result mustBe true
      }
      val updatedResponse = repository.findByConversationId("conversationId123456")

      whenReady(updatedResponse) { result =>
        result must matchPattern {
          case Seq(SubmissionDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Accepted, "file1.xml", _, _)) =>
        }
      }
    }

    "must update SubmissionDetails status to Rejected by ConversationId" in {
      val insert = repository.insert(submissionDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.updateStatus("conversationId123456", Rejected(FileError("error in file")))
      whenReady(res) { result =>
        result mustBe true
      }
      val updatedResponse = repository.findByConversationId("conversationId123456")
      whenReady(updatedResponse) { result =>
        result must matchPattern {
          case Seq(
                SubmissionDetails(ConversationId("conversationId123456"),
                                  "subscriptionId",
                                  "messageRefId",
                                  Rejected(FileError("error in file")),
                                  "file1.xml",
                                  _,
                                  _
                )
              ) =>
        }
      }
    }

  }

}
