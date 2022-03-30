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
import controllers.auth.ValidateAuthTokenAction
import models.submission.{Accepted => FileStatusAccepted, FileStatus, Rejected}
import models.xml.{BREResponse, ValidationStatus}
import play.api.Logging
import play.api.mvc.{Action, ControllerComponents}
import repositories.submission.FileDetailsRepository
import services.EmailService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.CustomAlertUtil
import utils.DateTimeFormatUtil.dateFormatted

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

class EISResponseController @Inject() (cc: ControllerComponents,
                                       validateAuth: ValidateAuthTokenAction,
                                       actionRefiner: EISResponsePreConditionCheckActionRefiner,
                                       fileDetailsRepository: FileDetailsRepository,
                                       emailService: EmailService,
                                       customAlertUtil: CustomAlertUtil
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private def convertToFileStatus(breResponse: BREResponse): FileStatus =
    breResponse.genericStatusMessage.status match {
      case ValidationStatus.accepted => FileStatusAccepted
      case ValidationStatus.rejected =>
        customAlertUtil.alertForProblemStatus(breResponse.genericStatusMessage.validationErrors)
        Rejected(breResponse.genericStatusMessage.validationErrors)
    }

  def processEISResponse(): Action[NodeSeq] = (Action(parse.xml) andThen validateAuth andThen actionRefiner).async { implicit request =>
    val conversationId = request.BREResponse.conversationID
    val fileStatus     = convertToFileStatus(request.BREResponse)

    fileDetailsRepository.updateStatus(conversationId, fileStatus) map {
      case Some(updatedFileDetails) =>
        val fastJourney = updatedFileDetails.lastUpdated.isBefore(updatedFileDetails.submitted.plusSeconds(10))

        (fastJourney, updatedFileDetails.status) match {
          case (_, FileStatusAccepted) | (false, Rejected(_)) =>
            emailService.sendAndLogEmail(
              updatedFileDetails.subscriptionId,
              dateFormatted(updatedFileDetails.submitted),
              updatedFileDetails.messageRefId,
              updatedFileDetails.status == FileStatusAccepted
            )
          case Rejected(error) =>
            customAlertUtil.alertForProblemStatus(error)
            logger.warn("Upload file status is rejected on fast journey. No email has been sent")
        }
        NoContent
      case _ =>
        logger.warn("Failed to update the status:mongo error")
        InternalServerError
    }
  }
}
