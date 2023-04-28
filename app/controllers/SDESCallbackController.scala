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
import models.sdes._
import play.api.Logging
import play.api.mvc.{Action, ControllerComponents}
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDESCallbackController @Inject() (
  authenticate: IdentifierAuthAction, //tax-frauds does not authenticate so might not be able to use this
  fileDetailsRepository: FileDetailsRepository,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def callback: Action[CallBackNotification] = Action.async(parse.json[CallBackNotification]) { request =>
    val CallBackNotification(status, filename, correlationID, failureReason) = request.body
    logger.info(s"Received SDES callback for file: $filename, with correlationId : $correlationID and status : $status")
    status match {
      case FileReady => ???
      case FileReceived => ???
      case  FileProcessingFailure => Future.successful(Ok)
      case FileProcessed =>
        Future.successful(Ok)
    }

  }
}
