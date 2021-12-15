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

package services.submission

import connectors.SubscriptionConnector
import models.error.{ApiError, ReadSubscriptionError}
import models.subscription._
import play.api.Logging
import play.api.http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (subscriptionConnector: SubscriptionConnector) extends Logging {

  def getContactInformation(enrolmentId: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Either[ApiError, ResponseDetail]] = {

    val subscriptionRequest: DisplaySubscriptionForMDRRequest =
      DisplaySubscriptionForMDRRequest(
        DisplaySubscriptionDetails(
          RequestCommonForSubscription(),
          ReadSubscriptionRequestDetail(enrolmentId)
        )
      )

    subscriptionConnector.readSubscriptionInformation(subscriptionRequest).map { response =>
      response.status match {
        case OK =>
          Right(response.json.as[DisplaySubscriptionForMDRResponse].displaySubscriptionForMDRResponse.responseDetail)
        case status =>
          logger.warn(s"Read subscription Got Status $status")
          Left(ReadSubscriptionError(status))
      }
    }
  }
}
