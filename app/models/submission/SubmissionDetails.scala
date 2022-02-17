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

package models.submission
import play.api.libs.json.{Format, JsPath, JsString, JsSuccess, Json, OFormat, Reads, Writes}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime
import java.util.UUID

sealed trait FileStatus
case object Pending extends FileStatus
case object Accepted extends FileStatus
case class Rejected(error: FileError) extends FileStatus {
  override def toString: String            = "Rejected"
  implicit def rejected: OFormat[Rejected] = Json.format[Rejected]
}

object FileStatus {
  implicit def rejected: OFormat[Rejected] = Json.format[Rejected]

  implicit val writes: Writes[FileStatus] = Writes[FileStatus] {
    case Pending            => JsString("Pending")
    case Accepted           => JsString("Accepted")
    case rejected: Rejected => Json.toJson(rejected)
  }

  implicit val reads: Reads[FileStatus] = Reads[FileStatus] {
    case JsString("Pending")  => JsSuccess(Pending)
    case JsString("Accepted") => JsSuccess(Accepted)
    case rejected             => JsSuccess(rejected.as[Rejected])
  }
}

case class FileError(detail: String)

object FileError {
  implicit val format: OFormat[FileError] = Json.format[FileError]
}

case class ConversationId(value: String)
object ConversationId {
  def apply(): ConversationId                 = ConversationId(UUID.randomUUID().toString)
  implicit val writes: Writes[ConversationId] = conversationId => JsString(conversationId.value)
  implicit val reads: Reads[ConversationId]   = (JsPath \ "_id").read[String].map(id => ConversationId(id))
}

case class SubmissionDetails(_id: ConversationId,
                             subscriptionId: String,
                             messageRefId: String,
                             status: FileStatus,
                             fileName: String,
                             submitted: LocalDateTime,
                             lastUpdated: LocalDateTime
)
object SubmissionDetails {
  implicit val mongoDateTime: Format[LocalDateTime] = MongoJavatimeFormats.localDateTimeFormat
  implicit val format: OFormat[SubmissionDetails]   = Json.format[SubmissionDetails]
}
