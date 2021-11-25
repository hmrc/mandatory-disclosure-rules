/*
 * Copyright 2021 HM Revenue & Customs
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

import com.ctc.wstx.exc.WstxException
import com.google.inject.Inject
import config.AppConfig
import org.codehaus.stax2.validation.{ValidationProblemHandler, XMLValidationProblem, XMLValidationSchema}
import org.codehaus.stax2.{XMLInputFactory2, XMLStreamReader2}
import play.api.Logging

import java.net.URL
import javax.xml.stream.XMLInputFactory
import scala.collection.mutable.ListBuffer
import scala.util.control.Exception.nonFatalCatch

class XMLValidator @Inject() (config: AppConfig) extends Logging {

  val xmlInputFactory2: XMLInputFactory2 =
    XMLInputFactory.newInstance.asInstanceOf[XMLInputFactory2]
  xmlInputFactory2.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  xmlInputFactory2.setProperty(
    "javax.xml.stream.isSupportingExternalEntities",
    true
  )
  xmlInputFactory2.setProperty(
    "javax.xml.stream.isNamespaceAware",
    true
  )

  def validateSchema(
    input: URL,
    xmlValidationSchema: XMLValidationSchema
  ): XmlErrorHandler = {
    val xmlErrorHandler = new XmlErrorHandler(config.maxValidationErrors)

    try {
      val xmlStreamReader: XMLStreamReader2 =
        xmlInputFactory2.createXMLStreamReader(input)
      xmlStreamReader.setValidationProblemHandler(xmlErrorHandler)
      xmlStreamReader.validateAgainst(xmlValidationSchema)
      while (xmlStreamReader.hasNext) xmlStreamReader.next
    } catch {
      case e: WstxException =>
        xmlErrorHandler.reportProblem(
          new XMLValidationProblem(
            e.getLocation,
            e.getMessage,
            XMLValidationProblem.SEVERITY_FATAL
          )
        )
      case ErrorLimitExceededException =>
        logger.warn(
          s"Errors exceeding the ${xmlErrorHandler.errorMessageLimit} encountered, validation aborting."
        )
    }

    xmlErrorHandler

  }
}

class XmlErrorHandler(val errorMessageLimit: Int) extends ValidationProblemHandler {

  override def reportProblem(problem: XMLValidationProblem): Unit =
    captureError(problem)

  private val errorsListBuffer: ListBuffer[String]   = new ListBuffer[String]()
  private val warningsListBuffer: ListBuffer[String] = new ListBuffer[String]()
  private val fatalErrorsListBuffer: ListBuffer[String] =
    new ListBuffer[String]()

  def hasErrors: Boolean      = errorsCollection.nonEmpty
  def hasFatalErrors: Boolean = fatalErrorsCollection.nonEmpty
  def hasWarnings: Boolean    = warningsCollection.nonEmpty

  def errorsCollection: List[String]      = errorsListBuffer.toList
  def warningsCollection: List[String]    = warningsListBuffer.toList
  def fatalErrorsCollection: List[String] = fatalErrorsListBuffer.toList

  private def captureError(problem: XMLValidationProblem) = {

    val listBuffer: ListBuffer[String] = problem.getSeverity match {
      case XMLValidationProblem.SEVERITY_WARNING => warningsListBuffer
      case XMLValidationProblem.SEVERITY_ERROR   => errorsListBuffer
      case XMLValidationProblem.SEVERITY_FATAL   => fatalErrorsListBuffer
    }

    if (listBuffer.size < errorMessageLimit) {
      val lineNumber = nonFatalCatch opt problem.getLocation.getLineNumber
      listBuffer += lineNumber.fold(s"${problem.getMessage}")(line => s"${problem.getMessage} on line $line")
    } else {
      fatalErrorsListBuffer += s"Number of errors exceeding limit ($errorMessageLimit), aborting validation.."
      throw ErrorLimitExceededException
    }

    ()
  }

}

case object ErrorLimitExceededException extends Throwable
