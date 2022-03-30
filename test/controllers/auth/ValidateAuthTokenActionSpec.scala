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

import base.SpecBase
import config.AppConfig
import play.api.http.Status.{FORBIDDEN, OK}
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidateAuthTokenActionSpec extends SpecBase {

  private val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  private lazy val action          = new ValidateAuthTokenActionImpl(appConfig)

  private val response: Request[AnyContent] => Future[Result] = { _ =>
    Future.successful(Ok(HtmlFormat.empty))
  }

  "ValidateAuthTokenActionSpec" - {

    "must return Ok when the request auth token is same as the config auth token" in {
      val headers: FakeHeaders = FakeHeaders(Seq(HeaderNames.authorisation -> s"Bearer token"))
      val request              = FakeRequest("", "").withHeaders(headers)

      val testAction: Future[Result] = action.invokeBlock(request, response)
      status(testAction) mustBe OK
    }

    "must return FORBIDDEN when the request auth token and config auth token are different" in {

      val request = FakeRequest("", "").withHeaders(FakeHeaders(Seq(HeaderNames.authorisation -> s"Bearer wrong")))

      val testAction: Future[Result] = action.invokeBlock(request, response)
      status(testAction) mustBe FORBIDDEN
    }
  }
}
