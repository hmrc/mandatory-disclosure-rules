/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json._

sealed trait UploadSubmissionValidationResult

object UploadSubmissionValidationResult {

  implicit val validationWrites = new Format[UploadSubmissionValidationResult] {

    override def reads(json: JsValue): JsResult[UploadSubmissionValidationResult] =
      json
        .validate[UploadSubmissionValidationSuccess]
        .orElse(
          json.validate[UploadSubmissionValidationFailure]
        )

    override def writes(o: UploadSubmissionValidationResult): JsValue = o match {
      case m @ UploadSubmissionValidationSuccess(_) => UploadSubmissionValidationSuccess.format.writes(m)
      case m @ UploadSubmissionValidationFailure(_) => UploadSubmissionValidationFailure.format.writes(m)
    }
  }
}

case class ValidationErrors(errors: Seq[GenericError])

object ValidationErrors {
  implicit val format = Json.format[ValidationErrors]
}

case class UploadSubmissionValidationSuccess(boolean: Boolean) extends UploadSubmissionValidationResult // ToDo change to metadata when available

object UploadSubmissionValidationSuccess {
  implicit val format: OFormat[UploadSubmissionValidationSuccess] = Json.format[UploadSubmissionValidationSuccess]
}

case class UploadSubmissionValidationFailure(validationErrors: ValidationErrors) extends UploadSubmissionValidationResult

case class UploadSubmissionValidationInvalid() extends UploadSubmissionValidationResult

object UploadSubmissionValidationFailure {
  implicit val format: OFormat[UploadSubmissionValidationFailure] = Json.format[UploadSubmissionValidationFailure]
}
