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

import models.xml.GenericStatusMessage.xmlReads
import models.xml.XmlPath.*
import models.xml.XmlPrimitiveReads.*
import play.api.libs.json.{Json, OWrites}

case class BREResponse(regime: String, conversationID: String, genericStatusMessage: GenericStatusMessage)

object BREResponse {

  implicit val xmlReads: XmlReads[BREResponse] =
    for {
      regime               <- (__ \ "requestCommon" \ "regime").read[String]
      conversationId       <- (__ \ "requestCommon" \ "conversationID").read[String]
      genericStatusMessage <- (__ \ "requestDetail" \ "GenericStatusMessage").read[GenericStatusMessage]
    } yield BREResponse(regime, conversationId, genericStatusMessage)

  implicit val writes: OWrites[BREResponse] = Json.writes[BREResponse]
}
