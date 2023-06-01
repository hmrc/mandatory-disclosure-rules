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

import base.SpecBase
import models.upscan.UploadId
import play.api.libs.json.{JsString, Json, Reads}

class UploadIdSpec extends SpecBase {

  "UploadId" - {
    val uploadId = UploadId("1234567890")

    "bind to query string" in {
      implicit val stringBinder: play.api.mvc.QueryStringBindable[String] = play.api.mvc.QueryStringBindable.bindableString
      val queryParam                                                      = implicitly[play.api.mvc.QueryStringBindable[UploadId]].unbind("uploadId", uploadId)
      queryParam mustBe "uploadId=1234567890"
    }

    "unbind from query string" in {
      implicit val stringBinder: play.api.mvc.QueryStringBindable[String] = play.api.mvc.QueryStringBindable.bindableString
      val queryParam                                                      = implicitly[play.api.mvc.QueryStringBindable[UploadId]].unbind("uploadId", uploadId)
      implicitly[play.api.mvc.QueryStringBindable[UploadId]].unbind("uploadId", uploadId) mustBe queryParam
    }

    "serialize to JSON" in {
      val json = Json.toJson(uploadId)
      json mustBe Json.parse("""{"value":"1234567890"}""")
    }

    "deserialize from JSON" in {
      val json   = Json.parse("""{"value":"1234567890"}""")
      val result = Json.fromJson[UploadId](json)
      result.isSuccess mustBe true
      result.get mustBe uploadId
    }

    "be convertible to String" in {
      uploadId.value mustBe "1234567890"
    }

    "read from string" in {
      val result = Reads.of[String].map(UploadId(_)).reads(JsString("1234567890"))
      result.isSuccess mustBe true
      result.get mustBe uploadId
    }

    "write to string" in {
      val result = UploadId.writesUploadId.writes(uploadId)
      result mustBe JsString("1234567890")
    }
  }
}
