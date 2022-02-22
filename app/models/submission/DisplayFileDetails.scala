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

import play.api.libs.json.{__, Json, OWrites, Reads}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime

case class DisplayFileDetails(conversationId: ConversationId,
                              subscriptionId: String,
                              messageRefId: String,
                              status: FileStatus,
                              fileName: String,
                              submitted: LocalDateTime,
                              lastUpdated: LocalDateTime
)
object DisplayFileDetails {

  import play.api.libs.functional.syntax._

  implicit val reads: Reads[DisplayFileDetails] = (
    (__ \ "_id").read[ConversationId] and
      (__ \ "subscriptionId").read[String] and
      (__ \ "messageRefId").read[String] and
      (__ \ "status").read[FileStatus] and
      (__ \ "fileName").read[String] and
      (__ \ "created").read[LocalDateTime](MongoJavatimeFormats.localDateTimeReads) and
      (__ \ "updated").read[LocalDateTime](MongoJavatimeFormats.localDateTimeReads)
  )(DisplayFileDetails.apply _)

  implicit val writes: OWrites[DisplayFileDetails] = Json.writes[DisplayFileDetails]

  def build(fileDetails: FileDetails): DisplayFileDetails = DisplayFileDetails(
    fileDetails._id,
    fileDetails.subscriptionId,
    fileDetails.messageRefId,
    fileDetails.status,
    fileDetails.fileName,
    fileDetails.submitted,
    fileDetails.lastUpdated
  )
}
