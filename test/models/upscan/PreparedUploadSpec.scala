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

package models.upscan

import base.SpecBase

class PreparedUploadSpec extends SpecBase {

  "PreparedUpload" - {
    "must produce a correct UpscanInitiateResponse" in {
      val uploadRequest = UploadForm("http://localhost/", Map("field1" -> "field2"))
      val reference     = Reference("xxxx")

      val preparedUpload = PreparedUpload(reference, uploadRequest)

      val result = preparedUpload.toUpscanInitiateResponse

      result.fileReference mustBe reference
      result.postTarget mustBe uploadRequest.href
      result.formFields mustBe uploadRequest.fields

    }

  }
}
