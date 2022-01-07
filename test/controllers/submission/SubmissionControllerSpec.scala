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

package controllers.submission

import base.SpecBase
import connectors.{SubmissionConnector, SubscriptionConnector}
import controllers.auth.{FakeIdentifierAuthAction, IdentifierAuthAction}
import controllers.routes._
import controllers.submission.SubmissionFixture._
import models.error.ReadSubscriptionError
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.submission.{ReadSubscriptionService, TransformService}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.xml.NodeSeq
class SubmissionControllerSpec extends SpecBase with MockitoSugar with ScalaCheckDrivenPropertyChecks with BeforeAndAfterEach {

  val mockTransformService                                 = mock[TransformService]
  val mockSubmissionConnector: SubmissionConnector         = mock[SubmissionConnector]
  val mockSubscriptionConnector: SubscriptionConnector     = mock[SubscriptionConnector]
  val mockReadSubscriptionService: ReadSubscriptionService = mock[ReadSubscriptionService]

  val errorStatusCodes: Seq[Int] = Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED, CONFLICT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE)

  override def beforeEach(): Unit =
    reset(mockReadSubscriptionService, mockSubscriptionConnector, mockSubmissionConnector)

  "submission controller" - {

    val application = applicationBuilder()
      .overrides(
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
        bind[SubmissionConnector].toInstance(mockSubmissionConnector),
        bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
        bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
      )
      .build()

    "when a file is posted we transform it, send it to the HOD and return OK" in {

      when(mockReadSubscriptionService.getContactInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(Json.toJson(responseDetail))))
      when(mockSubmissionConnector.submitDisclosure(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val submission = basicXml

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure().url).withXmlBody(submission)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe OK

      val argumentCaptor: ArgumentCaptor[NodeSeq] = ArgumentCaptor.forClass(classOf[NodeSeq])

      verify(mockSubmissionConnector, times(1)).submitDisclosure(argumentCaptor.capture())(any())
    }
    "when a read subscription returns not OK response INTERNAL_SERVER_ERROR" in {

      when(mockReadSubscriptionService.getContactInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(ReadSubscriptionError(500))))
      when(mockSubmissionConnector.submitDisclosure(any())(any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val submission = basicXml

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure().url).withXmlBody(submission)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      val argumentCaptor: ArgumentCaptor[NodeSeq] = ArgumentCaptor.forClass(classOf[NodeSeq])

      verify(mockSubmissionConnector, times(0)).submitDisclosure(argumentCaptor.capture())(any())
    }
  }
}
