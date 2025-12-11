/*
 * Copyright 2023 HM Revenue & Customs
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

import config.AppConfig
import helpers.XmlErrorMessageHelper
import models.submission.MessageSpecData
import models.validation._
import org.xml.sax.SAXParseException
import play.api.Logging
import services.DataExtraction

import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.xml.Elem

class UploadedXmlValidationEngine @Inject() (xmlValidationService: XMLValidationService,
                                             xmlErrorMessageHelper: XmlErrorMessageHelper,
                                             dataExtraction: DataExtraction,
                                             appConfig: AppConfig
) extends Logging {

  def validateUploadSubmission(upScanUrl: String): Future[SubmissionValidationResult] =
    try
      performXmlValidation(upScanUrl) match {
        case Right(messageSpecData) =>
          messageSpecData match {
            case Some(msd) => Future.successful(SubmissionValidationSuccess(msd))
            case None      =>
              val errorMessage = "Could not retrieve messageSpec information from the submission"
              logger.warn(errorMessage)
              Future.successful(InvalidXmlError(errorMessage))
          }

        case Left(errors) =>
          Future.successful(SubmissionValidationFailure(ValidationErrors(errors)))
      }
    catch {
      case e: SAXParseException =>
        logger.warn(s"XML parsing failed. The XML parser has thrown the exception: $e")
        Future.successful(InvalidXmlError(e.getMessage))
    }

  def performXmlValidation(upScanUrl: String): Either[List[GenericError], Option[MessageSpecData]] = {
    val xmlOrErrors: Either[ListBuffer[SaxParseError], Elem] = xmlValidationService.validate(upScanUrl, appConfig.fileUploadXSDFilePath)
    xmlOrErrors match {
      case Right(xml) => Right(dataExtraction.messageSpecData(xml))
      case Left(list) => Left(xmlErrorMessageHelper.generateErrorMessages(list))
    }
  }
}
