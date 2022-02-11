package models.submission

import play.api.libs.json._

sealed trait MessageTypeIndic
case object MDR401 extends MessageTypeIndic
case object MDR402 extends MessageTypeIndic

object MessageTypeIndic {
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
