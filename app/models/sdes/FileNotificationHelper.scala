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

package models.sdes

import models.submissions.SubmissionDetails

object FileNotificationHelper {

  def createFileNotificationRequest(submissionDetails: SubmissionDetails,
                                    informationType: String,
                                    recipientOrSender: String,
                                    correlationId: String,
                                    metaData: Option[Map[String, String]] = None
  ): FileTransferNotification =
    FileTransferNotification(
      informationType,
      File(
        Some(recipientOrSender),
        submissionDetails.fileName,
        Some(submissionDetails.documentUrl),
        Checksum(SHA2, submissionDetails.checkSum),
        submissionDetails.fileSize.toInt,
        if (metaData.isEmpty) {
          List.empty[Property]
        } else mapToProperty(metaData.get)
      ),
      Audit(
        correlationId
      )
    )

  private def mapToProperty(metaData: Map[String, String]): List[Property] = metaData.toList map { md => Property(md._1, md._2) }
}
