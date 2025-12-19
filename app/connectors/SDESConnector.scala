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
import models.sdes.*
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URI
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SDESConnector @Inject() (
  val config: AppConfig,
  val http: HttpClientV2
)() {

  private val extraHeaders: Seq[(String, String)] = Seq("x-client-id" -> config.sdesclientId)

  def fileReady(fileTransferNotification: FileTransferNotification)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, Int]] =
    http
      .post(new URI(config.sdesUrl).toURL)
      .withBody(Json.toJson(fileTransferNotification))
      .setHeader(extraHeaders: _*)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT => Right(response.status)
          case _          => Left(response)
        }
      }
}
