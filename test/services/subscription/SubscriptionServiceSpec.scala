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

package services.subscription

import base.SpecBase
import connectors.SubscriptionConnector
import models.error.{ReadSubscriptionError, UpdateSubscriptionError}
import models.subscription.{DisplaySubscriptionForMDRRequest, RequestDetailForUpdate, UpdateSubscriptionForMDRRequest}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status._
import play.api.inject.bind
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = reset(mockSubscriptionConnector)

  val mockSubscriptionConnector = mock[SubscriptionConnector]

  "SubscriptionService" - {
    val application = applicationBuilder()
      .overrides(
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()

    val requestDetailJson = Json.parse("""
                                         |{
                                         |      "IDType": "SAFE",
                                         |      "IDNumber": "IDNumber",
                                         |      "tradingName": "Trading Name",
                                         |      "isGBUser": true,
                                         |      "primaryContact":
                                         |        {
                                         |          "individual": {
                                         |             "lastName": "lastName",
                                         |             "firstName": "firstName",
                                         |             "middleName": "middleName"
                                         |         },
                                         |          "email": "test@email.com",
                                         |          "phone": "+4411223344"
                                         |        },
                                         |      "secondaryContact":
                                         |        {
                                         |          "organisation": {
                                         |            "organisationName": "orgName"
                                         |          },
                                         |          "email": "test@email.com",
                                         |          "phone": "+4411223344"
                                         |        }
                                         |}
                                         |""".stripMargin)
    val requestDetailForUpdate = requestDetailJson.as[RequestDetailForUpdate]

    "must correctly retrieve subscription from connector" in {
      val service = application.injector.instanceOf[SubscriptionService]
      val subscriptionResponseJson: String =
        """
          |{
          |"displaySubscriptionForMDRResponse": {
          |"responseCommon": {
          |"status": "OK",
          |"processingDate": "2020-08-09T11:23:45Z"
          |},
          |"responseDetail": {
          |"subscriptionID": "111111111",
          |"tradingName": "",
          |"isGBUser": true,
          |"primaryContact": [
          |{
          |"email": "",
          |"phone": "",
          |"mobile": "",
          |"individual": {
          |"lastName": "Last",
          |"firstName": "First"
          |}
          |}
          |],
          |"secondaryContact": [
          |{
          |"email": "",
          |"organisation": {
          |"organisationName": ""
          |}
          |}
          |]
          |}
          |}
          |}""".stripMargin

      when(mockSubscriptionConnector.readSubscriptionInformation(any[DisplaySubscriptionForMDRRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(OK, subscriptionResponseJson)))

      val result = service.getContactInformation("111111111")

      whenReady(result) { _ =>
        verify(mockSubscriptionConnector, times(1)).readSubscriptionInformation(any[DisplaySubscriptionForMDRRequest]())(any[HeaderCarrier](),
                                                                                                                         any[ExecutionContext]()
        )
      }
    }

    "must retrieve ReadSubscriptionError from connector when not ok status" in {
      val service = application.injector.instanceOf[SubscriptionService]

      when(mockSubscriptionConnector.readSubscriptionInformation(any[DisplaySubscriptionForMDRRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result = service.getContactInformation("111111111")

      whenReady(result) { sub =>
        sub mustBe Left(ReadSubscriptionError(500))
        verify(mockSubscriptionConnector, times(1)).readSubscriptionInformation(any[DisplaySubscriptionForMDRRequest]())(any[HeaderCarrier](),
                                                                                                                         any[ExecutionContext]()
        )
      }
    }

    "must  return UpdateSubscription with OK status when connector response with ok status" in {
      val service = application.injector.instanceOf[SubscriptionService]

      when(mockSubscriptionConnector.updateSubscription(any[UpdateSubscriptionForMDRRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(OK, "Good Response")))

      val result = service.updateSubscription(requestDetailForUpdate)

      whenReady(result) { sub =>
        verify(mockSubscriptionConnector, times(1)).updateSubscription(any[UpdateSubscriptionForMDRRequest]())(any[HeaderCarrier](), any[ExecutionContext]())
        sub mustBe Right(())
      }
    }

    "must have UpdateSubscriptionError when connector response with not ok status" in {
      val service = application.injector.instanceOf[SubscriptionService]

      when(mockSubscriptionConnector.updateSubscription(any[UpdateSubscriptionForMDRRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result = service.updateSubscription(requestDetailForUpdate)

      whenReady(result) { sub =>
        verify(mockSubscriptionConnector, times(1)).updateSubscription(any[UpdateSubscriptionForMDRRequest]())(any[HeaderCarrier](), any[ExecutionContext]())
        sub mustBe Left(UpdateSubscriptionError(500))
      }
    }
  }

}
