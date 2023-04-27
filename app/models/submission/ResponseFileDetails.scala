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

package models.submission

import play.api.libs.json.{__, Json, OWrites, Reads}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime

case class ResponseFileDetails(conversationId: ConversationId,
                               subscriptionId: String,
                               messageRefId: String,
                               reportType: Option[ReportType],
                               status: FileStatus,
                               name: String,
                               submitted: LocalDateTime,
                               lastUpdated: LocalDateTime
)
object ResponseFileDetails {

  import play.api.libs.functional.syntax._

  implicit val reads: Reads[ResponseFileDetails] = (
    (__ \ "_id").read[ConversationId] and
      (__ \ "subscriptionId").read[String] and
      (__ \ "messageRefId").read[String] and
      (__ \ "reportType").readNullable[ReportType] and
      (__ \ "status").read[FileStatus] and
      (__ \ "name").read[String] and
      (__ \ "created").read[LocalDateTime](MongoJavatimeFormats.localDateTimeReads) and
      (__ \ "updated").read[LocalDateTime](MongoJavatimeFormats.localDateTimeReads)
  )(ResponseFileDetails.apply _)

  implicit val writes: OWrites[ResponseFileDetails] = Json.writes[ResponseFileDetails]

  def build(fileDetails: FileDetails): ResponseFileDetails = ResponseFileDetails(
    fileDetails._id,
    fileDetails.subscriptionId,
    fileDetails.messageRefId,
    fileDetails.reportType,
    fileDetails.status,
    fileDetails.name,
    fileDetails.submitted,
    fileDetails.lastUpdated
  )
}
