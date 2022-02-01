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

import config.AppConfig
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class SubmissionConnector @Inject() (
  val config: AppConfig,
  http: HttpClient
)(implicit ec: ExecutionContext)
    extends Logging {

  def submitDisclosure(submission: NodeSeq)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val serviceName = "submission"
    val extraHeaders = Seq()
      .withBearerToken(s"${config.bearerToken(serviceName)}")
      .withXForwardedHost()
      .withDate()
      .withXCorrelationId()
      .withXConversationId()
      .withContentType(Some("application/xml"))
      .withAccept(Some("application/xml"))
      .withEnvironment(Some(config.environment(serviceName)))
    logger.info(s"ExtraHeaders size = ${extraHeaders.size}")
    http.POSTString[HttpResponse](config.serviceUrl(serviceName), submission.mkString, extraHeaders)(implicitly, hc, ec)
  }

}
