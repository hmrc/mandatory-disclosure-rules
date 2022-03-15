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
import models.submission._
import models.xml.ValidationErrors
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsXml, POST}
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
      bind[EmailService].toInstance(mockEmailService)
    )
    .build()

  private val randomUUID = UUID.randomUUID()
  val xml: NodeSeq = <cadx:BREResponse xmlns:cadx="http://www.hmrc.gsi.gov.uk/mdr/cadx">
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47Z</receiptDate>
      <regime>MDR</regime>
      <conversationID>{randomUUID}</conversationID>
      <schemaVersion>1.0.0</schemaVersion>
    </requestCommon>
    <requestDetail>
      <GenericStatusMessage>
        <ValidationErrors>
          <FileError>
            <Code>50009</Code>
            <Details>Duplicate message ref ID</Details>
          </FileError>
          <RecordError>
            <Code>80010</Code>
            <Details>A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both</Details>
            <DocRefIDInError>asjdhjjhjssjhdjshdAJGSJJS</DocRefIDInError>
          </RecordError>
        </ValidationErrors>
        <ValidationResult>
          <Status>Rejected</Status>
        </ValidationResult>
      </GenericStatusMessage>
    </requestDetail>
  </cadx:BREResponse>

  "EISResponseController" - {
    "must return ok when input xml is valid" in {
      val fileDetails =
        FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Accepted, "file1.xml", LocalDateTime.now(), LocalDateTime.now())

      when(mockFileDetailsRepository.updateStatus(any[String], any[FileStatus])).thenReturn(Future.successful(Some(fileDetails)))
      when(mockEmailService.sendAndLogEmail(any[String], any[String], any[String], any[Boolean])(any[HeaderCarrier]))
        .thenReturn(Future.successful(ACCEPTED))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual OK
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

      status(result) mustEqual OK
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

      status(result) mustEqual OK
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

      status(result) mustEqual OK
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

      status(result) mustEqual OK
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

      status(result) mustEqual OK
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

      status(result) mustEqual OK
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
  }
}
