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
import handlers.XmlHandler
import models.submissions.SubmissionDetails
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import services.submission.SubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  submissionService: SubmissionService,
  xmlHandler: XmlHandler
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def submitDisclosure: Action[JsValue] = authenticate.async(parse.json) { implicit request =>
    request.body
      .validate[SubmissionDetails]
      .fold(
        invalid = _ => Future.successful(InternalServerError),
        valid = submission => {
          val xml = xmlHandler.load(submission.documentUrl)
          submissionService.processSubmission(xml, submission.enrolmentId, submission.fileName, submission.fileSize)
          //ToDo sdes if file to big
        }
      )
  }

  def submitSDESDisclosure: Action[JsValue] = authenticate.async(parse.json) { implicit request =>
    request.body
      .validate[SubmissionDetails]
      .fold(
        invalid = _ => Future.successful(InternalServerError),
        valid = submission =>
          //ToDo Create File Transfer Request and send url to service
          ???
      )
  }
}
