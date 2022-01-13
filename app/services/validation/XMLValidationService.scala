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

import models.validation.SaxParseError
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

import java.io.StringReader
import java.net.URL
import javax.inject.Inject
import javax.xml.parsers.{SAXParser, SAXParserFactory}
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import scala.collection.mutable.ListBuffer
import scala.xml.factory.XMLLoader
import scala.xml.{Elem, NodeSeq}

class XMLValidationService @Inject() (xmlValidatingParser: SaxParser) {

  def validateXML(upScanUrl: Option[String] = None, xml: Option[NodeSeq] = None): Either[ListBuffer[SaxParseError], Elem] = {

    val list: ListBuffer[SaxParseError] = new ListBuffer[SaxParseError]

    trait AccumulatorState extends DefaultHandler {
      override def warning(e: SAXParseException): Unit    = list += SaxParseError(e.getLineNumber, e.getMessage)
      override def error(e: SAXParseException): Unit      = list += SaxParseError(e.getLineNumber, e.getMessage)
      override def fatalError(e: SAXParseException): Unit = list += SaxParseError(e.getLineNumber, e.getMessage)
    }

    val loader: XMLLoader[Elem] = new scala.xml.factory.XMLLoader[scala.xml.Elem] {
      override def parser: SAXParser = xmlValidatingParser.validatingParser
      override def adapter =
        new scala.xml.parsing.NoBindingFactoryAdapter with AccumulatorState
    }

    val loadedXML = if (xml.isDefined) {
      loader.load(new StringReader(xml.mkString))
    } else {
      loader.load(new URL(upScanUrl.get))
    }

    if (list.isEmpty) Right(loadedXML) else Left(list)
  }
}

trait SaxParser {
  def validatingParser: SAXParser
}

class MDRSchemaValidatingParser extends SaxParser {

  val schemaLang: String                = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
  val isoXsdUrl: URL                    = getClass.getResource("/schemas/mdr/IsoMdrTypes_v1.0.xsd")
  val ukMDRXsdUrl: URL                  = getClass.getResource("/schemas/mdr/MdrXML_v1.0.xsd")
  val mdrTypesUrl: URL                  = getClass.getResource("/schemas/mdr/OecdMdrTypes_v1.0.xsd")
  val ukDCT06XsdUrl: URL                = getClass.getResource("/schemas/mdr/DCT06_EIS_UK_schema.xsd")
  val isoXsdStream: StreamSource        = new StreamSource(isoXsdUrl.openStream())
  val ukMDRXsdStream: StreamSource      = new StreamSource(ukMDRXsdUrl.openStream())
  val ukMDRTypesXsdStream: StreamSource = new StreamSource(mdrTypesUrl.openStream())
  val ukDCT06XsdStream: StreamSource    = new StreamSource(ukDCT06XsdUrl.openStream())

  //IsoTypes xsd is referenced by UKDac6XSD so must come first in the array
  val streams: Array[Source] = Array(isoXsdStream, ukMDRTypesXsdStream, ukMDRXsdStream)

  val schema: Schema = SchemaFactory.newInstance(schemaLang).newSchema(streams)

  val factory: SAXParserFactory = SAXParserFactory.newInstance()
  factory.setNamespaceAware(true)
  factory.setSchema(schema)

  override def validatingParser: SAXParser = factory.newSAXParser()
}
