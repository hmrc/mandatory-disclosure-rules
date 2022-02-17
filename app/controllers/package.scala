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

import models.error.ErrorDetails
import models.submission.ConversationId
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HttpResponse

import scala.util.{Success, Try}

package object controllers {

  implicit class HttpResponseExt(httpResponse: HttpResponse) {
    def handleResponse(conversationId: ConversationId)(implicit logger: Logger): Result =
      httpResponse.status match {
        case OK        => Ok(Json.toJson(conversationId))
        case NOT_FOUND => NotFound(httpResponse.body)
        case BAD_REQUEST =>
          logDownStreamError(httpResponse.body)
          BadRequest(httpResponse.body)
        case FORBIDDEN =>
          logDownStreamError(httpResponse.body)
          Forbidden(httpResponse.body)
        case METHOD_NOT_ALLOWED =>
          logDownStreamError(httpResponse.body)
          MethodNotAllowed(httpResponse.body)
        case CONFLICT =>
          logDownStreamError(httpResponse.body)
          Conflict(httpResponse.body)
        case INTERNAL_SERVER_ERROR =>
          logDownStreamError(httpResponse.body)
          InternalServerError(httpResponse.body)
        case _ =>
          logDownStreamError(httpResponse.body)
          ServiceUnavailable(httpResponse.body)
      }

    private def logDownStreamError(body: String)(implicit logger: Logger): Unit = {
      val error = Try(Json.parse(body).validate[ErrorDetails])
      error match {
        case Success(JsSuccess(value, _)) =>
          logger.error(s"Error with submission: ${value.errorDetail.sourceFaultDetail.map(_.detail.mkString)}")
        case _ => logger.error("Error with submission but return is not a valid json")
      }
    }
  }
}
