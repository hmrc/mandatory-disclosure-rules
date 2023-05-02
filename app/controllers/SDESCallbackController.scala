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

import controllers.auth.IdentifierAuthAction
import models.sdes.NotificationType.{FileProcessed, FileProcessingFailure, FileReady, FileReceived}
import models.sdes._
import models.submission.TransferFailure
import play.api.Logging
import play.api.mvc.ControllerComponents
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDESCallbackController @Inject() (
  //authenticate: IdentifierAuthAction, //tax-frauds does not authenticate so might not be able to use this
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
              logger.info(s"Processing FileReady") //ToDo update logging
              Future.successful(Ok)
            case FileReceived =>
              logger.info(s"Processing FileReceived") //ToDo update logging
              Future.successful(Ok)
            case FileProcessingFailure =>
              logger.warn(s"SDES transfer failed with message ${callBackNotification.failureReason}")
              fileDetailsRepository.updateStatus(callBackNotification.correlationID, TransferFailure).map(_ => Ok)
            case FileProcessed =>
              logger.info(s"Processing FileProcessed") //ToDo update logging
              Future.successful(Ok)
          }
        }
      )

  }
}
