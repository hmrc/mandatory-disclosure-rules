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

package controllers.validation

import models.validation.{InvalidXmlError, SubmissionValidationFailure, SubmissionValidationSuccess}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.validation.SubmissionValidationEngine
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmissionValidationController @Inject() (
  cc: ControllerComponents,
  validationEngine: SubmissionValidationEngine
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def validateSubmission: Action[AnyContent] = Action.async { implicit request =>
    try validationEngine.validateUploadSubmission(request.body.asText) map {
      case Some(SubmissionValidationSuccess(_)) =>
        Ok(Json.toJsObject(SubmissionValidationSuccess(true)))

      case Some(SubmissionValidationFailure(errors)) =>
        Ok(Json.toJson(SubmissionValidationFailure(errors)))

      case Some(InvalidXmlError(saxException)) =>
        BadRequest(InvalidXmlError(saxException).toString)

      case None =>
        BadRequest("Service unavailable")
    }
  }
}
