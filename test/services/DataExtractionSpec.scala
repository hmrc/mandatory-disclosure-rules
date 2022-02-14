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

package services

import base.SpecBase
import models.submission.{MDR401, MessageSpecData}
import services.validation.XMLValidationService

import scala.xml.XML

class DataExtractionSpec extends SpecBase {

  "DataExtraction" - {
    "messageSpecData must return messageSpec data from a valid XML file" in {
      val service         = app.injector.instanceOf[XMLValidationService]
      val extractor       = app.injector.instanceOf[DataExtraction]
      val validSubmission = XML.loadFile("test/resources/mdr/validmdr.xml")

      val xml = service.validateXML(None, Some(validSubmission)).right.get

      extractor.messageSpecData(xml) mustBe Some(MessageSpecData("GBXAMDR1234567", MDR401))

    }
  }
}
