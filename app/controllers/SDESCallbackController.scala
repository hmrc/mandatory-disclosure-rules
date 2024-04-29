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

package controllers

import models.sdes.NotificationType.FileProcessingFailure
import models.sdes._
import models.submission._
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import repositories.submission.FileDetailsRepository
import services.EmailService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.DateTimeFormatUtil

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDESCallbackController @Inject() (
  fileDetailsRepository: FileDetailsRepository,
  emailService: EmailService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def callback: Action[JsValue] = Action.async(parse.json) { request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    request.body
      .validate[NotificationCallback]
      .fold(
        _ => {
          logger.warn(logErrorInvalidJson)
          Future.successful(InternalServerError)
        },
        callback => handleSDESCallback(callback)
      )
  }

  private def handleSDESCallback(callback: NotificationCallback)(implicit hc: HeaderCarrier): Future[Status] = {
    logger.info(logReceived(callback))

    callback.notification match {
      case FileProcessingFailure =>
        logger.warn(logFileProcessingFailure(callback))

        fileDetailsRepository.findByConversationId(ConversationId(callback.correlationID)) flatMap {
          case Some(fileDetails) if fileDetails.status == Pending =>
            val status = if (callback.failureReason.getOrElse("").matches(".*virus.*")) RejectedSDESVirus else RejectedSDES

            fileDetailsRepository
              .updateStatus(callback.correlationID, status)
              .map {
                case Some(updatedFileDetails) =>
                  emailService.sendAndLogEmail(
                    updatedFileDetails.subscriptionId,
                    DateTimeFormatUtil.displayFormattedDate(updatedFileDetails.submitted),
                    updatedFileDetails.messageRefId,
                    isUploadSuccessful = false,
                    ReportType.getMessage(updatedFileDetails.reportType)
                  )
                  Ok
                case None =>
                  logger.warn(logErrorDbNotUpdated(callback))
                  InternalServerError
              }
          case Some(_) =>
            logger.warn(logErrorStatusNotPending(callback))
            Future.successful(Ok)
          case None =>
            logger.warn(logErrorFileNotFound(callback))
            Future.successful(Ok)
        }
      case _ =>
        Future.successful(Ok)
    }
  }

  private val logReceived = (callback: NotificationCallback) =>
    s"SDESCallbackController: Received SDES ${callback.notification} callback for file: ${callback.filename} (${callback.correlationID})"

  private val logFileProcessingFailure = (callback: NotificationCallback) =>
    s"SDESCallbackController: SDES transfer failed with message: ${callback.failureReason} (${callback.correlationID})"

  private val logErrorInvalidJson = "SDESCallbackController: Unexpected error - sdes callback failed json validation"

  private val logErrorDbNotUpdated = (callback: NotificationCallback) =>
    s"SDESCallbackController: Unexpected error - unable to update file status in db (${callback.correlationID})"

  private val logErrorStatusNotPending = (callback: NotificationCallback) =>
    s"SDESCallbackController: Unexpected error - file status is not Pending (${callback.correlationID})"

  private val logErrorFileNotFound = (callback: NotificationCallback) =>
    s"SDESCallbackController: Unexpected error - cannot find file in database (${callback.correlationID})"
}
