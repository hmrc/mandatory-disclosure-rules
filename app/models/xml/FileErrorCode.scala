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

package models.xml

import play.api.libs.json.*

sealed abstract class FileErrorCode(val code: String)

object FileErrorCode {

  case object FailedSchemaValidation extends FileErrorCode("50007")
  case object InvalidMessageRefIDFormat extends FileErrorCode("50008")
  case object MessageRefIDHasAlreadyBeenUsed extends FileErrorCode("50009")
  case object FileContainsTestDataForProductionEnvironment extends FileErrorCode("50010")
  case object NotMeantToBeReceivedByTheIndicatedJurisdiction extends FileErrorCode("50012")
  case object CustomError extends FileErrorCode("99999")
  case class UnknownFileErrorCode(override val code: String) extends FileErrorCode(code)

  val values: Seq[FileErrorCode] = Seq(
    FailedSchemaValidation,
    InvalidMessageRefIDFormat,
    MessageRefIDHasAlreadyBeenUsed,
    FileContainsTestDataForProductionEnvironment,
    NotMeantToBeReceivedByTheIndicatedJurisdiction,
    CustomError
  )

  val fileErrorCodesForProblemStatus: Seq[FileErrorCode] = Seq(
    FailedSchemaValidation,
    InvalidMessageRefIDFormat,
    NotMeantToBeReceivedByTheIndicatedJurisdiction
  )

  implicit val writes: Writes[FileErrorCode] = Writes[FileErrorCode] { x =>
    JsString(x.code)
  }

  implicit val reads: Reads[FileErrorCode] = __.read[String].map {
    case "50007"   => FailedSchemaValidation
    case "50008"   => InvalidMessageRefIDFormat
    case "50009"   => MessageRefIDHasAlreadyBeenUsed
    case "50010"   => FileContainsTestDataForProductionEnvironment
    case "50012"   => NotMeantToBeReceivedByTheIndicatedJurisdiction
    case "99999"   => CustomError
    case otherCode => UnknownFileErrorCode(otherCode)
  }

  val byCode: Map[String, FileErrorCode] = values.map(f => f.code -> f).toMap

  implicit val xmlReads: XmlReads[FileErrorCode] =
    XmlReads.from { ns =>
      val txt = ns.text.trim
      byCode.get(txt) match {
        case Some(value)          => JsSuccess(value)
        case None if txt.nonEmpty =>
          try
            JsSuccess(UnknownFileErrorCode(Integer.parseInt(txt).toString))
          catch {
            case _: Exception => JsError(JsonValidationError(s"Invalid or missing FileErrorCode: $txt"))
          }
        case _ => JsError("error.required.fileErrorCode")
      }
    }

}
