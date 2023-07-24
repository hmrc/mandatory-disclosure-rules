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

package models.sdes

import base.SpecBase
import play.api.libs.json.Json

class ChecksumSpec extends SpecBase {

  "Checksum" - {
    "must serialise md5 to json correctly" in {
      Json.toJson(checkSumFactory(MD5)) mustBe Json.parse(expectedString("md5"))
    }
    "must deserialise md5 from json correctly" in {
      Json.parse(expectedString("md5")).as[Checksum] mustBe checkSumFactory(MD5)
    }
    "must serialise SHA1 to json correctly" in {
      Json.toJson(checkSumFactory(SHA1)) mustBe Json.parse(expectedString("SHA1"))
    }
    "must deserialise SHA1 from json correctly" in {
      Json.parse(expectedString("SHA1")).as[Checksum] mustBe checkSumFactory(SHA1)
    }
    "must serialise SHA2 to json correctly" in {
      Json.toJson(checkSumFactory(SHA2)) mustBe Json.parse(expectedString("SHA2"))
    }
    "must deserialise SHA2 from json correctly" in {
      Json.parse(expectedString("SHA2")).as[Checksum] mustBe checkSumFactory(SHA2)
    }
    "must serialise SHA256 to json correctly" in {
      Json.toJson(checkSumFactory(SHA256)) mustBe Json.parse(expectedString("SHA-256"))
    }
    "must deserialise SHA256 from json correctly" in {
      Json.parse(expectedString("SHA-256")).as[Checksum] mustBe checkSumFactory(SHA256)
    }
    "must serialise SHA512 to json correctly" in {
      Json.toJson(checkSumFactory(SHA512)) mustBe Json.parse(expectedString("SHA-512"))
    }
    "must deserialise SHA512 from json correctly" in {
      Json.parse(expectedString("SHA-512")).as[Checksum] mustBe checkSumFactory(SHA512)
    }
  }

  def checkSumFactory(algorithm: Algorithm) = {
    val encodedString = "1234"
    Checksum(algorithm, encodedString)
  }
  def expectedString(algorithm: String) =
    s"""{"algorithm":"SHA2","value":"1234"}""".stripMargin
      .replace("SHA2", algorithm)
}
