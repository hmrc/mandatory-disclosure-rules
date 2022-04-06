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

package controllers.auth

import com.google.inject.ImplementedBy
import config.AppConfig
import play.api.Logging
import play.api.mvc.Results.Forbidden
import play.api.mvc._
import uk.gov.hmrc.http.HeaderNames

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateAuthTokenActionImpl @Inject() (appConfig: AppConfig)(implicit val executionContext: ExecutionContext)
    extends ValidateAuthTokenAction
    with Logging {

  private def validateBearerToken[A](request: Request[A]): Boolean =
    request.headers.get(HeaderNames.authorisation) match {
      case Some(value) => value == s"Bearer ${appConfig.bearerToken("eis-response")}"
      case _           => false
    }

  override def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] =
    if (validateBearerToken(request)) {
      Future.successful(Right(request))
    } else {
      logger.warn("Unexpected auth Bearer token received")
      Future.successful(Left(Forbidden))
    }
}

@ImplementedBy(classOf[ValidateAuthTokenActionImpl])
trait ValidateAuthTokenAction extends ActionRefiner[Request, Request]
