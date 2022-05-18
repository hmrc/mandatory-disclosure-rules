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
import play.api.libs.json.{Json, OWrites}

case class BREResponse(regime: String, conversationID: String, genericStatusMessage: GenericStatusMessage)

object BREResponse {

  implicit val xmlReader: XmlReader[BREResponse] = (
    (__ \ "requestCommon" \ "regime").read[String],
    (__ \ "requestCommon" \ "conversationID").read[String],
    (__ \ "requestDetail" \ "GenericStatusMessage").read[GenericStatusMessage]
  ).mapN(apply)

  implicit val writes: OWrites[BREResponse] = Json.writes[BREResponse]
}
