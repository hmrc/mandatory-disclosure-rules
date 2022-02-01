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

package connectors

import com.google.inject.Inject
import config.AppConfig
import models.subscription.{DisplaySubscriptionForMDRRequest, UpdateSubscriptionForMDRRequest}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (
  val config: AppConfig,
  val http: HttpClient
) extends Logging {

  def readSubscriptionInformation(
    subscription: DisplaySubscriptionForMDRRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "read-subscription"

    val extraHeaders = Seq()
      .withBearerToken(s"${config.bearerToken}")
      .withXForwardedHost()
      .withDate()
      .withXCorrelationId()
      .withXConversationId()
      .withContentType(Some("application/json"))
      .withAccept(Some("application/json"))
      .withEnvironment(Some(config.environment(serviceName)))
    logger.info(s"ExtraHeaders size = ${extraHeaders.size}")
    logger.debug(s"ExtraHeaders = $extraHeaders")

    http.POST[DisplaySubscriptionForMDRRequest, HttpResponse](
      config.serviceUrl(serviceName),
      subscription,
      headers = extraHeaders
    )(
      wts = DisplaySubscriptionForMDRRequest.format,
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }

  def updateSubscription(
    updateSubscription: UpdateSubscriptionForMDRRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    //TODO Not sure why above conversationId is not applicable here as per DAC6 comment

    val serviceName = "update-subscription"
    val extraHeaders = Seq()
      .withBearerToken(s"${config.bearerToken}")
      .withXForwardedHost()
      .withDate()
      .withXCorrelationId()
      .withXConversationId()
      .withContentType(Some("application/json"))
      .withAccept(Some("application/json"))
      .withEnvironment(Some(config.environment(serviceName)))
    logger.info(s"ExtraHeaders size = ${extraHeaders.size}")
    logger.debug(s"ExtraHeaders = $extraHeaders")

    http.POST[UpdateSubscriptionForMDRRequest, HttpResponse](config.serviceUrl(serviceName), updateSubscription, extraHeaders)(
      wts = UpdateSubscriptionForMDRRequest.format,
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }
}
