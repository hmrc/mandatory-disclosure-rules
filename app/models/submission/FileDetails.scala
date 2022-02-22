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
import julienrf.json.derived
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime

sealed trait FileStatus
case object Pending extends FileStatus
case object Accepted extends FileStatus
case class Rejected(error: FileError) extends FileStatus {
  override def toString: String = "Rejected"
}

object FileStatus {
  implicit val format: OFormat[FileStatus] = derived.oformat()
}

case class FileError(detail: String)

object FileError {
  implicit val format: OFormat[FileError] = Json.format[FileError]
}

case class FileDetails(_id: ConversationId,
                       subscriptionId: String,
                       messageRefId: String,
                       status: FileStatus,
                       fileName: String,
                       submitted: LocalDateTime,
                       lastUpdated: LocalDateTime
)
object FileDetails {
  implicit val mongoDateTime: Format[LocalDateTime] = MongoJavatimeFormats.localDateTimeFormat
  implicit val format: OFormat[FileDetails]         = Json.format[FileDetails]
}
