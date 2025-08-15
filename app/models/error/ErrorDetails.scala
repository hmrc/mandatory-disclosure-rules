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

package models.error

import play.api.libs.json.{Json, OFormat}

case class SourceFaultDetail(detail: Seq[String])

object SourceFaultDetail {
  implicit val format: OFormat[SourceFaultDetail] = Json.format[SourceFaultDetail]
}

case class ErrorDetail(
  timestamp: String,
  correlationId: Option[String],
  errorCode: String,
  errorMessage: String,
  source: String,
  sourceFaultDetail: Option[SourceFaultDetail]
)

object ErrorDetail {
  implicit val format: OFormat[ErrorDetail] = Json.format[ErrorDetail]
}

case class ErrorDetails(errorDetail: ErrorDetail)

object ErrorDetails {
  implicit val format: OFormat[ErrorDetails] = Json.format[ErrorDetails]
}
