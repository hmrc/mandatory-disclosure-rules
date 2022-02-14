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

package models.submission

import play.api.libs.json._

sealed trait MessageTypeIndic
case object MDR401 extends MessageTypeIndic
case object MDR402 extends MessageTypeIndic

object MessageTypeIndic {

  def fromString(typeIndic: String): MessageTypeIndic = typeIndic.toUpperCase match {
    case "MDR401" => MDR401
    case "MDR402" => MDR402
    case _        => throw new IllegalArgumentException
  }

  implicit val read: Reads[MessageTypeIndic] = (json: JsValue) => {
    val jsObject = json.asInstanceOf[JsObject]
    jsObject.value.get("_type") match {
      case Some(JsString("MDR401")) => JsSuccess(MDR401)
      case Some(JsString("MDR402")) => JsSuccess(MDR402)
      case Some(value)              => JsError(s"Unexpected value of _type: $value")
      case None                     => JsError("Missing _type field")
    }
  }

  implicit val write: Writes[MessageTypeIndic] = {
    case MDR401 => JsObject(Map("_type" -> JsString("MDR401")))
    case MDR402 => JsObject(Map("_type" -> JsString("MDR402")))
  }

}

case class MessageSpecData(messageRefId: String, messageTypeIndic: MessageTypeIndic)

object MessageSpecData {
  implicit val format: OFormat[MessageSpecData] = Json.format[MessageSpecData]
}
