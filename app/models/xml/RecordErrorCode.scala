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

import com.lucidchart.open.xtract.{ParseError, ParseFailure, ParseSuccess, XmlReader}
import play.api.libs.json.{JsString, Writes}

import scala.xml.NodeSeq

sealed abstract class RecordErrorCode(val code: String)

object RecordErrorCode {
  case object DocRefIDAlreadyUsed extends RecordErrorCode("80000")
  case object DocRefIDFormat extends RecordErrorCode("80001")
  case object CorrDocRefIdUnknown extends RecordErrorCode("80002")
  case object CorrDocRefIdNoLongerValid extends RecordErrorCode("80003")
  case object CorrDocRefIdForNewData extends RecordErrorCode("80004")
  case object MissingCorrDocRefId extends RecordErrorCode("80005")
  case object ResendOption extends RecordErrorCode("80008")
  case object DeleteParentRecord extends RecordErrorCode("80009")
  case object MessageTypeIndic extends RecordErrorCode("80010")
  case object CorrDocRefIDTwiceInSameMessage extends RecordErrorCode("80011")
  case object UnknownDocRefID extends RecordErrorCode("80013")
  case object DocRefIDIsNoLongerValid extends RecordErrorCode("80014")
  case object CustomError extends RecordErrorCode("99999")
  case class UnknownRecordErrorCode(override val code: String) extends RecordErrorCode(code)

  val values: Seq[RecordErrorCode] = Seq(
    DocRefIDAlreadyUsed,
    DocRefIDFormat,
    CorrDocRefIdUnknown,
    CorrDocRefIdNoLongerValid,
    CorrDocRefIdForNewData,
    MissingCorrDocRefId,
    ResendOption,
    DeleteParentRecord,
    MessageTypeIndic,
    CorrDocRefIDTwiceInSameMessage,
    UnknownDocRefID,
    DocRefIDIsNoLongerValid,
    CustomError
  )

  implicit val writes: Writes[RecordErrorCode] = Writes[RecordErrorCode] { x =>
    JsString(x.code)
  }

  implicit val xmlReads: XmlReader[RecordErrorCode] =
    (xml: NodeSeq) => {
      case class RecordErrorCodeParseError(message: String) extends ParseError
      values.find(x => x.code == xml.text) match {
        case Some(errorCode) => ParseSuccess(errorCode)
        case None =>
          try ParseSuccess(UnknownRecordErrorCode(Integer.parseInt(xml.text).toString))
          catch {
            case _: Exception => ParseFailure(RecordErrorCodeParseError(s"Invalid or missing RecordErrorCode: ${xml.text}"))
          }
      }
    }
}
