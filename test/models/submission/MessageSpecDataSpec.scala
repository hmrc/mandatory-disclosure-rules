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

package models.submission

import base.SpecBase
import play.api.libs.json.Json

class MessageSpecDataSpec extends SpecBase {

  "MessageSpecDataSpec" - {
    "must serialize MessageSpec" in {
      val msd = MessageSpecData("XDSG111111", MDR401, 2, "OECD1", MultipleNewInformation)
      val expectedJson =
        Json.parse(
          """{"messageRefId":"XDSG111111","messageTypeIndic":"MDR401","mdrBodyCount":2,"docTypeIndic":"OECD1","reportType":"MultipleNewInformation"}"""
        )
      Json.toJson(msd) mustBe expectedJson
    }
    "must deserialize MessageSpec" in {
      val json = Json.parse(
        """{"messageRefId":"XDSG333333","messageTypeIndic":"MDR402","mdrBodyCount":2,"docTypeIndic":"OECD1","reportType":"MultipleNewInformation"}"""
      )
      val expected = MessageSpecData("XDSG333333", MDR402, 2, "OECD1", MultipleNewInformation)

      json.as[MessageSpecData] mustEqual expected
    }
  }
}
