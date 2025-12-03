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

package models.xml

import play.api.libs.json._

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

  implicit val reads: Reads[RecordErrorCode] = __.read[String].map {
    case "80000"   => DocRefIDAlreadyUsed
    case "80001"   => DocRefIDFormat
    case "80002"   => CorrDocRefIdUnknown
    case "80003"   => CorrDocRefIdNoLongerValid
    case "80004"   => CorrDocRefIdForNewData
    case "80005"   => MissingCorrDocRefId
    case "80008"   => ResendOption
    case "80009"   => DeleteParentRecord
    case "80010"   => MessageTypeIndic
    case "80011"   => CorrDocRefIDTwiceInSameMessage
    case "80013"   => UnknownDocRefID
    case "80014"   => DocRefIDIsNoLongerValid
    case "99999"   => CustomError
    case otherCode => UnknownRecordErrorCode(otherCode)
  }

  val byCode: Map[String, RecordErrorCode] = values.map(f => f.code -> f).toMap

  implicit val xmlReads: XmlReads[RecordErrorCode] =
    XmlReads.from { ns =>
      val txt = ns.text.trim
      byCode.get(txt) match {
        case Some(value) => JsSuccess(value)
        case None if txt.nonEmpty =>
          try
            JsSuccess(UnknownRecordErrorCode(Integer.parseInt(txt).toString))
          catch {
            case _: Exception => JsError(JsonValidationError(s"Invalid or missing RecordErrorCode: $txt"))
          }
        case _ => JsError("error.required.recordErrorCode")
      }
    }
}
