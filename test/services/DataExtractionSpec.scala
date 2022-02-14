package services

import base.SpecBase
import models.submission.{MDR401, MessageSpecData}
import services.validation.XMLValidationService

import scala.xml.XML

class DataExtractionSpec extends SpecBase {

  "DataExtraction" - {
    "messageSpecData must return messageSpec data from a valid XML file" in {
      val service = app.injector.instanceOf[XMLValidationService]
      val extractor = app.injector.instanceOf[DataExtraction]
      val validSubmission = XML.loadFile("test/resources/mdr/validmdr.xml")

      val xml = service.validateXML(None, Some(validSubmission)).right.get

      extractor.messageSpecData(xml) mustBe Some(MessageSpecData("GBXAMDR1234567", MDR401))

    }
  }
}
