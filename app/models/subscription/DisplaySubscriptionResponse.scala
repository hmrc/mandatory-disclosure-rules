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

package models.subscription

import play.api.Logger
import play.api.libs.json._
import cats.data.NonEmptyList

case class ResponseDetail(subscriptionID: String,
                          tradingName: Option[String],
                          isGBUser: Boolean,
                          primaryContact: ContactInformation,
                          secondaryContact: Option[ContactInformation]
)

object ResponseDetail {

  implicit def formatsNonEmptyList[A](implicit listReads: Reads[List[A]], listWrites: Writes[List[A]]): Format[NonEmptyList[A]] =
    new Format[NonEmptyList[A]] {
      override def writes(o: NonEmptyList[A]): JsValue = Json.toJson(o.toList)

      override def reads(json: JsValue): JsResult[NonEmptyList[A]] = json.validate(listReads).map(NonEmptyList.fromListUnsafe)
    }

  val logger = Logger.apply(getClass)
  implicit lazy val reads: Reads[ResponseDetail] = {
    import play.api.libs.functional.syntax._

    (
      (__ \ "subscriptionID").read[String] and
        (__ \ "tradingName").readNullable[String] and
        (__ \ "isGBUser").read[Boolean] and
        (__ \ "primaryContact").read[NonEmptyList[ContactInformation]] and
        (__ \ "secondaryContact").readNullable[Seq[ContactInformation]]
    ) { (subscriptionID, tradingName, isGBUser, primaryContact, secondaryContact) =>
      logger.warn(s"ResponseDetail: received ${primaryContact.size} primary contacts and ${secondaryContact.fold(0)(_.size)} secondaryContacts")
      ResponseDetail(subscriptionID, tradingName, isGBUser, primaryContact.head, secondaryContact.map(_.head))
    }
  }

  implicit val writes: OWrites[ResponseDetail] = Json.writes[ResponseDetail]
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
