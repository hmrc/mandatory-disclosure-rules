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
      val json      = JsString("80005")
      val errorCode = Json.fromJson[RecordErrorCode](json).asOpt
      errorCode mustBe Some(RecordErrorCode.MissingCorrDocRefId)
    }

    "deserialize from JSON correctly - DocRefIDIsNoLongerValid" in {
      val json      = JsString("80014")
      val errorCode = Json.fromJson[RecordErrorCode](json).asOpt
      errorCode mustBe Some(RecordErrorCode.DocRefIDIsNoLongerValid)
    }

    "deserialize from JSON correctly - CustomError" in {
      val json      = JsString("99999")
      val errorCode = Json.fromJson[RecordErrorCode](json).asOpt
      errorCode mustBe Some(RecordErrorCode.CustomError)
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
