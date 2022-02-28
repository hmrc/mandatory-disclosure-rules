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
import models.xml.FileErrorCode.UnknownFileErrorCode

import scala.xml.Elem

class FileErrorsSpec extends SpecBase {

  "FileErrors" - {
    "must read xml as FileErrors for known error code" in {
      for (errorCode <- FileErrorCode.values) {
        val xml: Elem = <gsm:FileError>
                          <gsm:Code>{errorCode.code}</gsm:Code>
                          <gsm:Details Language="EN">Duplicate message ref ID</gsm:Details>
                        </gsm:FileError>

        XmlReader.of[FileErrors].read(xml) mustBe ParseSuccess(FileErrors(errorCode, Some("Duplicate message ref ID")))
      }
    }

    "must read xml as FileErrors for unknown code" in {
      val xml: Elem = <gsm:FileError>
        <gsm:Code>50011</gsm:Code>
        <gsm:Details Language="EN">error message</gsm:Details>
      </gsm:FileError>

      XmlReader.of[FileErrors].read(xml) mustBe ParseSuccess(FileErrors(UnknownFileErrorCode("50011"), Some("error message")))
    }

    "must fail to read xml as FileErrors for invalid code" in {
      val xml: Elem = <gsm:FileError>
        <gsm:Code>invalid</gsm:Code>
        <gsm:Details Language="EN">error message</gsm:Details>
      </gsm:FileError>

      XmlReader.of[FileErrors].read(xml) mustBe an[ParseFailure]
    }
  }
}
