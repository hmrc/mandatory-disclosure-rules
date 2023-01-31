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
import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import models.xml.FileErrorCode.MessageRefIDHasAlreadyBeenUsed
import models.xml.RecordErrorCode.MessageTypeIndic

class ValidationErrorsSpec extends SpecBase {

  "ValidationErrors" - {
    "must read xml as ValidationErrors for the status 'Rejected'" in {

      val xml = <gsm:ValidationErrors>
                  <gsm:FileError>
                    <gsm:Code>50009</gsm:Code>
                    <gsm:Details Language="EN">Duplicate message ref ID</gsm:Details>
                  </gsm:FileError>
                  <gsm:RecordError>
                    <gsm:Code>80010</gsm:Code>
                    <gsm:Details Language="EN">A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both</gsm:Details>
                    <gsm:DocRefIDInError>asjdhjjhjssjhdjshdAJGSJJS</gsm:DocRefIDInError>
                  </gsm:RecordError>
                </gsm:ValidationErrors>

      XmlReader.of[ValidationErrors].read(xml) mustBe ParseSuccess(
        ValidationErrors(
          Some(List(FileErrors(MessageRefIDHasAlreadyBeenUsed, Some("Duplicate message ref ID")))),
          Some(
            List(
              RecordError(
                MessageTypeIndic,
                Some("A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both"),
                Some(List("asjdhjjhjssjhdjshdAJGSJJS"))
              )
            )
          )
        )
      )

    }

    "must read xml as ValidationErrors for the status 'Accepted'" in {

      val xml = <gsm:ValidationErrors></gsm:ValidationErrors>

      XmlReader.of[ValidationErrors].read(xml) mustBe ParseSuccess(ValidationErrors(None, None))

    }
  }
}
