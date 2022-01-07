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
import models.error.ReadSubscriptionError
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import services.submission.ReadSubscriptionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubscriptionController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  readSubscriptionService: ReadSubscriptionService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def readSubscription() = authenticate.async { implicit request =>
    readSubscriptionService.getContactInformation(request.enrolmentID).map {
      case Right(value) => Ok(Json.toJson(value))
      case Left(ReadSubscriptionError(value)) =>
        logger.warn(s"ReadSubscriptionError $value")
        InternalServerError
    }

  }

}
