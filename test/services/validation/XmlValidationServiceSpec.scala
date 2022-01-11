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
import play.api.Application
import play.api.inject.bind

import java.net.URL
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import scala.collection.mutable.ListBuffer
import scala.xml.XML

class XmlValidationServiceSpec extends SpecBase {
  val noErrors: ListBuffer[SaxParseError] = ListBuffer()

  val application: Application = applicationBuilder()
    .overrides(
      bind[SaxParser].toInstance(mock[SaxParser])
    )
    .build()

  trait ActualSetup {

    //val testUrl: URL = getClass.getResource("/sitemap-v0.9.xsd")

    val schemaLang: String = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
    val isoXsdUrl: URL     = getClass.getResource("/schemas/dac6/IsoTypes_v1.01.xsd")
    val xsdUrl: URL        = getClass.getResource("/schemas/dac6/UKDac6XSD_v0.5.xsd")

    val isoXsdStream: StreamSource    = new StreamSource(isoXsdUrl.openStream())
    val ukDAC6XsdStream: StreamSource = new StreamSource(xsdUrl.openStream())

    val streams: Array[javax.xml.transform.Source] = Array(isoXsdStream, ukDAC6XsdStream)

    val schema: Schema = SchemaFactory.newInstance(schemaLang).newSchema(streams)

    val factory: SAXParserFactory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    factory.setSchema(schema)

    lazy val sut: XMLValidationService = {
      when(application.injector.instanceOf[SaxParser].validatingParser)
        .thenReturn(factory.newSAXParser())

      application.injector.instanceOf[XMLValidationService]
    }
  }

  "Validation Service" - {
    "must pass back errors if a file is invalid" in {
      val service = app.injector.instanceOf[XMLValidationService]

      val invalid = <this>
      <will>not validate</will>
      </this>

      val result = service.validateXML(None, Some(invalid))

      result.isLeft mustBe true
    }

    "must correctly invalidate a submission with a data problem" in {
      val service = app.injector.instanceOf[XMLValidationService]

      val validSubmission = XML.loadFile("test/resources/invalid.xml")

      val result = service.validateXML(None, Some(validSubmission))

      result.isLeft mustBe true
    }

    "must correctly validate a submission" in {
      val service = app.injector.instanceOf[XMLValidationService]

      val validSubmission = XML.loadFile("test/resources/valid.xml")

      val result = service.validateXML(None, Some(validSubmission))

      result.isLeft mustBe false
    }
  }
}
