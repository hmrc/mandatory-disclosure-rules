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

package services.subscription

import connectors.SubscriptionConnector
import models.error.{ApiError, ReadSubscriptionError, UpdateSubscriptionError}
import models.subscription.{RequestDetailForUpdate, _}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsResult, JsValue}
import play.api.mvc.Action
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (subscriptionConnector: SubscriptionConnector) extends Logging {

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
          val responseDetail = response.json.as[DisplaySubscriptionForMDRResponse].displaySubscriptionForMDRResponse.responseDetail
          Right(responseDetail)
        case status =>
          logger.warn(s"Read subscription Got Status $status")
          Left(ReadSubscriptionError(status))
      }
    }
  }

  def updateSubscription(requestDetailForUpdate: RequestDetailForUpdate)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Either[ApiError, Unit]] =
    subscriptionConnector.updateSubscription(UpdateSubscriptionForMDRRequest(requestDetailForUpdate)).map { res =>
      res.status match {
        case OK => Right(())
        case status =>
          logger.warn(s"Update Subscription Got Status $status")
          logger.debug(s"Update Subscription Got Status $res")
          Left(UpdateSubscriptionError(status))
      }
    }
}
