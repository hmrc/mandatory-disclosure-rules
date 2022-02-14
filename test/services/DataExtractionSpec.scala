package services

import base.SpecBase
import models.submission.{MDR401, MessageSpecData}
import models.validation.SaxParseError
import services.validation.XMLValidationService

import scala.collection.mutable.ListBuffer
import scala.xml.{Elem, XML}

class DataExtractionSpec extends SpecBase {

  "DataExtraction" - {
    "messageSpecData must return messageSpec data from a valid XML file" in {
      val service = app.injector.instanceOf[XMLValidationService]
      val extractor = app.injector.instanceOf[DataExtraction]
      val validSubmission = XML.loadFile("test/resources/mdr/validmdr.xml")

      val xml: Either[ListBuffer[SaxParseError], Elem] = service.validateXML(None, Some(validSubmission))

      xml match {
        case Right(validXml) => extractor.messageSpecData(validXml) mustBe MessageSpecData("GBXAMDR1234567", MDR401)
        case _ => fail("Invalid XML")
      }

    }
  }
}
