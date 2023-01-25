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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class RequestCommonForUpdate(regime: String,
                                  receiptDate: String,
                                  acknowledgementReference: String,
                                  originatingSystem: String,
                                  requestParameters: Option[Seq[RequestParameter]]
)

object RequestCommonForUpdate {
  implicit val format: OFormat[RequestCommonForUpdate] = Json.format[RequestCommonForUpdate]

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  private val mdtp      = "MDTP"

  def apply(regime: String): RequestCommonForUpdate = {
    //Generate a 32 chars UUID without hyphens
    val acknowledgementReference = UUID.randomUUID().toString.replace("-", "")
    RequestCommonForUpdate(
      regime = regime,
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = acknowledgementReference,
      originatingSystem = mdtp,
      requestParameters = None
    )
  }

}

case class RequestDetailForUpdate(IDType: String,
                                  IDNumber: String,
                                  tradingName: Option[String],
                                  isGBUser: Boolean,
                                  primaryContact: ContactInformation,
                                  secondaryContact: Option[ContactInformation]
)

object RequestDetailForUpdate {

  implicit val reads: Reads[RequestDetailForUpdate] = (
    (__ \ "IDType").read[String] and
      (__ \ "IDNumber").read[String] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "isGBUser").read[Boolean] and
      (__ \ "primaryContact").read[ContactInformation] and
      (__ \ "secondaryContact").readNullable[ContactInformation]
  )((idt, idr, tn, gb, pc, sc) => RequestDetailForUpdate(idt, idr, tn, gb, pc, sc))

  implicit lazy val writes: Writes[RequestDetailForUpdate] = (
    (__ \ "IDType").write[String] and
      (__ \ "IDNumber").write[String] and
      (__ \ "tradingName").writeNullable[String] and
      (__ \ "isGBUser").write[Boolean] and
      (__ \ "primaryContact").write[Seq[ContactInformation]] and
      (__ \ "secondaryContact").writeNullable[Seq[ContactInformation]]
  )(r => (r.IDType, r.IDNumber, r.tradingName, r.isGBUser, Seq(r.primaryContact), r.secondaryContact.map(Seq(_))))

}

case class UpdateSubscriptionDetails(requestCommon: RequestCommonForUpdate, requestDetail: RequestDetailForUpdate)

object UpdateSubscriptionDetails {
  implicit val format: OFormat[UpdateSubscriptionDetails] = Json.format[UpdateSubscriptionDetails]
}

case class UpdateSubscriptionForMDRRequest(updateSubscriptionForMDRRequest: UpdateSubscriptionDetails)

object UpdateSubscriptionForMDRRequest {
  implicit val format: OFormat[UpdateSubscriptionForMDRRequest] = Json.format[UpdateSubscriptionForMDRRequest]

  def apply(requestDetail: RequestDetailForUpdate): UpdateSubscriptionForMDRRequest =
    UpdateSubscriptionForMDRRequest(UpdateSubscriptionDetails(RequestCommonForUpdate("MDR"), requestDetail))
}
