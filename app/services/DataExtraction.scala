package services

import models.submission.{MessageSpecData, MessageTypeIndic}

import javax.inject.Inject
import scala.xml.Elem

@Inject
class DataExtraction()() {

  def messageSpecData(xml: Elem): Option[MessageSpecData] =
    for {
      messageID <- (xml \\ "MessageRefId").headOption
      typeIndic <- (xml \\ "MessageTypeIndic").headOption.map(x => MessageTypeIndic.fromString(x.text))
    } yield MessageSpecData(messageID.text, typeIndic)

}
