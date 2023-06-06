/*
 * Copyright 2023 HM Revenue & Customs
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
import models.sdes._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import com.google.inject.Inject
import play.api.http.Status.NO_CONTENT

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SDESConnector @Inject() (
  val config: AppConfig,
  val http: HttpClient
)() {

  private val extraHeaders: Seq[(String, String)] = Seq("x-client-id" -> config.sdesclientId)

  def fileReady(fileTransferNotification: FileTransferNotification)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, Int]] =
    http.POST[FileTransferNotification, HttpResponse](s"${config.sdesUrl}", fileTransferNotification, extraHeaders) map { response =>
      response.status match {
        case NO_CONTENT => Right(response.status)
        case _          => Left(response)
      }
    }
}
