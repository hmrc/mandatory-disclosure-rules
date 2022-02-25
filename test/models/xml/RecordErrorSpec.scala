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

import base.SpecBase
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, XmlReader}
import models.xml.RecordErrorCode.UnknownRecordErrorCode

import scala.xml.Elem

class RecordErrorSpec extends SpecBase {

  "RecordError" - {
    "must read xml as FileErrors for known error code" in {
      for (errorCode <- RecordErrorCode.values) {
        val xml: Elem = <gsm:RecordError>
                          <gsm:Code>{errorCode.code}</gsm:Code>
                          <gsm:Details Language="EN">error details</gsm:Details>
                          <gsm:DocRefIDInError>asjdhjjhjssjhdjshdAJGSJJS</gsm:DocRefIDInError>
                        </gsm:RecordError>

        XmlReader.of[RecordError].read(xml) mustBe ParseSuccess(
          RecordError(errorCode, Some("error details"), Some(Seq("asjdhjjhjssjhdjshdAJGSJJS")))
        )
      }
    }

    "must read xml as FileErrors for unknown code" in {
      val xml: Elem = <gsm:FileError>
        <gsm:Code>50011</gsm:Code>
        <gsm:Details Language="EN">error message</gsm:Details>
      </gsm:FileError>

      XmlReader.of[RecordError].read(xml) mustBe ParseSuccess(RecordError(UnknownRecordErrorCode("50011"), Some("error message"), None))
    }

    "must fail to read xml as FileErrors for invalid code" in {
      val xml: Elem = <gsm:FileError>
        <gsm:Code>invalid</gsm:Code>
        <gsm:Details Language="EN">error message</gsm:Details>
      </gsm:FileError>

      XmlReader.of[RecordError].read(xml) mustBe an[ParseFailure]
    }
  }
}
