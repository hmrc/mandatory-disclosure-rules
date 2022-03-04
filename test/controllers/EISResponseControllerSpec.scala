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
import models.submission.{Accepted, ConversationId, FileDetails, FileStatus}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsXml, POST}
import repositories.submission.FileDetailsRepository

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.Future
import scala.xml.NodeSeq

class EISResponseControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]

  override def beforeEach(): Unit = {
    reset(mockFileDetailsRepository)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[FileDetailsRepository].toInstance(mockFileDetailsRepository)
    )
    .build()

  private val randomUUID = UUID.randomUUID()
  val xml: NodeSeq = <BREResponse>
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47Z</receiptDate>
      <regime>MDR</regime>
      <conversationID>{randomUUID}</conversationID>
      <schemaVersion>1.0.0</schemaVersion>
    </requestCommon>
    <requestDetail>
      <gsm:GenericStatusMessage>
        <gsm:ValidationErrors>
          <gsm:FileError>
            <gsm:Code>50009</gsm:Code>
            <gsm:Details Language="EN">Duplicate message ref ID</gsm:Details>
          </gsm:FileError>
          <gsm:RecordError>
            <gsm:Code>80010</gsm:Code>
            <gsm:Details Language="EN">A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both</gsm:Details>
            <gsm:DocRefIDInError>asjdhjjhjssjhdjshdAJGSJJS</gsm:DocRefIDInError>
          </gsm:RecordError>
        </gsm:ValidationErrors>
        <gsm:ValidationResult>
          <gsm:Status>Rejected</gsm:Status>
        </gsm:ValidationResult>
      </gsm:GenericStatusMessage>
    </requestDetail>
  </BREResponse>

  "EISResponseController" - {
    "must return ok when input xml is valid" in {
      val fileDetails =
        FileDetails(ConversationId("conversationId123456"), "subscriptionId", "messageRefId", Accepted, "file1.xml", LocalDateTime.now(), LocalDateTime.now())

      when(mockFileDetailsRepository.updateStatus(any[String](), any[FileStatus]())).thenReturn(Future.successful(Some(fileDetails)))

      val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
        .withHeaders("x-conversation-id" -> randomUUID.toString)
        .withXmlBody(xml)

      val result = route(application, request).value

      status(result) mustEqual OK
      verify(mockFileDetailsRepository, times(1)).updateStatus(any[String](), any[FileStatus]())
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

    "must return InternalServerError on filing to update the status" in {

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
