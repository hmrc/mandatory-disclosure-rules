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

import controllers.actions.EISResponsePreConditionCheckActionRefiner
import controllers.auth.AuthAction
import models.submission.{Accepted => FileStatusAccepted, FileStatus, Rejected}
import models.xml.{BREResponse, ValidationStatus}
import play.api.Logging
import play.api.mvc.{Action, ControllerComponents}
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class EISResponseController @Inject() (cc: ControllerComponents,
                                       authAction: AuthAction,
                                       actionRefiner: EISResponsePreConditionCheckActionRefiner,
                                       fileDetailsRepository: FileDetailsRepository
) extends BackendController(cc)
    with Logging {

  private def convertToFileStatus(breResponse: BREResponse): FileStatus =
    breResponse.genericStatusMessage.status match {
      case ValidationStatus.accepted => FileStatusAccepted
      case ValidationStatus.rejected => Rejected(breResponse.genericStatusMessage.validationErrors)
    }

  def processEISResponse(): Action[NodeSeq] = (authAction(parse.xml) andThen actionRefiner).async { implicit request =>
    val conversationId = request.BREResponse.conversationID
    val fileStatus     = convertToFileStatus(request.BREResponse)
    fileDetailsRepository.updateStatus(conversationId, fileStatus)
    Future.successful(Ok)
  }
}
