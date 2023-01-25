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

package controllers.submission

import base.SpecBase
import config.AppConfig
import connectors.{SubmissionConnector, SubscriptionConnector}
import controllers.auth.{FakeIdentifierAuthAction, IdentifierAuthAction}
import controllers.routes._
import controllers.submission.SubmissionFixture._
import models.error.ReadSubscriptionError
import models.submission.{ConversationId, FileDetails}
import models.validation.SaxParseError
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import repositories.submission.FileDetailsRepository
import services.submission.TransformService
import services.subscription.SubscriptionService
import services.validation.XMLValidationService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq
class SubmissionControllerSpec extends SpecBase with MockitoSugar with ScalaCheckDrivenPropertyChecks with BeforeAndAfterEach {

  val mockTransformService                             = mock[TransformService]
  val mockSubmissionConnector: SubmissionConnector     = mock[SubmissionConnector]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  val mockReadSubscriptionService: SubscriptionService = mock[SubscriptionService]
  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]
  val mockXMLValidationService: XMLValidationService   = mock[XMLValidationService]
  val mockAppConf: AppConfig                           = mock[AppConfig]

  val errorStatusCodes: Seq[Int] = Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED, CONFLICT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE)

  override def beforeEach(): Unit =
    reset(mockAppConf, mockXMLValidationService, mockReadSubscriptionService, mockSubscriptionConnector, mockSubmissionConnector, mockFileDetailsRepository)

  "submission controller" - {

    val application = applicationBuilder()
      .overrides(
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
        bind[SubmissionConnector].toInstance(mockSubmissionConnector),
        bind[SubscriptionService].toInstance(mockReadSubscriptionService),
        bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
        bind[XMLValidationService].toInstance(mockXMLValidationService),
        bind[AppConfig].toInstance(mockAppConf),
        bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
      )
      .build()

    "when a file is posted we transform it, send it to the HOD and return OK" in {
      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(responseDetail)))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Right(basicXml))
      val submission = basicXml

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withXmlBody(submission)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe OK

      val argumentCaptor: ArgumentCaptor[NodeSeq]                      = ArgumentCaptor.forClass(classOf[NodeSeq])
      val argumentCaptorSubmissionDetails: ArgumentCaptor[FileDetails] = ArgumentCaptor.forClass(classOf[FileDetails])
      val argumentCaptorConversationId: ArgumentCaptor[ConversationId] = ArgumentCaptor.forClass(classOf[ConversationId])

      verify(mockFileDetailsRepository, times(1)).insert(argumentCaptorSubmissionDetails.capture())
      verify(mockSubmissionConnector, times(1)).submitDisclosure(argumentCaptor.capture(), argumentCaptorConversationId.capture())(any[HeaderCarrier]())
    }

    "when a file is posted we transform it and trim and blank spaces on the node send it to the HOD expect return OK" in {
      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(responseDetailForIndividual)))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Right(basicXml))
      val submission = basicXml

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withXmlBody(submission)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe OK

      val argumentCaptor: ArgumentCaptor[NodeSeq]                      = ArgumentCaptor.forClass(classOf[NodeSeq])
      val argumentCaptorSubmissionDetails: ArgumentCaptor[FileDetails] = ArgumentCaptor.forClass(classOf[FileDetails])
      val argumentCaptorConversationId: ArgumentCaptor[ConversationId] = ArgumentCaptor.forClass(classOf[ConversationId])

      verify(mockFileDetailsRepository, times(1)).insert(argumentCaptorSubmissionDetails.capture())
      verify(mockSubmissionConnector, times(1)).submitDisclosure(argumentCaptor.capture(), argumentCaptorConversationId.capture())(any[HeaderCarrier]())
    }

    "when a read subscription returns not OK response INTERNAL_SERVER_ERROR" in {
      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Left(ReadSubscriptionError(500))))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val submission = basicXml

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withXmlBody(submission)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      val argumentCaptor: ArgumentCaptor[NodeSeq]                      = ArgumentCaptor.forClass(classOf[NodeSeq])
      val argumentCaptorSubmissionDetails: ArgumentCaptor[FileDetails] = ArgumentCaptor.forClass(classOf[FileDetails])
      val argumentCaptorConversationId: ArgumentCaptor[ConversationId] = ArgumentCaptor.forClass(classOf[ConversationId])

      verify(mockFileDetailsRepository, times(0)).insert(argumentCaptorSubmissionDetails.capture())
      verify(mockSubmissionConnector, times(0)).submitDisclosure(argumentCaptor.capture(), argumentCaptorConversationId.capture())(any[HeaderCarrier]())
    }

    "when a submission xml is invalid return INTERNAL_SERVER_ERROR" in {
      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Left(ReadSubscriptionError(500))))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val submission = basicXml
      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Left(ListBuffer(SaxParseError(1, "Invalid Node at line 1"))))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withXmlBody(submission)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      val argumentCaptor: ArgumentCaptor[NodeSeq]                      = ArgumentCaptor.forClass(classOf[NodeSeq])
      val argumentCaptorSubmissionDetails: ArgumentCaptor[FileDetails] = ArgumentCaptor.forClass(classOf[FileDetails])
      val argumentCaptorConversationId: ArgumentCaptor[ConversationId] = ArgumentCaptor.forClass(classOf[ConversationId])

      verify(mockFileDetailsRepository, times(0)).insert(argumentCaptorSubmissionDetails.capture())
      verify(mockSubmissionConnector, times(0)).submitDisclosure(argumentCaptor.capture(), argumentCaptorConversationId.capture())(any[HeaderCarrier]())
    }
  }
}
