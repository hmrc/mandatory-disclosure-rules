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

package models.xml

import com.lucidchart.open.xtract._
import play.api.libs.json.{__, JsString, Reads, Writes}

import scala.xml.NodeSeq

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
    NotMeantToBeReceivedByTheIndicatedJurisdiction
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
    case otherCode => UnknownFileErrorCode(otherCode)
  }

  implicit val xmlReads: XmlReader[FileErrorCode] =
    (xml: NodeSeq) => {
      case class FileErrorCodeParseError(message: String) extends ParseError
      values.find(x => x.code == xml.text) match {
        case Some(errorCode) => ParseSuccess(errorCode)
        case None =>
          try ParseSuccess(UnknownFileErrorCode(Integer.parseInt(xml.text).toString))
          catch {
            case _: Exception => ParseFailure(FileErrorCodeParseError(s"Invalid or missing FileErrorCode: ${xml.text}"))
          }
      }
    }

}
