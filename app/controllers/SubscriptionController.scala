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
import models.error.{ReadSubscriptionError, UpdateSubscriptionError}
import models.subscription.RequestDetailForUpdate
import play.api.Logging
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.subscription.SubscriptionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  subscriptionService: SubscriptionService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def readSubscription(): Action[AnyContent] = authenticate.async { implicit request =>
    subscriptionService.getContactInformation(request.subscriptionId).map {
      case Right(value)                       => Ok(Json.toJson(value))
      case Left(ReadSubscriptionError(value)) =>
        logger.warn(s"ReadSubscriptionError $value")
        InternalServerError(s"ReadSubscriptionError $value")
    }
  }

  def updateSubscription(): Action[JsValue] = authenticate.async(parse.json) { implicit request =>
    val updateSubscriptionResult: JsResult[RequestDetailForUpdate] =
      request.body.validate[RequestDetailForUpdate]

    updateSubscriptionResult.fold(
      invalid =>
        Future.successful {
          logger.warn(s" updateSubscription Json Validation Failed: $invalid")
          InternalServerError("Json Validation Failed")
        },
      validReq =>
        subscriptionService.updateSubscription(validReq).map {
          case Right(_)                             => Ok
          case Left(UpdateSubscriptionError(value)) =>
            logger.warn(s"UpdateSubscriptionError $value")
            InternalServerError(s"UpdateSubscriptionError $value")
        }
    )

  }

}
