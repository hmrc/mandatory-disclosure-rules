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
import models.xml.FileErrorCode.UnknownFileErrorCode
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FileErrorCodeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "FileErrorCode" - {
    "read errorCode" in {
      for (errorCode <- FileErrorCode.values) {
        val xml = <Code>{errorCode.code}</Code>
        FileErrorCode.xmlReads.read(xml) mustBe ParseSuccess(errorCode)
      }
    }

    "read unknown errorCode" in {
      val xml = <Code>{50000}</Code>
      FileErrorCode.xmlReads.read(xml) mustBe ParseSuccess(UnknownFileErrorCode("50000"))
    }

    "return ParseFailureError for invalid value" in {
      val xml = <Code>Invalid</Code>
      FileErrorCode.xmlReads.read(xml) mustBe an[ParseFailure]
    }
  }
}
