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

import models.sdes.NotificationType.{FileProcessed, FileProcessingFailure, FileReady, FileReceived}
import models.sdes._
import models.submission.{ConversationId, Pending, RejectedSDES, RejectedSDESVirus}
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDESCallbackController @Inject() (
  fileDetailsRepository: FileDetailsRepository,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def callback: Action[JsValue] = Action.async(parse.json) { request =>
    request.body
      .validate[NotificationCallback]
      .fold(
        invalid = _ => {
          // This should never happen. Will trigger a Pagerduty alert.
          logger.warn(s"SDESCallbackController: Unexpected SDES response - Invalid JSON payload")
          Future.successful(InternalServerError)
        },
        valid = callBackNotification => {
          logger.info(
            s"SDESCallbackController: Received ${callBackNotification.notification} callback for file: ${callBackNotification.filename}, with correlationId: ${callBackNotification.correlationID}"
          )
          callBackNotification.notification match {
            case FileReady | FileReceived | FileProcessed =>
              // Leave file as Pending until we get a response from CADX.
              Future.successful(Ok)
            case FileProcessingFailure =>
              // File has failed to reach CADX, update the file status so the user can see the transfer has failed.
              val failureReason = callBackNotification.failureReason.getOrElse("")

              logger.warn(s"SDESCallbackController: File transfer failed with reason: $failureReason")
              fileDetailsRepository.findByConversationId(ConversationId(callBackNotification.correlationID)) flatMap {
                case Some(fileDetails) if fileDetails.status == Pending =>
                  val newStatus = if (failureReason.matches(".*virus.*")) RejectedSDESVirus else RejectedSDES

                  fileDetailsRepository.updateStatus(callBackNotification.correlationID, newStatus).map(_ => Ok)
                case Some(_) =>
                  // SDES has told us the file has failed to reach CADX, but we've already had a response from CADX.
                  // This should never happen and will trigger a Pagerduty alert.
                  logger.warn(
                    s"SDESCallbackController: Unexpected SDES response - EIS has already responded for correlation ID: ${callBackNotification.correlationID}"
                  )
                  Future.successful(Ok)
                case None =>
                  // We've got a status update for a file we have no record of. This should only be possible if the SDES
                  // callback is incorrect, or if there has been a > 28 day delay between the file being submitted and
                  // us receiving this callback. This also shouldn't happen and will trigger a Pagerduty alert.
                  logger.warn(s"SDESCallbackController: Unexpected SDES response - Cannot find file with correlation ID: ${callBackNotification.correlationID}")
                  Future.successful(Ok)
              }
          }
        }
      )

  }
}
