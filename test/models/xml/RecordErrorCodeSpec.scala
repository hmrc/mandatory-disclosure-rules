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

import base.SpecBase
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess}
import models.xml.RecordErrorCode.UnknownRecordErrorCode
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsNull, JsString, Json, Writes}

class RecordErrorCodeSpec extends SpecBase {

  implicit val errorCodeWrites: Writes[RecordErrorCode] = Writes[RecordErrorCode] { errorCode =>
    JsString(errorCode.code)
  }

  val ErrorCodes: Iterator[String] =
    Iterator("80000", "80001", "80002", "80003", "80004", "80005", "80008", "80009", "80010", "80011", "80013", "80014", "99999")

  def matchErrorCode(code: String): RecordErrorCode = code match {
    case "80000" => RecordErrorCode.DocRefIDAlreadyUsed
    case "80001" => RecordErrorCode.DocRefIDFormat
    case "80002" => RecordErrorCode.CorrDocRefIdUnknown
    case "80003" => RecordErrorCode.CorrDocRefIdNoLongerValid
    case "80004" => RecordErrorCode.CorrDocRefIdForNewData
    case "80005" => RecordErrorCode.MissingCorrDocRefId
    case "80008" => RecordErrorCode.ResendOption
    case "80009" => RecordErrorCode.DeleteParentRecord
    case "80010" => RecordErrorCode.MessageTypeIndic
    case "80011" => RecordErrorCode.CorrDocRefIDTwiceInSameMessage
    case "80013" => RecordErrorCode.UnknownDocRefID
    case "80014" => RecordErrorCode.DocRefIDIsNoLongerValid
    case "99999" => RecordErrorCode.CustomError
  }

  "RecordErrorCode" - {
    "read errorCode" in {
      for (errorCode <- RecordErrorCode.values) {
        val xml = <Code>{errorCode.code}</Code>
        RecordErrorCode.xmlReads.read(xml) mustBe ParseSuccess(errorCode)
      }
    }

    "read unknown errorCode" in {
      val xml = <Code>{50000}</Code>
      RecordErrorCode.xmlReads.read(xml) mustBe ParseSuccess(UnknownRecordErrorCode("50000"))
    }

    "return ParseFailureError for invalid value" in {
      val xml = <Code>Invalid</Code>
      RecordErrorCode.xmlReads.read(xml) mustBe an[ParseFailure]
    }

    "deserialize from JSON correctly" in {
      while (ErrorCodes.hasNext) {
        val code      = ErrorCodes.next()
        val json      = JsString(code)
        val errorCode = Json.fromJson[RecordErrorCode](json).asOpt
        errorCode mustBe Some(matchErrorCode(code))
      }
    }

    "deserialize from JSON with unknown code correctly" in {
      val json      = JsString("99998")
      val errorCode = Json.fromJson[RecordErrorCode](json).asOpt
      errorCode mustBe Some(RecordErrorCode.UnknownRecordErrorCode("99998"))
    }

    "deserialize from JSON with null value should return None" in {
      val json      = JsNull
      val errorCode = Json.fromJson[RecordErrorCode](json).asOpt
      errorCode shouldBe None
    }

  }
}
