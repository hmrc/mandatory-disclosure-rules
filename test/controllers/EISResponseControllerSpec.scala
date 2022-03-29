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

package controllers

import base.SpecBase
import controllers.auth.{AuthAction, FakeAuthAction}
import models.submission._
import models.xml.FileErrorCode.MessageRefIDHasAlreadyBeenUsed
import models.xml.RecordErrorCode.MessageTypeIndic
import models.xml.{BREResponse, FileErrors, GenericStatusMessage, RecordError, ValidationErrors, ValidationStatus}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsXml}
import repositories.submission.FileDetailsRepository
import services.EmailService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.Future
import scala.xml.NodeSeq

class EISResponseControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]
  val mockEmailService: EmailService                   = mock[EmailService]

  override def beforeEach(): Unit = {
    reset(mockFileDetailsRepository, mockEmailService)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
      bind[EmailService].toInstance(mockEmailService),
      bind[AuthAction].to[FakeAuthAction]
    )
    .build()

  private val randomUUID = UUID.randomUUID()
  val xml: NodeSeq = <gsm:BREResponse xmlns:gsm="http://www.hmrc.gsi.gov.uk/gsm">
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47.400Z</receiptDate>
      <regime>MDR</regime>
      <conversationID>{randomUUID}</conversationID>
      <schemaVersion>1.0.0</schemaVersion>
    </requestCommon>
    <requestDetail>
      <GenericStatusMessage>
        <ValidationErrors>
          <FileError>
            <Code>50009</Code>
            <Details>Duplicate message ref IDs</Details>
          </FileError>
          <RecordError>
            <Code>80000</Code>
            <Details>Duplicate doc ref IDs</Details>
            <DocRefIDInError>MDRUSER001DHSJEURUT20001</DocRefIDInError>
            <DocRefIDInError>MDRUSER001DHSJEURUT20002</DocRefIDInError>
          </RecordError>
        </ValidationErrors>
        <ValidationResult>
          <Status>Rejected</Status>
        </ValidationResult>
      </GenericStatusMessage>
    </requestDetail>
  </gsm:BREResponse>

  "EISResponseController" - {
    "must return NoContent when input xml is valid" in {
      val fileDetails =
        FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Accepted, "file1.xml", LocalDateTime.now(), LocalDateTime.now())

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockFileDetailsRepository, times(1)).updateStatus(any[String](), any[FileStatus]())
    }

    "must send an email when on the fast journey and file upload is Accepted" in {
      val fileDetails =
        FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Accepted, "file1.xml", LocalDateTime.now(), LocalDateTime.now())

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockEmailService, times(1)).sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier])
    }

    "must not send an email when on the fast journey and file upload is Rejected" in {
      val fileDetails =
        FileDetails(
          ConversationId("conversationId123456"),
          "subscriptionId",
          "messageRefId",
          Rejected(ValidationErrors(None, None)),
          "file1.xml",
          LocalDateTime.now(),
          LocalDateTime.now()
        )

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockEmailService, times(0)).sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier])
    }

    "must not send an email when on the fast journey and file upload is Pending" in {
      val fileDetails =
        FileDetails(
          ConversationId("conversationId123456"),
          "subscriptionId",
          "messageRefId",
          Pending,
          "file1.xml",
          LocalDateTime.now(),
          LocalDateTime.now()
        )

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockEmailService, times(0)).sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier])
    }

    "must send an email when on the slow journey and file upload is Accepted" in {
      val fileDetails =
        FileDetails(ConversationId("conversationId123456"),
                    "subscriptionId",
                    "messageRefId",
                    Accepted,
                    "file1.xml",
                    LocalDateTime.now().minusSeconds(20),
                    LocalDateTime.now()
        )

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockEmailService, times(1)).sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier])
    }

    "must send an email when on the slow journey and file upload is Rejected" in {
      val fileDetails =
        FileDetails(
          ConversationId("conversationId123456"),
          "subscriptionId",
          "messageRefId",
          Rejected(ValidationErrors(None, None)),
          "file1.xml",
          LocalDateTime.now().minusSeconds(20),
          LocalDateTime.now()
        )

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockEmailService, times(1)).sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier])
    }

    "must not send an email when on the slow journey and file upload is Pending" in {
      val fileDetails =
        FileDetails(
          ConversationId("conversationId123456"),
          "subscriptionId",
          "messageRefId",
          Pending,
          "file1.xml",
          LocalDateTime.now().minusSeconds(20),
          LocalDateTime.now()
        )

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual NO_CONTENT
      verify(mockEmailService, times(0)).sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier])
    }
    "must return BadRequest when input xml is invalid" in {

      val invalidXml = <test>invalid</test>

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(invalidXml)

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      verify(mockFileDetailsRepository, never).updateStatus(any[String](), any[FileStatus]())
    }

    "must return InternalServerError on failing to update the status" in {

      when(mockFileDetailsRepository.updateStatus(any[String](), any[FileStatus]())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString)
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        verify(mockFileDetailsRepository, times(1)).updateStatus(any[String](), any[FileStatus]())
      }
    }

    "Must return false when containing a non CDAX error code" in {
      val uuid = UUID.randomUUID().toString
      val fileErrors = Some(List(FileErrors(MessageRefIDHasAlreadyBeenUsed, Some("Duplicate message ref ID"))))
      val breResponse = BREResponse(
        "MDR",
        uuid,
        GenericStatusMessage(
          ValidationErrors(
            fileErrors,
            Some(
              List(
                RecordError(
                  MessageTypeIndic,
                  Some("A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both"),
                  Some(List("asjdhjjhjssjhdjshdAJGSJJS"))
                )
              )
            )
          ),
          ValidationStatus.rejected
        )
      )

      when(mockFileDetailsRepository.updateStatus(any[String](), any[FileStatus]())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString)
          .withXmlBody(xml)


    }
  }
}
