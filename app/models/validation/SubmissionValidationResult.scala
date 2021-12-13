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

sealed trait SubmissionValidationResult
object SubmissionValidationResult {

  implicit val validationWrites: Format[SubmissionValidationResult] = new Format[SubmissionValidationResult] {

    override def reads(json: JsValue): JsResult[SubmissionValidationResult] =
      json
        .validate[SubmissionValidationSuccess]
        .orElse(
          json.validate[SubmissionValidationFailure]
        )

    override def writes(o: SubmissionValidationResult): JsValue = o match {
      case m @ SubmissionValidationSuccess(_) => SubmissionValidationSuccess.format.writes(m)
      case m @ SubmissionValidationFailure(_) => SubmissionValidationFailure.format.writes(m)
    }
  }
}

case class ValidationErrors(errors: Seq[GenericError])

object ValidationErrors {
  implicit val format = Json.format[ValidationErrors]
}

case class SubmissionValidationSuccess(boolean: Boolean) extends SubmissionValidationResult // ToDo change to metadata when available

object SubmissionValidationSuccess {
  implicit val format: OFormat[SubmissionValidationSuccess] = Json.format[SubmissionValidationSuccess]
}

case class SubmissionValidationFailure(validationErrors: ValidationErrors) extends SubmissionValidationResult

case class SubmissionValidationInvalid() extends SubmissionValidationResult

object SubmissionValidationFailure {
  implicit val format: OFormat[SubmissionValidationFailure] = Json.format[SubmissionValidationFailure]
}
