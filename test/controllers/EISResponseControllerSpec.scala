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
import controllers.auth.{FakeIdentifierAuthAction, IdentifierAuthAction}
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsXml, POST}
import repositories.submission.FileDetailsRepository

import java.util.UUID
import scala.xml.NodeSeq

class EISResponseControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]

  override def beforeEach(): Unit = {
    reset(mockFileDetailsRepository)
    super.beforeEach()
  }

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

      val application: Application = applicationBuilder()
        .overrides(
          bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
          bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString)
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return BadRequest when input xml is invalid" in {

      val application: Application = applicationBuilder()
        .overrides(
          bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
          bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
        )
        .build()

      val invalidXml = <test>invalid</test>

      running(application) {
        val request = FakeRequest(POST, routes.EISResponseController.processEISResponse().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString)
          .withXmlBody(invalidXml)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
