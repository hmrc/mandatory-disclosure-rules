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

import base.SpecBase
import models.submission.{MDR401, MessageSpecData, MultipleNewInformation}
import models.submissions.SubmissionDetails

class FileNotificationHelperSpec extends SpecBase {

  "FileNotificationHelper" - {
    val messageSpec       = MessageSpecData("x9999", MDR401, 2, "OECD1", MultipleNewInformation)
    val information       = "0123456789"
    val recipientOrSender = "mdr"
    val checksum          = "1234"
    val fileSize          = 12345L
    val submissionDetails = SubmissionDetails("test.xml", "MDR1", fileSize, "http://localhost/", checksum, messageSpec)
    val correlationID     = "aa928"

    val fileTransferNotification = FileTransferNotification(
      information,
      File(
        Some(recipientOrSender),
        submissionDetails.fileName,
        Some(submissionDetails.documentUrl),
        Checksum(SHA2, submissionDetails.checkSum),
        submissionDetails.fileSize.toInt,
        List.empty[Property]
      ),
      Audit(
        correlationID
      )
    )
    "must correctly create a FileTransferNotification from submissionDetails and config" in {
      FileNotificationHelper.createFileNotificationRequest(submissionDetails, information, recipientOrSender, correlationID) mustBe fileTransferNotification
    }
    "must correctly create a FileTransferNotification from submissionDetails and config with metadata" in {
      val metaData     = Map[String, String]("filename" -> "test", "id" -> "123")
      val propertyList = List(Property("filename", "test"), Property("id", "123"))
      val fileInfo     = fileTransferNotification.file.copy(properties = propertyList)

      val fileTransferNotificationWithProperties = fileTransferNotification.copy(file = fileInfo)
      FileNotificationHelper.createFileNotificationRequest(submissionDetails,
                                                           information,
                                                           recipientOrSender,
                                                           correlationID,
                                                           Some(metaData)
      ) mustBe fileTransferNotificationWithProperties
    }
  }
}
