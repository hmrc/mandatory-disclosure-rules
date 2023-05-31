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

package models.validation

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.Json

class GenericErrorSpec extends SpecBase {

  "GenericError" - {

    "serialize to JSON" in {
      val error = GenericError(1, Message("error.key", Seq("arg1", "arg2")))
      val expectedJson = Json.obj(
        "lineNumber" -> 1,
        "message" -> Json.obj(
          "messageKey" -> "error.key",
          "args"       -> Seq("arg1", "arg2")
        )
      )

      val json = Json.toJson(error)

      json mustBe expectedJson
    }

    "deserialize from JSON" in {
      val json = Json.obj(
        "lineNumber" -> 1,
        "message" -> Json.obj(
          "messageKey" -> "error.key",
          "args"       -> Seq("arg1", "arg2")
        )
      )
      val expectedError = GenericError(1, Message("error.key", Seq("arg1", "arg2")))

      val error = Json.fromJson[GenericError](json).get

      error mustBe expectedError
    }
  }
}
