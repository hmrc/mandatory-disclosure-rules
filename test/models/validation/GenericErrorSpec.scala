/*
 * Copyright 2025 HM Revenue & Customs
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
import models.validation.{GenericError, Message}
import play.api.libs.json.Json

class GenericErrorSpec extends SpecBase {

  "GenericError" - {
    "serialize to JSON" in {
      val message      = Message("error.message", Seq("param1", "param2"))
      val genericError = GenericError(123, message)
      val expectedJson = Json.parse("""
                                      |{
                                      |  "lineNumber": 123,
                                      |  "message": {
                                      |    "messageKey": "error.message",
                                      |    "args": ["param1", "param2"]
                                      |  }
                                      |}
                                      |""".stripMargin)

      val json = Json.toJson(genericError)

      json mustBe expectedJson
    }

    "deserialize from JSON" in {
      val json = Json.parse("""
                              |{
                              |  "lineNumber": 456,
                              |  "message": {
                              |    "messageKey": "another.error",
                              |    "args": []
                              |  }
                              |}
                              |""".stripMargin)
      val expectedGenericError = GenericError(456, Message("another.error"))

      val genericError = json.as[GenericError]

      genericError mustBe expectedGenericError
    }

    "order by lineNumber and messageKey" in {
      val genericError1 = GenericError(123, Message("error.message", Seq("param1", "param2")))
      val genericError2 = GenericError(456, Message("another.error"))
      val genericError3 = GenericError(123, Message("another.error"))

      val orderedErrors = List(genericError1, genericError2, genericError3).sorted

      orderedErrors mustBe List(genericError3, genericError1, genericError2)
    }
  }

}
