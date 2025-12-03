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

package models.xml

import models.xml.XmlPath._
import models.xml.XmlPrimitiveReads._
import play.api.libs.json.{Json, OFormat}

case class RecordError(code: RecordErrorCode, details: Option[String], docRefIDInError: Option[Seq[String]])

object RecordError {

  implicit val xmlReads: XmlReads[RecordError] =
    for {
      code            <- (__ \ "Code").read[RecordErrorCode]
      details         <- (__ \ "Details").readOpt[String]
      docRefIDInError <- (__ \ "DocRefIDInError").readListOpt[String]
    } yield RecordError(code, details, docRefIDInError)

  implicit val format: OFormat[RecordError] = Json.format[RecordError]
}
