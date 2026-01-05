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

package models.validation

import play.api.libs.json.{Json, OFormat}

case class GenericError(lineNumber: Int, message: Message)

case class Message(messageKey: String, args: Seq[String] = Seq.empty)

object Message {
  implicit val messageFormat: OFormat[Message] = Json.format[Message]
}

object GenericError {

  implicit def orderByLineNumber[A <: GenericError]: Ordering[A] =
    Ordering.by(ge => (ge.lineNumber, ge.message.messageKey))

  implicit val format: OFormat[GenericError] = Json.format[GenericError]
}
