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

package models.audit

import models.submission.{ConversationId, MDR401, MessageTypeIndic}
import play.api.libs.json.{Json, OFormat}

case class AuditFileSubmission(regime: String,
                               subscriptionId: String,
                               conversationId: ConversationId,
                               filename: String,
                               fileSize: String,
                               mimeType: String,
                               submissionType: String,
                               reportType: String
)

object AuditFileSubmission {

  implicit val formats: OFormat[AuditFileSubmission] = Json.format[AuditFileSubmission]

  def apply(subscriptionId: String,
            conversationId: ConversationId,
            filename: String,
            fileSize: String,
            mimeType: String,
            mdrBodyCount: Int,
            messageTypeIndic: MessageTypeIndic,
            docTypeIndic: Option[String]
  ): AuditFileSubmission = {
    val (submissionType, reportType) = (mdrBodyCount, messageTypeIndic, docTypeIndic) match {
      case (count, MDR401, _) if count > 1               => ("MultipleReports", "NewInformation")
      case (count, _, _) if count > 1                    => ("MultipleReports", "Corrections/Deletions")
      case (_, _, Some("OECD1"))                         => ("SingleReport", "NewInformation")
      case (_, _, Some("OECD0")) | (_, _, Some("OECD2")) => ("SingleReport", "Correction")
      case (_, _, Some("OECD3"))                         => ("SingleReport", "Deletion")
      case _                                             => ("SingleReport", "Other")
    }

    AuditFileSubmission(
      "MDR",
      subscriptionId,
      conversationId,
      filename,
      fileSize,
      mimeType,
      submissionType,
      reportType
    )
  }
}
