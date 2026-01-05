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

package models.upscan

import base.SpecBase
import play.api.libs.json._

import java.time.Instant

class UpscanResponseSpec extends SpecBase {

  val reference: Reference   = Reference("file-reference")
  val uploadForm: UploadForm = UploadForm("upload-url", Map("field1" -> "value1", "field2" -> "value2"))

  "UpscanInitiateResponse" - {

    val upscanInitiateResponse = UpscanInitiateResponse(reference, "post-target", Map("field1" -> "value1"))

    "format" - {

      "must serialize and deserialize UpscanInitiateResponse correctly" in {
        val json = Json.obj(
          "fileReference" -> "file-reference",
          "postTarget"    -> "post-target",
          "formFields"    -> Json.obj("field1" -> "value1")
        )
        Json.fromJson[UpscanInitiateResponse](json) mustBe JsSuccess(upscanInitiateResponse)

        Json.toJson(upscanInitiateResponse) mustBe json
      }
    }
  }

  "Reference" - {

    "implicit formats" - {

      "must serialize and deserialize Reference correctly" in {
        val json = JsString("file-reference")
        Json.fromJson[Reference](json) mustBe JsSuccess(reference)
        Json.toJson(reference) mustBe json
      }
    }
  }

  "PreparedUpload" - {

    val preparedUpload = PreparedUpload(reference, uploadForm)

    "format" - {

      "must serialize and deserialize PreparedUpload correctly" in {
        val json = Json.obj(
          "reference"     -> "file-reference",
          "uploadRequest" -> Json.obj(
            "href"   -> "upload-url",
            "fields" -> Json.obj("field1" -> "value1", "field2" -> "value2")
          )
        )
        Json.fromJson[PreparedUpload](json) mustBe JsSuccess(preparedUpload)
        Json.toJson(preparedUpload) mustBe json
      }
    }

    "toUpscanInitiateResponse" - {

      "must convert PreparedUpload to UpscanInitiateResponse" in {
        val expectedResponse = UpscanInitiateResponse(reference, "upload-url", Map("field1" -> "value1", "field2" -> "value2"))
        preparedUpload.toUpscanInitiateResponse mustBe expectedResponse
      }
    }
  }

  "CallbackBody" - {

    val uploadTime         = Instant.now()
    val uploadDetails      = UploadDetails(uploadTime, "checksum", "mime-type", "file-name", 123L)
    val readyCallbackBody  = ReadyCallbackBody(reference, "download-url", uploadDetails)
    val failedCallbackBody = FailedCallbackBody(reference, ErrorDetails("failure-reason", "message"))

    "reads" - {

      "must parse JSON to the corresponding CallbackBody" in {
        val readyJson = Json.obj(
          "fileStatus"    -> "READY",
          "reference"     -> "file-reference",
          "downloadUrl"   -> "download-url",
          "uploadDetails" -> Json.obj(
            "uploadTimestamp" -> uploadTime,
            "checksum"        -> "checksum",
            "fileMimeType"    -> "mime-type",
            "fileName"        -> "file-name",
            "size"            -> 123L
          )
        )
        Json.fromJson[CallbackBody](readyJson) mustBe JsSuccess(readyCallbackBody)

        val failedJson = Json.obj(
          "fileStatus"     -> "FAILED",
          "reference"      -> "file-reference",
          "failureDetails" -> Json.obj(
            "failureReason" -> "failure-reason",
            "message"       -> "message"
          )
        )
        Json.fromJson[CallbackBody](failedJson) mustBe JsSuccess(failedCallbackBody)
      }

      "must return JsError for invalid JSON" in {
        val invalidJson = Json.obj(
          "fileStatus" -> "INVALID",
          "reference"  -> "file-reference"
        )
        Json.fromJson[CallbackBody](invalidJson) mustBe JsError("""Invalid type distriminator: "INVALID"""")
      }
    }

    "ReadyCallbackBody" - {

      "format" - {

        "must serialize and deserialize ReadyCallbackBody correctly" in {
          val json = Json.obj(
            "reference"     -> "file-reference",
            "downloadUrl"   -> "download-url",
            "uploadDetails" -> Json.obj(
              "uploadTimestamp" -> uploadTime,
              "checksum"        -> "checksum",
              "fileMimeType"    -> "mime-type",
              "fileName"        -> "file-name",
              "size"            -> 123L
            )
          )
          Json.fromJson[ReadyCallbackBody](json) mustBe JsSuccess(readyCallbackBody)
          Json.toJson(readyCallbackBody) mustBe json
        }
      }
    }
  }
}
