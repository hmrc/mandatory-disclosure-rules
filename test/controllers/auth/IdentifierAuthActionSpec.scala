/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.util.Timeout
import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, InjectedController}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class IdentifierAuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  implicit private class HelperOps[A](a: A) {
    def ~[B](b: B) = new ~(a, b)
  }

  class Harness(authAction: IdentifierAuthAction) extends InjectedController {

    def onPageLoad(): Action[AnyContent] = authAction { _ =>
      Ok
    }
  }

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockAppConfig: AppConfig         = mock[AppConfig]

  implicit val timeout: Timeout = 5 seconds

  val application: Application = new GuiceApplicationBuilder()
    .configure(Configuration("metrics.enabled" -> "false"))
    .overrides(
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[AppConfig].toInstance(mockAppConfig)
    )
    .build()

  "Identifier Auth Action" when {
    "the user is not logged in" must {
      "must return unauthorised" in {
        when(mockAuthConnector.authorise(any(), any())(any(), any()))
          .thenReturn(Future.failed(new MissingBearerToken))
        when(mockAppConfig.enrolmentKey)
          .thenReturn(_ => "HMRC-MDR-ORG")
        when(mockAppConfig.enrolmentId)
          .thenReturn(_ => "MDRID")
        val authAction = application.injector.instanceOf[IdentifierAuthAction]
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe UNAUTHORIZED

      }
      "must return IllegalAccessException for not known enrolment" in {
        val retrieval = Enrolments(Set(Enrolment("HMRC-TEST-ORG", Seq(EnrolmentIdentifier("TESTID", "subscriptionID")), "ACTIVE")))
        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(retrieval))

        when(mockAppConfig.enrolmentKey)
          .thenReturn(_ => "HMRC-MDR-ORG")
        when(mockAppConfig.enrolmentId)
          .thenReturn(_ => "MDRID")
        val authAction = application.injector.instanceOf[IdentifierAuthAction]
        val controller = new Harness(authAction)
        intercept[IllegalAccessException] {
          val result = controller.onPageLoad()(FakeRequest("", ""))
          status(result)
        }
      }
    }

    "the user is logged in" must {
      "must return the request" in {
        val retrieval = Enrolments(Set(Enrolment("HMRC-MDR-ORG", Seq(EnrolmentIdentifier("MDRID", "subscriptionID")), "ACTIVE")))
        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(retrieval))
        when(mockAppConfig.enrolmentKey)
          .thenReturn(_ => "HMRC-MDR-ORG")
        when(mockAppConfig.enrolmentId)
          .thenReturn(_ => "MDRID")

        val authAction = application.injector.instanceOf[IdentifierAuthAction]
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) mustBe OK
      }
    }
  }
}
