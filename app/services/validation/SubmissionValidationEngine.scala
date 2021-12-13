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

import helpers.XmlErrorMessageHelper
import models.validation._
import org.xml.sax.SAXParseException
import play.api.Logging

import java.net.ConnectException
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.xml.Elem

class SubmissionValidationEngine @Inject() (xmlValidationService: XMLValidationService, xmlErrorMessageHelper: XmlErrorMessageHelper) extends Logging {

  def validateUploadSubmission(upScanUrl: Option[String]): Future[Option[SubmissionValidationResult]] =
    try performXmlValidation(upScanUrl) match {
      case None =>
        Future.successful(Some(SubmissionValidationSuccess(true)))
      case Some(errors) =>
        Future.successful(Some(SubmissionValidationFailure(ValidationErrors(errors))))
    } catch {
      case e: SAXParseException =>
        logger.warn(s"XML parsing failed. The XML parser has thrown the exception: $e")
        Future.successful(Some(InvalidXmlError(e.getMessage)))
      case e: ConnectException =>
        logger.warn(s"Connection timed out with exception: $e")
        Future.successful(None)
    }

  def performXmlValidation(upScanUrl: Option[String]): Option[Seq[GenericError]] = {
    val xmlOrErrors: Either[ListBuffer[SaxParseError], Elem] = xmlValidationService.validateXML(upScanUrl)
    xmlOrErrors.fold(list => Some(xmlErrorMessageHelper.generateErrorMessages(list)), _ => None)
  }
}
