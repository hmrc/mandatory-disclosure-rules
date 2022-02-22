/*
 * Copyright 2022 HM Revenue & Customs
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
import models.submission.{ConversationId, DisplayFileDetails}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FileDetailsController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  fileDetailsRepository: FileDetailsRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getFileDetails(conversationId: ConversationId): Action[AnyContent] = authenticate.async { implicit request =>
    fileDetailsRepository.findByConversationId(conversationId) map {
      case Some(fileDetails) => Ok(Json.toJson(DisplayFileDetails.build(fileDetails)))
      case _ =>
        logger.info(s"No record found for the conversationId: ${conversationId.value}")
        NotFound
    }
  }

  def getAllFileDetails: Action[AnyContent] = authenticate.async { implicit request =>
    fileDetailsRepository.findBySubscriptionId(request.subscriptionId).map {
      case Nil     => NotFound
      case details => Ok(Json.toJson(details.map(DisplayFileDetails.build)))
    }
  }

}
