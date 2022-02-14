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

package repositories.upscan

import base.SpecBase
import models.upscan.{Failed, Quarantined, Reference, UploadId, UploadSessionDetails}
import org.bson.types.ObjectId

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class UpScanSessionRepositorySpec extends SpecBase {
  lazy val uploadRep = app.injector.instanceOf[UpScanSessionRepository]
  val uploadId       = UploadId(UUID.randomUUID().toString)

  val uploadDetails = UploadSessionDetails(
    ObjectId.get(),
    uploadId,
    Reference("xxxx"),
    Quarantined
  )
  "Insert" - {
    "must insert UploadStatus" in {
      val uploadDetails = UploadSessionDetails(
        ObjectId.get(),
        uploadId,
        Reference("xxxx"),
        Quarantined
      )
      val res = uploadRep.insert(uploadDetails)
      whenReady(res) { result =>
        result mustBe true
      }
    }
    "must read UploadStatus" in {
      val res = uploadRep.findByUploadId(uploadId)
      whenReady(res) {
        case Some(result) =>
          result.uploadId mustBe (uploadDetails.uploadId)
          result.reference mustBe (uploadDetails.reference)
          result.status mustBe (uploadDetails.status)
        case _ => true
      }
    }
  }
  "Update" - {
    "must update a status" in {
      val result1: Boolean = Await.result(uploadRep.updateStatus(Reference("xxxx"), Failed), 5.seconds)
      result1 mustBe true
    }
  }
}
