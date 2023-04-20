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
import models.submissions.SubmissionDetails
import play.api.libs.json.Json

class FileTransferNotificationSpec extends SpecBase {

  "FileTransferNotification" - {
    val information       = "0123456789"
    val recipientOrSender = "mdr"
    val checksum          = "1234"
    val fileSize          = 12345L
    val submissionDetails = SubmissionDetails("test.xml", "MDR1", fileSize, "http://localhost/", checksum)
    val correlationID     = "aa928"

    val fileTransferNotification = FileTransferNotification(
      information,
      File(
        Some(recipientOrSender),
        submissionDetails.fileName,
        Some(submissionDetails.documentUrl),
        Checksum("SHA-256", submissionDetails.checkSum),
        submissionDetails.fileSize.toInt,
        List.empty[Property]
      ),
      Audit(
        correlationID
      )
    )

    "must correctly create a FileTransferNotification from submissionDetails and config" in {
      FileTransferNotification(submissionDetails, information, recipientOrSender, correlationID) mustBe fileTransferNotification
    }
    "must serialise to json correctly" in {
      val expectedJson =
        """{"informationType":"0123456789","file":{"recipientOrSender":"mdr","name":"test.xml",
          |"location":"http://localhost/","checksum":{"algorithm":"SHA-256","value":"1234"},
          |"size":12345,"properties":[]},"audit":{"correlationID":"aa928"}}""".stripMargin

      Json.toJson(fileTransferNotification) mustBe Json.parse(expectedJson)
    }
    "must deserialise from json" in {
      val jsonString =
        """{"informationType":"0123456789","file":{"recipientOrSender":"mdr","name":"test.xml",
          |"location":"http://localhost/","checksum":{"algorithm":"SHA-256","value":"1234"},
          |"size":12345,"properties":[]},"audit":{"correlationID":"aa928"}}""".stripMargin
      val fileTransferNotificationJson = Json.parse(jsonString)

      fileTransferNotificationJson.as[FileTransferNotification] mustBe fileTransferNotification
    }
  }
}
