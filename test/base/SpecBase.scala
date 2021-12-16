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

package base

import org.mockito.MockitoSugar
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with GuiceOneAppPerSuite
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  def injector: Injector = app.injector

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(Configuration("metrics.enabled" -> "false", "enrolmentKeys.mdr.key" -> "HMRC-MDR-ORG", "enrolmentKeys.mdr.identifier" -> "MDRID"))
      .overrides()
}
