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

import models.validation._
import play.api.Logging
import schemas.{DAC6XMLSchema, XMLSchema}
import uk.gov.hmrc.http.HeaderCarrier

import java.net.{ConnectException, URL}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UploadSubmissionValidationEngine @Inject() (xmlValidator: XMLValidator, xmlSchema: XMLSchema) extends Logging {

  def validateUploadSubmission(upScanUrl: Option[String])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[UploadSubmissionValidationResult]] = {

    val xmlUrl = upScanUrl.fold(throw new Exception("Unable to retrieve XML from Upscan URL"))(xmlLocation => xmlLocation)

    try performXmlValidation(xmlUrl) match {
      case Seq() =>
        Future.successful(Some(UploadSubmissionValidationSuccess(true)))
      case errors: Seq[String] =>
        Future.successful(Some(UploadSubmissionValidationFailure(ValidationErrors(errors))))
      case _ =>
        Future.successful(Some(UploadSubmissionValidationInvalid())) //ToDo not yet implemented in this skeleton
    } catch {
      case e: ConnectException =>
        logger.warn(s"XML parsing failed. The XML parser has thrown the exception: $e")
        Future.successful(None)
      case e: Exception =>
        logger.warn(s"XML parsing failed. The XML parser has thrown the exception: $e")
        Future.successful(Some(UploadSubmissionValidationInvalid()))
    }
  }

  def performXmlValidation(xmlUrl: String): Seq[String] = {

    //ToDo update when errors finalised
    val xmlErrors = xmlValidator.validateSchema(new URL(xmlUrl), xmlSchema.xmlValidationSchema)

    if (xmlErrors.hasErrors || xmlErrors.hasFatalErrors || xmlErrors.hasWarnings) {
      xmlErrors.errorsCollection ++ xmlErrors.fatalErrorsCollection ++ xmlErrors.warningsCollection
    } else {
      Nil
    }
  }

}
