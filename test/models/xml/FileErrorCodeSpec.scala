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
import models.xml.FileErrorCode.{MessageRefIDHasAlreadyBeenUsed, UnknownFileErrorCode}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class FileErrorCodeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "FileErrorCode" - {
    "read errorCode" in {
      for (errorCode <- FileErrorCode.values) {
        val xml = <Code>{errorCode.code}</Code>
        XmlReads[FileErrorCode].reads(xml) mustBe JsSuccess(errorCode)
      }
    }

    "read unknown errorCode" in {
      val xml = <Code>{50000}</Code>
      XmlReads[FileErrorCode].reads(xml) mustBe JsSuccess(UnknownFileErrorCode("50000"))
    }

    "return ParseFailureError for invalid value" in {
      val xml = <Code>Invalid</Code>
      XmlReads[FileErrorCode].reads(xml) mustBe an[JsError]
    }

    "deserialize from JSON correctly" in {
      val json      = JsString("50009")
      val errorCode = Json.fromJson[FileErrorCode](json).asOpt
      errorCode shouldBe Some(MessageRefIDHasAlreadyBeenUsed)
    }

    "deserialize from JSON with unknown code correctly" in {
      val json      = JsString("99998")
      val errorCode = Json.fromJson[FileErrorCode](json).asOpt
      errorCode shouldBe Some(UnknownFileErrorCode("99998"))
    }

    "read unknown errorCode from XML" in {
      val xml = <Code>50000</Code>
      XmlReads[FileErrorCode].reads(xml) shouldBe JsSuccess(UnknownFileErrorCode("50000"))
    }

    "return ParseFailure for invalid value from XML" in {
      val xml = <Code>Invalid</Code>
      XmlReads[FileErrorCode].reads(xml) shouldBe an[JsError]
    }
  }
}
