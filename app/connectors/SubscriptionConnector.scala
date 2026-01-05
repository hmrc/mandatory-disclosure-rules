/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import com.google.inject.Inject
import config.AppConfig
import models.subscription.{DisplaySubscriptionForMDRRequest, UpdateSubscriptionForMDRRequest}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URI
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (
  val config: AppConfig,
  val http: HttpClientV2
) {

  def readSubscriptionInformation(
    subscription: DisplaySubscriptionForMDRRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "read-subscription"

    val extraHeaders = Seq()
      .withBearerToken(s"${config.bearerToken(serviceName)}")
      .withXForwardedHost()
      .withDate()
      .withXCorrelationId()
      .withXConversationId()
      .withContentType(Some("application/json"))
      .withAccept(Some("application/json"))
      .withEnvironment(Some(config.environment(serviceName)))

    http
      .post(new URI(config.serviceUrl(serviceName)).toURL)
      .withBody(Json.toJson(subscription))
      .setHeader(extraHeaders: _*)
      .execute[HttpResponse]
  }

  def updateSubscription(
    updateSubscription: UpdateSubscriptionForMDRRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val serviceName  = "update-subscription"
    val extraHeaders = Seq()
      .withBearerToken(s"${config.bearerToken(serviceName)}")
      .withXForwardedHost()
      .withDate()
      .withXCorrelationId()
      .withXConversationId()
      .withContentType(Some("application/json"))
      .withAccept(Some("application/json"))
      .withEnvironment(Some(config.environment(serviceName)))

    http
      .post(new URI(config.serviceUrl(serviceName)).toURL)
      .withBody(Json.toJson(updateSubscription))
      .setHeader(extraHeaders: _*)
      .execute[HttpResponse]
  }
}
