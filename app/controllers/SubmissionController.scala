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

import connectors.SubmissionConnector
import controllers.auth.IdentifierAuthAction
import models.error.ReadSubscriptionError
import models.submission.{ConversationId, Pending, SubmissionDetails, SubmissionMetaData}
import play.api.{Logger, Logging}
import play.api.mvc.{Action, ControllerComponents}
import repositories.submission.SubmissionRepository
import services.submission.TransformService
import services.subscription.SubscriptionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class SubmissionController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  transformService: TransformService,
  readSubscriptionService: SubscriptionService,
  submissionConnector: SubmissionConnector,
  submissionRepository: SubmissionRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def submitDisclosure: Action[NodeSeq] = authenticate.async(parse.xml) { implicit request =>
    //TODO receive xml and read details not sure what I need
    val xml                  = request.body
    val fileName             = (xml \ "fileName").text
    val subscriptionId       = request.subscriptionId
    val submissionTime       = LocalDateTime.now()
    val conversationId       = ConversationId()
    val messageRefId         = (xml \\ "MessageRefId").text
    val submissionDetails    = SubmissionDetails(conversationId, subscriptionId, messageRefId, Pending, fileName, submissionTime, submissionTime)
    implicit val log: Logger = logger
    submissionRepository.insert(submissionDetails).flatMap { _ =>
      val submissionMetaData = SubmissionMetaData.build(submissionTime, conversationId.value, fileName)
      readSubscriptionService.getContactInformation(subscriptionId).flatMap {
        case Right(value) =>
          // Add metadata
          val submission: NodeSeq = transformService.addSubscriptionDetailsToSubmission(xml, value, submissionMetaData)
          //TODO validate XML
          //Submit disclosure
          submissionConnector.submitDisclosure(submission).map(_.handleResponse(conversationId))
        case Left(ReadSubscriptionError(value)) =>
          logger.warn(s"ReadSubscriptionError $value")
          Future.successful(InternalServerError)
      }
    }

  }

}
