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

import connectors.{stripSession, SubmissionConnector}
import controllers.auth.IdentifierAuthAction
import models.error.ReadSubscriptionError
import models.submission.SubmissionMetaData
import models.subscription.ResponseDetail
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import services.submission.{ReadSubscriptionService, TransformService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class SubmissionController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  transformService: TransformService,
  readSubscriptionService: ReadSubscriptionService,
  submissionConnector: SubmissionConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def submitDisclosure: Action[NodeSeq] = authenticate.async(parse.xml) { implicit request =>
    //TODO receive xml and read details not sure what I need
    val xml                    = request.body
    val fileName               = (xml \ "fileName").text
    val enrolmentID            = request.enrolmentID
    val submissionTime         = LocalDateTime.now()
    val conversationID: String = stripSession(hc.sessionId.map(_.value).getOrElse(UUID.randomUUID().toString))

    val submissionMetaData = SubmissionMetaData.build(submissionTime, conversationID, fileName)
    readSubscriptionService.getContactInformation(enrolmentID).flatMap {
      case Right(value) =>
        // Add metadata
        val submission: NodeSeq = transformService.addSubscriptionDetailsToSubmission(xml, value.as[ResponseDetail], submissionMetaData)
        //TODO validate XML
        //Submit disclosure
        submissionConnector.submitDisclosure(submission).map(_.handleResponse(logger))
      case Left(ReadSubscriptionError(value)) =>
        logger.warn(s"ReadSubscriptionError $value")
        Future.successful(InternalServerError)
    }

  }

}
