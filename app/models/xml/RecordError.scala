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

package models.xml

import cats.implicits.catsSyntaxTuple3Semigroupal
import com.lucidchart.open.xtract.{__, XmlReader}

case class RecordError(code: RecordErrorCode, details: Option[String], docRefIDInError: Option[Seq[String]])

object RecordError {

  implicit val xmlReader: XmlReader[RecordError] = (
    (__ \ "Code").read[RecordErrorCode],
    (__ \ "Details").read[String].optional,
    (__ \ "DocRefIDInError").read(strictReadOptionSeq[String])
  ).mapN(apply)
}
