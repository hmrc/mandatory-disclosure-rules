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
import play.api.mvc.ControllerComponents
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDESCallbackController @Inject() (
  //authenticate: IdentifierAuthAction, //ToDo tax-frauds does not authenticate can we use it?
  fileDetailsRepository: FileDetailsRepository,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def callback = Action.async(parse.json) { request =>
    request.body
      .validate[NotificationCallback]
      .fold(
        invalid = _ => Future.successful(InternalServerError),
        valid = callBackNotification => {
          logger.info(
            s"Received SDES callback for file: ${callBackNotification.filename}, with correlationId : ${callBackNotification.correlationID} and status : ${callBackNotification.notification}"
          )
          callBackNotification.notification match {
            case FileReady =>
              logger.info(s"Processing FileReady received: ${callBackNotification.correlationID}")
              Future.successful(Ok) //Leave fileDetailsRepository record state as Pending
            case FileReceived =>
              logger.info(s"Processing FileReceived:  ${callBackNotification.correlationID}")
              Future.successful(Ok) //Leave fileDetailsRepository record state as Pending
            case FileProcessingFailure =>
              logger.warn(s"SDES transfer failed with message ${callBackNotification.failureReason}")
              //ToDo check that we are not overwriting and EIS response
              fileDetailsRepository.findByConversationId(ConversationId(callBackNotification.correlationID)) flatMap {
                case Some(fileDetails) =>
                  if (fileDetails.status == Pending) {
                    if (callBackNotification.failureReason.getOrElse("").matches(".*virus.*")) {
                      fileDetailsRepository.updateStatus(callBackNotification.correlationID, RejectedSDESVirus).map(_ => Ok)
                    } else {
                      fileDetailsRepository.updateStatus(callBackNotification.correlationID, RejectedSDES).map(_ => Ok)
                    }
                  } else {
                    logger.warn("SDESCallbackController: EIS has already responded") //ToDo confirm how we handle this case
                    Future.successful(Ok)
                  }
                case None =>
                  logger.warn(s"Cannot file correlation ID for callback: ${callBackNotification.correlationID}")
                  Future.successful(Ok) //ToDo confirm error handling
              }
            case FileProcessed =>
              logger.info(s"Processing FileProcessed: ${callBackNotification.correlationID} awaiting EIS response")
              Future.successful(Ok) // Leave fileDetailsRepository record state as Pending
          }
        }
      )

  }
}
