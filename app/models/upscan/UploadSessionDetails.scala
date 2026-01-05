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

package models.upscan

import org.bson.types.ObjectId
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*
import uk.gov.hmrc.mongo.play.json.formats.{MongoFormats, MongoJavatimeFormats}

import java.time.Instant

case class UploadSessionDetails(
  _id: ObjectId,
  uploadId: UploadId,
  reference: Reference,
  status: UploadStatus,
  lastUpdated: Instant = Instant.now
)

object UploadSessionDetails {
  val status = "status"

  implicit val mongoDateTimeWrites: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val objectIdFormats: Format[ObjectId] = MongoFormats.objectIdFormat

  val uploadedSuccessfullyFormat: OFormat[UploadedSuccessfully] =
    Json.format[UploadedSuccessfully]

  implicit val reads: Reads[UploadSessionDetails] = (
    (__ \ "_id").read[ObjectId] and
      (__ \ "uploadId").read[UploadId] and
      (__ \ "reference" \ "value").read[Reference] and
      (__ \ "status").read[UploadStatus] and
      (__ \ "lastUpdated").read[Instant](MongoJavatimeFormats.instantReads)
  )(UploadSessionDetails.apply _)

  implicit val writes: OWrites[UploadSessionDetails] = (
    (__ \ "_id").write[ObjectId] and
      (__ \ "uploadId" \ "value").write[UploadId](UploadId.writesUploadId) and
      (__ \ "reference" \ "value").write[Reference](Reference.referenceWrites) and
      (__ \ "status").write[UploadStatus](UploadStatus.write) and
      (__ \ "lastUpdated").write[Instant](MongoJavatimeFormats.instantWrites)
  )(u => (u._id, u.uploadId, u.reference, u.status, u.lastUpdated))

  implicit val format: OFormat[UploadSessionDetails] = OFormat(reads, writes)
}
