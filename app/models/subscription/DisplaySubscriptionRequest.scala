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

package models.subscription

import play.api.libs.json.{Json, OFormat}

case class RequestParameters(paramName: String, paramValue: String)

object RequestParameters {
  implicit val indentifierFormats: OFormat[RequestParameters] =
    Json.format[RequestParameters]
}

case class RequestCommonForSubscription(
  regime: String,
  conversationID: Option[String] = None,
  receiptDate: String,
  acknowledgementReference: String,
  originatingSystem: String,
  requestParameters: Option[Seq[RequestParameters]]
)

object RequestCommonForSubscription {
  implicit val requestCommonForSubscriptionFormats: OFormat[RequestCommonForSubscription] =
    Json.format[RequestCommonForSubscription]
}

case class ReadSubscriptionRequestDetail(IDType: String, IDNumber: String)

object ReadSubscriptionRequestDetail {
  implicit val format: OFormat[ReadSubscriptionRequestDetail] =
    Json.format[ReadSubscriptionRequestDetail]
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
