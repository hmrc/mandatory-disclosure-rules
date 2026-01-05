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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.time.Instant

case class UpscanInitiateResponse(
  fileReference: Reference,
  postTarget: String,
  formFields: Map[String, String]
)

object UpscanInitiateResponse {
  implicit val format: OFormat[UpscanInitiateResponse] = Json.format[UpscanInitiateResponse]
}

case class Reference(value: String) extends AnyVal

case class UploadForm(href: String, fields: Map[String, String])

object Reference {

  implicit val referenceReader: Reads[Reference] =
    Reads.StringReads.map(Reference(_))

  implicit val referenceWrites: Writes[Reference] =
    Writes[Reference](x => JsString(x.value))
}

case class PreparedUpload(reference: Reference, uploadRequest: UploadForm) {

  def toUpscanInitiateResponse: UpscanInitiateResponse = {
    val fileReference = reference
    val postTarget    = uploadRequest.href
    val formFields    = uploadRequest.fields
    UpscanInitiateResponse(fileReference, postTarget, formFields)
  }
}

object PreparedUpload {
  implicit val uploadFormFormat: Format[UploadForm] = Json.format[UploadForm]
  implicit val format: Format[PreparedUpload]       = Json.format[PreparedUpload]
}

sealed trait CallbackBody {
  def reference: Reference
}

object CallbackBody {

  implicit val uploadDetailsReads: Reads[UploadDetails] = Json.reads[UploadDetails]

  implicit val failedCallbackBodyReads: Reads[FailedCallbackBody] = Json.reads[FailedCallbackBody]

  implicit val reads: Reads[CallbackBody] = new Reads[CallbackBody] {

    override def reads(json: JsValue): JsResult[CallbackBody] =
      json \ "fileStatus" match {
        case JsDefined(JsString("READY")) =>
          implicitly[Reads[ReadyCallbackBody]].reads(json)
        case JsDefined(JsString("FAILED")) =>
          implicitly[Reads[FailedCallbackBody]].reads(json)
        case JsDefined(value) => JsError(s"Invalid type distriminator: $value")
        case _                => JsError(s"Missing type distriminator")
      }
  }
}

case class UploadDetails(
  uploadTimestamp: Instant,
  checksum: String,
  fileMimeType: String,
  fileName: String,
  size: Long
)

object UploadDetails {
  implicit val format: OFormat[UploadDetails] = Json.format[UploadDetails]
}

case class ReadyCallbackBody(
  reference: Reference,
  downloadUrl: String,
  uploadDetails: UploadDetails
) extends CallbackBody

object ReadyCallbackBody {
  // must be in scope to create Reads for ReadyCallbackBody

  implicit val writes: OWrites[ReadyCallbackBody] = OWrites { readyCallbackBody =>
    Json.obj(
      "reference" -> Json.toJsFieldJsValueWrapper(
        readyCallbackBody.reference
      ),
      "downloadUrl"   -> readyCallbackBody.downloadUrl,
      "uploadDetails" -> Json.toJsFieldJsValueWrapper(
        readyCallbackBody.uploadDetails
      )
    )
  }

  implicit val reads: Reads[ReadyCallbackBody] = (
    (__ \ "reference").read[Reference] and
      (__ \ "downloadUrl").read[String] and
      (__ \ "uploadDetails").read[UploadDetails]
  )((ref, url, ud) => ReadyCallbackBody(ref, url, ud))
}

case class FailedCallbackBody(
  reference: Reference,
  failureDetails: ErrorDetails
) extends CallbackBody
