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

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime}

case class SubmissionMetaData(submissionTime: String, conversationId: ConversationId, fileName: Option[String])

object SubmissionMetaData {
  val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def build(submissionTime: LocalDateTime, conversationID: ConversationId, fileName: String): SubmissionMetaData =
    SubmissionMetaData(
      dateTimeFormat.format(submissionTime.toInstant(OffsetDateTime.now().getOffset)),
      conversationID,
      Option(fileName)
    )
}
