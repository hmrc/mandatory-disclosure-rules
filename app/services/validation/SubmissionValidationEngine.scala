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
import play.api.Logging

import java.net.ConnectException
import javax.inject.Inject
import scala.concurrent.Future

class SubmissionValidationEngine @Inject() (xmlValidationService: XMLValidationService, xmlErrorMessageHelper: XmlErrorMessageHelper) extends Logging {

  def validateUploadSubmission(upScanUrl: Option[String]): Future[Option[UploadSubmissionValidationResult]] = {

    val xmlUrl = upScanUrl.fold(throw new Exception("Unable to retrieve XML from Upscan URL"))(xmlLocation => xmlLocation)

    try performXmlValidation(xmlUrl) match {
      case None =>
        Future.successful(Some(UploadSubmissionValidationSuccess(true)))
      case Some(errors) =>
        Future.successful(Some(UploadSubmissionValidationFailure(ValidationErrors(errors))))
    } catch {
      case e: ConnectException =>
        logger.warn(s"XML parsing failed. The XML parser has thrown the exception: $e")
        Future.successful(None)
      case e: Exception =>
        logger.warn(s"XML parsing failed. The XML parser has thrown the exception: $e")
        Future.successful(Some(UploadSubmissionValidationInvalid()))
    }
  }

  def performXmlValidation(xmlURL: String): Option[Seq[GenericError]] = {
    val xmlErrors = xmlValidationService.validateXML(xmlURL)
    if (xmlErrors.isEmpty) None else Some(xmlErrorMessageHelper.generateErrorMessages(xmlErrors))
  }
}
