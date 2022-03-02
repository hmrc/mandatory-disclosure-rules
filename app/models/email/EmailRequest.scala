/*
 * Copyright 2022 HM Revenue & Customs
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

package models.email

import play.api.libs.json.{Json, OFormat}

case class EmailRequest(to: List[String], templateId: String, parameters: Map[String, String])

object EmailRequest {
  implicit val format: OFormat[EmailRequest] = Json.format[EmailRequest]

  def fileUploadSubmission(email: String, name: Option[String], emailTemplate: String, submissionTime: String, messageRefId: String): EmailRequest = {

    val contactName = name.fold("Registrant")(name => name)

    EmailRequest(
      List(email),
      emailTemplate,
      Map("dateAndTime" -> submissionTime, "messageRefId" -> messageRefId, "contactName" -> contactName)
    )
  }
}
