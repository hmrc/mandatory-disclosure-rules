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

import models.WithName
import models.submissions.SubmissionDetails
import play.api.libs.json.{Json, OFormat}

import java.util.UUID

case class Property(
  name: String,
  value: String
)

object Property {
  implicit val format: OFormat[Property] = Json.format[Property]
}

sealed trait Algorithm
case object MD5 extends WithName("md5") with Algorithm
case object SHA512 extends WithName("SHA-512") with Algorithm
case object SHA256 extends WithName("SHA-256") with Algorithm

case class Checksum(
  algorithm: String,
  value: String
)

object Checksum {
  implicit val format: OFormat[Checksum] = Json.format[Checksum]
}

case class File(
  recipientOrSender: Option[String],
  name: String,
  location: Option[String],
  checksum: Checksum,
  size: Int,
  properties: List[Property]
)

object File {
  implicit val format: OFormat[File] = Json.format[File]
}

case class Audit(
  correlationID: String
)

object Audit {
  implicit val format: OFormat[Audit] = Json.format[Audit]
}

case class FileTransferNotification(
  informationType: String,
  file: File,
  audit: Audit
)

object FileTransferNotification {
  implicit val format: OFormat[FileTransferNotification] = Json.format[FileTransferNotification]

  def apply(submissionDetails: SubmissionDetails, informationType: String, recipientOrSender: String, correlationId: String): FileTransferNotification =
    FileTransferNotification(
      informationType,
      File(
        Some(recipientOrSender),
        submissionDetails.fileName,
        Some(submissionDetails.documentUrl),
        Checksum(SHA256.toString, submissionDetails.checkSum),
        submissionDetails.fileSize.toInt,
        List.empty[Property] //ToDo metaData will be transferred here ?
      ),
      Audit(
        correlationId
      )
    )
}
