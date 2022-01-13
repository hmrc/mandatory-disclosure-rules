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

package models.subscription

import play.api.libs.json.{Json, OFormat}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class RequestParameter(paramName: String, paramValue: String)

object RequestParameter {
  implicit val indentifierFormats: OFormat[RequestParameter] =
    Json.format[RequestParameter]
}

case class RequestCommonForSubscription(
  regime: String,
  conversationID: Option[String] = None,
  receiptDate: String,
  acknowledgementReference: String,
  originatingSystem: String,
  requestParameters: Option[Seq[RequestParameter]]
)

object RequestCommonForSubscription {
  //Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Zs
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val requestCommonForSubscriptionFormats: OFormat[RequestCommonForSubscription] =
    Json.format[RequestCommonForSubscription]

  def apply(): RequestCommonForSubscription = {
    //Generate a 32 chars UUID without hyphens
    val acknowledgementReference = UUID.randomUUID().toString.replace("-", "")
    val conversationID           = UUID.randomUUID().toString

    RequestCommonForSubscription(
      regime = "MDR",
      conversationID = Some(conversationID),
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = acknowledgementReference,
      originatingSystem = "MDTP",
      requestParameters = None
    )
  }
}

case class ReadSubscriptionRequestDetail(IDType: String, IDNumber: String)

object ReadSubscriptionRequestDetail {
  implicit val format: OFormat[ReadSubscriptionRequestDetail] =
    Json.format[ReadSubscriptionRequestDetail]

  def apply(subscriptionId: String): ReadSubscriptionRequestDetail           = new ReadSubscriptionRequestDetail("MDR", subscriptionId)
  def apply(IDType: String, IDNumber: String): ReadSubscriptionRequestDetail = new ReadSubscriptionRequestDetail(IDType, IDNumber)

}

case class DisplaySubscriptionDetails(
  requestCommon: RequestCommonForSubscription,
  requestDetail: ReadSubscriptionRequestDetail
)

object DisplaySubscriptionDetails {
  implicit val format: OFormat[DisplaySubscriptionDetails] =
    Json.format[DisplaySubscriptionDetails]
}

case class DisplaySubscriptionForMDRRequest(
  displaySubscriptionForMDRRequest: DisplaySubscriptionDetails
)

object DisplaySubscriptionForMDRRequest {
  implicit val format: OFormat[DisplaySubscriptionForMDRRequest] =
    Json.format[DisplaySubscriptionForMDRRequest]
}
