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

package services.validation

import base.SpecBase
import models.validation.SaxParseError
import play.api.{Application, Configuration}
import play.api.inject.bind

import java.net.URL
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import scala.collection.mutable.ListBuffer
import javax.xml.transform.Source
import scala.xml.XML

class XmlValidationServiceMDRSpec extends SpecBase {
  val noErrors: ListBuffer[SaxParseError] = ListBuffer()

  val application: Application = applicationBuilder()
    .configure(Configuration("xmlparser.useMDR" -> "true"))
    .build()

  "Validation Service" - {
    "must pass back errors if a file is invalid" in {
      val service = application.injector.instanceOf[XMLValidationService]

      val invalid = <this>
      <will>not validate</will>
      </this>

      val result = service.validateXML(None, Some(invalid))

      result.isLeft mustBe true
    }

    "must correctly invalidate a submission with a data problem" in {
      val service = application.injector.instanceOf[XMLValidationService]

      val validSubmission = XML.loadFile("test/resources/mdr/invalid.xml")

      val result = service.validateXML(None, Some(validSubmission))

      result.isLeft mustBe true
    }

    "must correctly validate a submission" in {
      val service = application.injector.instanceOf[XMLValidationService]

      val validSubmission = XML.loadFile("test/resources/mdr/validmdr.xml")

      val result = service.validateXML(None, Some(validSubmission))

      result.isLeft mustBe false
    }
  }
}
