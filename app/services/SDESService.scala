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

package services

import config.AppConfig
import connectors.SDESConnector
import models.sdes._
import models.submission.ConversationId
import models.submissions.SubmissionDetails
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SDESService {
  def fileNotify(submissionDetails: SubmissionDetails)(implicit hc: HeaderCarrier): Future[Unit]
}

class SDESServiceImpl @Inject() (sdesConnector: SDESConnector, appConfig: AppConfig)(implicit
  ec: ExecutionContext
) extends SDESService
    with Logging {

  override def fileNotify(submissionDetails: SubmissionDetails)(implicit hc: HeaderCarrier): Future[Unit] = {
    val conversationId    = ConversationId()
    val fileNotifyRequest = FileTransferNotification(submissionDetails, appConfig.sdesInformationType, appConfig.sdesRecipientOrSender, conversationId.value)
    logger.debug(s"SDES notification request: ${Json.stringify(Json.toJson(fileNotifyRequest))}")
    sdesConnector.fileReady(fileNotifyRequest).flatMap { response =>
      response.status match {
        case NO_CONTENT =>
          logger.info(
            s"SDES has been notified of file :: ${fileNotifyRequest.file.name}  with correlationId::${fileNotifyRequest.audit.correlationID}"
          )
          //ToDo add correlation id record to mongo tracker for callback
          Future.successful(())
        case status =>
          val e = new Exception(s"Exception in notifying SDES. Received http status: $status body: ${response.body}")
          logger.error(
            s"Received a non 204 status from SDES when notified about file :: ${fileNotifyRequest.file.name}  with correlationId::${fileNotifyRequest.audit.correlationID}.",
            e
          )
          Future.failed(e)
      }
    }
  }
}
