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

package services.submission

import config.AppConfig
import connectors.SDESConnector
import models.sdes._
import models.submission.{ConversationId, FileDetails, Pending, SubmissionMetaData}
import models.submissions.SubmissionDetails
import models.subscription.{IndividualDetails, ResponseDetail}
import play.api.Logging
import play.api.libs.json.Json
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeFormatUtil

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SDESService {
  def fileNotify(submissionDetails: SubmissionDetails)(implicit hc: HeaderCarrier): Future[Either[Exception, ConversationId]]
}

class SDESServiceImpl @Inject() (sdesConnector: SDESConnector, fileDetailsRepository: FileDetailsRepository, appConfig: AppConfig)(implicit
  ec: ExecutionContext
) extends SDESService
    with Logging {

  override def fileNotify(submissionDetails: SubmissionDetails)(implicit hc: HeaderCarrier): Future[Either[Exception, ConversationId]] = {
    val correlationID = ConversationId() //CorrelationID is also a UUID so using ConversationId for compatibility with FileDetailsRepository
    val fileNotifyRequest = FileNotificationHelper.createFileNotificationRequest(submissionDetails,
                                                                                 appConfig.sdesInformationType,
                                                                                 appConfig.sdesRecipientOrSender,
                                                                                 correlationID.value
    )
    logger.debug(s"SDES notification request: ${Json.stringify(Json.toJson(fileNotifyRequest))}")
    sdesConnector.fileReady(fileNotifyRequest).flatMap {
      case Right(_) =>
        logger.info(
          s"SDES has been notified of file :: ${fileNotifyRequest.file.name}  with correlationId::${fileNotifyRequest.audit.correlationID}"
        )
        val submissionTime = DateTimeFormatUtil.zonedDateTimeNow.toLocalDateTime
        val fileDetails: FileDetails = FileDetails(
          correlationID,
          submissionDetails.enrolmentId,
          submissionDetails.messageSpecData.messageRefId,
          Some(submissionDetails.messageSpecData.reportType),
          Pending,
          submissionDetails.fileName,
          submissionTime,
          submissionTime
        )
        fileDetailsRepository.insert(fileDetails).map(_ => Right(correlationID))
      case Left(response) =>
        val e = new Exception(s"Exception in notifying SDES. Received http status: ${response.status} body: ${response.body}")
        logger.error(
          s"Received a non 204 status from SDES when notified about file :: ${fileNotifyRequest.file.name}  with correlationId::${fileNotifyRequest.audit.correlationID}.",
          e
        )
        Future.successful(Left(e))
    }
  }
}