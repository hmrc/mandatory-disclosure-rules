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

import config.AppConfig
import models.submission.ConversationId
import play.api.libs.ws.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URI
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class SubmissionConnector @Inject() (val config: AppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {

  def submitDisclosure(submission: NodeSeq, conversationId: ConversationId)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val serviceName  = "submission"
    val extraHeaders = Seq()
      .withBearerToken(s"${config.bearerToken(serviceName)}")
      .withXForwardedHost()
      .withDate()
      .withXCorrelationId()
      .withXConversationId(Some(conversationId.value))
      .withContentType(Some("application/xml"))
      .withAccept(Some("application/xml"))
      .withEnvironment(Some(config.environment(serviceName)))

    http
      .post(new URI(config.serviceUrl(serviceName)).toURL)
      .setHeader(extraHeaders: _*)
      .withBody(submission.mkString)
      .execute[HttpResponse]
  }
}
