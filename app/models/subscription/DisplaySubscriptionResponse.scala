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

import play.api.libs.json._

case class ResponseDetail(subscriptionID: String,
                          tradingName: Option[String],
                          isGBUser: Boolean,
                          primaryContact: PrimaryContact,
                          secondaryContact: Option[SecondaryContact]
)

object ResponseDetail {
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
}

case class ReturnParameters(paramName: String, paramValue: String)

object ReturnParameters {
  implicit val format: Format[ReturnParameters] = Json.format[ReturnParameters]
}

case class ResponseCommon(status: String, statusText: Option[String], processingDate: String, returnParameters: Option[Seq[ReturnParameters]])

object ResponseCommon {
  implicit val format: Format[ResponseCommon] = Json.format[ResponseCommon]
}

case class DisplaySubscriptionResponse(responseCommon: ResponseCommon, responseDetail: ResponseDetail)

object DisplaySubscriptionResponse {
  implicit val format: OFormat[DisplaySubscriptionResponse] = Json.format[DisplaySubscriptionResponse]
}

case class DisplaySubscriptionForMDRResponse(displaySubscriptionForMDRResponse: DisplaySubscriptionResponse)

object DisplaySubscriptionForMDRResponse {
  implicit val format: OFormat[DisplaySubscriptionForMDRResponse] = Json.format[DisplaySubscriptionForMDRResponse]
}
