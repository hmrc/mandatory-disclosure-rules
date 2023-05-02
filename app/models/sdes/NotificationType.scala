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

import play.api.libs.json.{__, JsString, Reads, Writes}

sealed abstract class NotificationType extends Product with Serializable

object NotificationType {

  case object FileReady extends NotificationType
  case object FileReceived extends NotificationType
  case object FileProcessingFailure extends NotificationType
  case object FileProcessed extends NotificationType

  implicit lazy val reads: Reads[NotificationType] =
    __.read[String].flatMap {
      case "FileReady"             => Reads.pure(FileReady)
      case "FileReceived"          => Reads.pure(FileReceived)
      case "FileProcessingFailure" => Reads.pure(FileProcessingFailure)
      case "FileProcessed"         => Reads.pure(FileProcessed)
      case _                       => Reads.failed("Invalid value for notification type")
    }

  implicit lazy val writes: Writes[NotificationType] =
    Writes {
      case FileReady             => JsString("FileReady")
      case FileReceived          => JsString("FileReceived")
      case FileProcessingFailure => JsString("FileProcessingFailure")
      case FileProcessed         => JsString("FileProcessed")
    }
}
