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

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.auth.UserRequest
import models.error.ReadSubscriptionError
import models.subscription._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status._
import play.api.inject.bind
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpResponse
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ReadSubscriptionServiceSpec extends SpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = reset(mockSubscriptionConnector)

  val mockSubscriptionConnector = mock[SubscriptionConnector]

  "ReadSubscriptionService" - {
    val application = applicationBuilder()
      .overrides(
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()

    "must correctly retrieve subscription from connector" in {
      val service = application.injector.instanceOf[ReadSubscriptionService]
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

      val subscriptionResponse = Json.parse(subscriptionResponseJson).as[DisplaySubscriptionForMDRResponse]

      when(mockSubscriptionConnector.readSubscriptionInformation(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, subscriptionResponseJson)))

      val result = service.getContactInformation("111111111")

      whenReady(result) { sub =>
        sub mustBe Right(subscriptionResponse.displaySubscriptionForMDRResponse.responseDetail)
        verify(mockSubscriptionConnector, times(1)).readSubscriptionInformation(any())(any(), any())
      }
    }

    "must  retrieve ReadSubscriptionError from connector when not ok status" in {
      val service = application.injector.instanceOf[ReadSubscriptionService]
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

      when(mockSubscriptionConnector.readSubscriptionInformation(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result = service.getContactInformation("111111111")

      whenReady(result) { sub =>
        sub mustBe Left(ReadSubscriptionError(500))
        verify(mockSubscriptionConnector, times(1)).readSubscriptionInformation(any())(any(), any())
      }
    }

  }

}
