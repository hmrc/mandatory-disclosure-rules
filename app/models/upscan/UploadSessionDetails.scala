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

package models.upscan

import org.bson.types.ObjectId
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats

case class UploadSessionDetails(
  _id: ObjectId,
  uploadId: UploadId,
  reference: Reference,
  status: UploadStatus
)

object UploadSessionDetails {
  val status = "status"

  implicit val objectIdFormats: Format[ObjectId] = MongoFormats.objectIdFormat

  val uploadedSuccessfullyFormat: OFormat[UploadedSuccessfully] =
    Json.format[UploadedSuccessfully]

  implicit val idFormat: OFormat[UploadId] = Json.format[UploadId]

  implicit val referenceFormat: OFormat[Reference] = Json.format[Reference]

  implicit val format: OFormat[UploadSessionDetails] =
    Json.format[UploadSessionDetails]

}
