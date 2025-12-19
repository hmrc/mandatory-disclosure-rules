/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import base.SpecBase
import controllers.auth.{FakeIdentifierAuthAction, IdentifierAuthAction}
import generators.Generators
import models.error.{ReadSubscriptionError, UpdateSubscriptionError}
import models.subscription.{RequestDetailForUpdate, ResponseDetail}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.subscription.SubscriptionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val mockAuthConnector: AuthConnector    = mock[AuthConnector]
  val mockResponseDetails: ResponseDetail = mock[ResponseDetail]

  val mockSubscriptionService: SubscriptionService =
    mock[SubscriptionService]
  val application: Application = applicationBuilder()
    .overrides(
      bind[SubscriptionService].toInstance(mockSubscriptionService),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
    )
    .build()

  "SubscriptionController" - {

    val responseDetailString: String =
      """
        |{
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
        |}""".stripMargin

    val responseDetail = Json.parse(responseDetailString)

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

    "should return OK when ReadSubscription is valid" in {
      when(
        mockSubscriptionService
          .getContactInformation(any[String]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
      ).thenReturn(
        Future.successful(
          Right(responseDetail.as[ResponseDetail])
        )
      )

      val request =
        FakeRequest(
          POST,
          routes.SubscriptionController.readSubscription().url
        )

      val result = route(application, request).value
      status(result) mustEqual OK

    }

    "should return InternalServerError when ReadSubscription fails" in {
      when(
        mockSubscriptionService
          .getContactInformation(any[String]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
      ).thenReturn(
        Future.successful(
          Left(ReadSubscriptionError(500))
        )
      )

      val request =
        FakeRequest(
          POST,
          routes.SubscriptionController.readSubscription().url
        )

      val result = route(application, request).value
      status(result) mustEqual INTERNAL_SERVER_ERROR

    }

    "should return OK when UpdateSubscription was successful" in {
      when(
        mockSubscriptionService
          .updateSubscription(any[RequestDetailForUpdate]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
      ).thenReturn(
        Future.successful(
          Right(())
        )
      )

      val request =
        FakeRequest(
          POST,
          routes.SubscriptionController.updateSubscription().url
        ).withJsonBody(requestDetailJson)

      val result = route(application, request).value
      status(result) mustEqual OK

    }

    "should return InternalServerError when UpdateSubscription fails" in {
      when(
        mockSubscriptionService
          .updateSubscription(any[RequestDetailForUpdate]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
      ).thenReturn(
        Future.successful(
          Left(UpdateSubscriptionError(500))
        )
      )

      val request =
        FakeRequest(
          POST,
          routes.SubscriptionController.updateSubscription().url
        ).withJsonBody(requestDetailJson)

      val result = route(application, request).value
      status(result) mustEqual INTERNAL_SERVER_ERROR

    }

    "should return BAD_REQUEST when Json is Invalid" in
      when(
        mockSubscriptionService
          .updateSubscription(any[RequestDetailForUpdate]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
      ).thenReturn(
        Future.successful(
          Left(UpdateSubscriptionError(500))
        )
      )

    "should return error when JSON is malformed" in {
      val request =
        FakeRequest(
          POST,
          routes.SubscriptionController.updateSubscription().url
        ).withJsonBody(Json.parse("""{}"""))

      val result = route(application, request).value
      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual "Json Validation Failed"
    }

  }
}
