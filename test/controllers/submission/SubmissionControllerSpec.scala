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
import handlers.{XmlHandler, XmlMockBasicHandler}
import models.error.ReadSubscriptionError
import models.submission._
import models.upscan.UploadId
import models.validation.SaxParseError
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, ArgumentMatchers, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.submission.FileDetailsRepository
import services.audit.AuditService
import services.submission.{SDESService, TransformService}
import services.subscription.SubscriptionService
import services.validation.XMLValidationService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class SubmissionControllerSpec extends SpecBase with MockitoSugar with ScalaCheckDrivenPropertyChecks with BeforeAndAfterEach {

  val mockAuditService: AuditService                   = mock[AuditService]
  val mockTransformService                             = mock[TransformService]
  val mockSubmissionConnector: SubmissionConnector     = mock[SubmissionConnector]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  val mockReadSubscriptionService: SubscriptionService = mock[SubscriptionService]
  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]
  val mockXMLValidationService: XMLValidationService   = mock[XMLValidationService]
  val mockSDESService: SDESService                     = mock[SDESService]
  val mockAppConf: AppConfig                           = mock[AppConfig]

  val messageSpec                = MessageSpecData("x9999", MDR401, 2, "OECD1", MultipleNewInformation)
  val errorStatusCodes: Seq[Int] = Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED, CONFLICT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE)

  override def beforeEach(): Unit =
    reset(
      mockAuditService,
      mockXMLValidationService,
      mockReadSubscriptionService,
      mockSubscriptionConnector,
      mockSubmissionConnector,
      mockFileDetailsRepository,
      mockSDESService
    )

  val application = applicationBuilder()
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
      bind[SubmissionConnector].toInstance(mockSubmissionConnector),
      bind[SubscriptionService].toInstance(mockReadSubscriptionService),
      bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
      bind[XMLValidationService].toInstance(mockXMLValidationService),
      bind[SDESService].toInstance(mockSDESService),
      bind[AppConfig].toInstance(mockAppConf),
      bind[XmlHandler].toInstance(new XmlMockBasicHandler),
      bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
    )
    .build()

  "submission controller normal size file path" - {

    when(mockAppConf.maxNormalFileSize).thenReturn(3145728)

    "when a file is posted we transform it, send it to the HOD and return OK" in {
      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(responseDetail)))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Right(basicXml))

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 1000L, "dummyUrl", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
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

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 1000L, "dummyUrl", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
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

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 1000L, "dummyUrl", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
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

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 1000L, "dummyUrl", "1234", messageSpec))

      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Left(ListBuffer(SaxParseError(1, "Invalid Node at line 1"))))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      val argumentCaptor: ArgumentCaptor[NodeSeq]                      = ArgumentCaptor.forClass(classOf[NodeSeq])
      val argumentCaptorSubmissionDetails: ArgumentCaptor[FileDetails] = ArgumentCaptor.forClass(classOf[FileDetails])
      val argumentCaptorConversationId: ArgumentCaptor[ConversationId] = ArgumentCaptor.forClass(classOf[ConversationId])

      verify(mockFileDetailsRepository, times(0)).insert(argumentCaptorSubmissionDetails.capture())
      verify(mockSubmissionConnector, times(0)).submitDisclosure(argumentCaptor.capture(), argumentCaptorConversationId.capture())(any[HeaderCarrier]())
    }

    "when auditFileSubmission is enabled must send a MandatoryDisclosureRulesFileSubmission audit event" in {

      val application = applicationBuilder()
        .configure("auditing.enabled" -> true, "auditing.event.fileSubmission.enabled" -> true)
        .overrides(
          bind[AuditService].toInstance(mockAuditService),
          bind[SubmissionConnector].toInstance(mockSubmissionConnector),
          bind[SubscriptionService].toInstance(mockReadSubscriptionService),
          bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
          bind[XMLValidationService].toInstance(mockXMLValidationService),
          bind[XmlHandler].toInstance(new XmlMockBasicHandler),
          bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
        )
        .build()

      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockAuditService.sendAuditEvent(ArgumentMatchers.eq("MandatoryDisclosureRulesFileSubmission"), any[JsValue]())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Success))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(responseDetail)))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Right(basicXml))

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 1000L, "dummyUrl", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe OK
      verify(mockAuditService, times(1)).sendAuditEvent(ArgumentMatchers.eq("MandatoryDisclosureRulesFileSubmission"), any[JsValue])(any[HeaderCarrier])
    }

    "when auditFileSubmission is disabled must not send a MandatoryDisclosureRulesFileSubmissionType audit event" in {

      val application = applicationBuilder()
        .configure("auditing.enabled" -> true, "auditing.event.fileSubmission.enabled" -> false)
        .overrides(
          bind[AuditService].toInstance(mockAuditService),
          bind[SubmissionConnector].toInstance(mockSubmissionConnector),
          bind[SubscriptionService].toInstance(mockReadSubscriptionService),
          bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
          bind[XmlHandler].toInstance(new XmlMockBasicHandler),
          bind[XMLValidationService].toInstance(mockXMLValidationService),
          bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
        )
        .build()

      when(mockFileDetailsRepository.insert(any[FileDetails]()))
        .thenReturn(Future.successful(true))
      when(mockAuditService.sendAuditEvent(ArgumentMatchers.eq("MandatoryDisclosureRulesFileSubmission"), any[JsValue]())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Success))
      when(mockReadSubscriptionService.getContactInformation(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(responseDetail)))
      when(mockSubmissionConnector.submitDisclosure(any[NodeSeq](), any[ConversationId])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
      when(mockXMLValidationService.validate(any[NodeSeq], any[String]))
        .thenReturn(Right(basicXml))

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 1000L, "http://localhost/", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe OK
      verify(mockAuditService, times(0)).sendAuditEvent(ArgumentMatchers.eq("MandatoryDisclosureRulesFileSubmission"), any[JsValue])(any[HeaderCarrier])
    }
  }
  "submission controller large file path" - {
    "when a file is larger than 3mb we use the SDES journey" in {
      val conversationId = ConversationId("1234")
      when(mockSDESService.fileNotify(any[SubmissionDetails])(any[HeaderCarrier])).thenReturn(Future.successful(Right(conversationId)))

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 4000000L, "dummyUrl", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe OK
      verify(mockSDESService, times(1)).fileNotify(any[SubmissionDetails])(any[HeaderCarrier])
    }
    "when the sdesService returns an error we return an Internal Server Error" in {
      when(mockSDESService.fileNotify(any[SubmissionDetails])(any[HeaderCarrier])).thenReturn(Future.successful(Left(new Exception("Error"))))

      val jsonPost = Json.toJson(SubmissionDetails("fileName", UploadId("uploadId"), "enrolmentId", 4000000L, "dummyUrl", "1234", messageSpec))

      val request                = FakeRequest(POST, SubmissionController.submitDisclosure.url).withJsonBody(jsonPost)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockSDESService, times(1)).fileNotify(any[SubmissionDetails])(any[HeaderCarrier])
    }

  }
}
