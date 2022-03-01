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
import generators.Generators
import models.submission._
import models.xml.ValidationErrors
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
class FileDetailsRepositorySpec extends SpecBase with DefaultPlayMongoRepositorySupport[FileDetails] with Generators with ScalaCheckPropertyChecks {

  lazy val config              = app.injector.instanceOf[Configuration]
  override lazy val repository = new FileDetailsRepository(mongoComponent, config)

  val dateTimeNow: LocalDateTime = LocalDateTime.now()
  val fileDetails: FileDetails =
    FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Pending, "file1.xml", dateTimeNow, dateTimeNow)

  "Insert" - {
    "must insert Filedetails" in {
      val res = repository.insert(fileDetails)
      whenReady(res) { result =>
        result mustBe true
      }
    }

    "must read Filedetails by SubscriptionId" in {
      val insert = repository.insert(fileDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findBySubscriptionId("subscriptionId")
      whenReady(res) { result =>
        result mustBe Seq(fileDetails)
      }
    }

    "must read Filedetails by ConversationId" in {
      val insert = repository.insert(fileDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findByConversationId(ConversationId("conversationId123456"))
      whenReady(res) { result =>
        result mustBe Some(fileDetails)
      }
    }

    "must read Filedetails by ConversationId doesn't exists" in {
      val insert = repository.insert(fileDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.findByConversationId(ConversationId("conversationId12345678"))
      whenReady(res) { result =>
        result mustBe None
      }
    }

    "must update Filedetails status to Accepted by ConversationId" in {
      val insert = repository.insert(fileDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.updateStatus("conversationId123456", Accepted)
      whenReady(res) { result =>
        result mustBe true
      }
      val updatedResponse = repository.findByConversationId(ConversationId("conversationId123456"))

      whenReady(updatedResponse) { result =>
        result must matchPattern {
          case Some(FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Accepted, "file1.xml", _, _)) =>
        }
      }
    }

    "must update FileDetails status to Rejected by ConversationId" in {
      val validationErrors = arbitrary[ValidationErrors].sample.value
      val insert           = repository.insert(fileDetails)
      whenReady(insert) { result =>
        result mustBe true
      }
      val res = repository.updateStatus("conversationId123456", Rejected(validationErrors))
      whenReady(res) { result =>
        result mustBe true
      }
      val updatedResponse = repository.findByConversationId(ConversationId("conversationId123456"))
      whenReady(updatedResponse) { result =>
        result must matchPattern {
          case Some(
                FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Rejected(validationErrors), "file1.xml", _, _)
              ) =>
        }
      }
    }

    "must read FileStatus by ConversationId" in {
      val insert = repository.insert(fileDetails)
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
