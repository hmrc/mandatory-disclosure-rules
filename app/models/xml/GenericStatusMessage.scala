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

package models.xml

import models.xml.ValidationErrors.xmlReads
import models.xml.ValidationStatus.xmlReads
import models.xml.XmlPath.*
import play.api.libs.json.{Json, OWrites}

case class GenericStatusMessage(validationErrors: ValidationErrors, status: ValidationStatus.Value)

object GenericStatusMessage {
  implicit val xmlReads: XmlReads[GenericStatusMessage] =
    for {
      validationErrors <- (__ \ "ValidationErrors").read[ValidationErrors]
      status           <- (__ \ "ValidationResult" \ "Status").read[ValidationStatus.Value]
    } yield GenericStatusMessage(validationErrors, status)

  implicit val writes: OWrites[GenericStatusMessage] = Json.writes[GenericStatusMessage]
}
