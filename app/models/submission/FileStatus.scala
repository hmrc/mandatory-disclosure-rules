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

import models.xml.ValidationErrors
import play.api.libs.json.*

sealed trait FileStatus

case object Pending extends FileStatus
case object Accepted extends FileStatus
case object RejectedSDES extends FileStatus
case object RejectedSDESVirus extends FileStatus
case class Rejected(error: ValidationErrors) extends FileStatus {
  // $COVERAGE-OFF$
  override def toString: String = "Rejected"
  // $COVERAGE-ON$
}

object FileStatus {
  implicit val rejectedFormat: OFormat[Rejected] = Json.format[Rejected]

  implicit val format: OFormat[FileStatus] = {
    val pending           = Json.obj("type" -> "Pending")
    val accepted          = Json.obj("type" -> "Accepted")
    val rejectedSDES      = Json.obj("type" -> "RejectedSDES")
    val rejectedSDESVirus = Json.obj("type" -> "RejectedSDESVirus")

    new OFormat[FileStatus] {
      def writes(fs: FileStatus): JsObject = fs match {
        case Pending           => pending
        case Accepted          => accepted
        case RejectedSDES      => rejectedSDES
        case RejectedSDESVirus => rejectedSDESVirus
        case r: Rejected       => rejectedFormat.writes(r) + ("type" -> JsString("Rejected"))
      }

      def reads(json: JsValue): JsResult[FileStatus] =
        (json \ "type").validate[String].flatMap {
          case "Pending"           => JsSuccess(Pending)
          case "Accepted"          => JsSuccess(Accepted)
          case "RejectedSDES"      => JsSuccess(RejectedSDES)
          case "RejectedSDESVirus" => JsSuccess(RejectedSDESVirus)
          case "Rejected"          => rejectedFormat.reads(json)
          case other               => JsError(s"Unknown FileStatus type: $other")
        }
    }
  }
}
