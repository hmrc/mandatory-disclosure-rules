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

package controllers.actions

import base.SpecBase
import config.AppConfig
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.HtmlFormat
import services.validation.XMLValidationService

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class EISResponsePreConditionCheckActionRefinerSpec extends SpecBase with BeforeAndAfterEach {
  val uuid: UUID           = UUID.randomUUID()
  val headers: FakeHeaders = FakeHeaders(Seq("x-conversation-id" -> uuid.toString))

  val acceptedXml: NodeSeq = <gsm:BREResponse xmlns:gsm="http://www.hmrc.gsi.gov.uk/gsm">
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47.450Z</receiptDate>
      <regime>MDR</regime>
      <conversationID>{uuid}</conversationID>
      <schemaVersion>1.0.0</schemaVersion>
    </requestCommon>
    <requestDetail>
      <GenericStatusMessage>
        <ValidationErrors>
        </ValidationErrors>
        <ValidationResult>
          <Status>Accepted</Status>
        </ValidationResult>
      </GenericStatusMessage>
    </requestDetail>
  </gsm:BREResponse>

  val rejectedXml: NodeSeq = <gsm:BREResponse xmlns:gsm="http://www.hmrc.gsi.gov.uk/gsm">
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47.400Z</receiptDate>
      <regime>MDR</regime>
      <conversationID>{uuid}</conversationID>
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

  val appConfig: AppConfig                    = app.injector.instanceOf[AppConfig]
  val validationService: XMLValidationService = app.injector.instanceOf[XMLValidationService]

  private lazy val action = new EISResponsePreConditionCheckActionRefiner(validationService, appConfig)

  private val response: Request[NodeSeq] => Future[Result] = { _ =>
    Future.successful(Ok(HtmlFormat.empty))
  }

  "EISResponsePreConditionCheckActionRefiner" - {

    "must return Ok when 'x-conversation-id' matches with conversationId in the xml and validation status is 'Accepted'" in {

      val request = FakeRequest("", "").withHeaders(headers).withBody(acceptedXml)

      val testAction: Future[Result] = action.invokeBlock(request, response)
      status(testAction) mustBe OK
    }

    "must return Ok when 'x-conversation-id' matches with conversationId in the xml and validation status is 'Rejected'" in {

      val request = FakeRequest("", "").withHeaders(headers).withBody(rejectedXml)

      val testAction: Future[Result] = action.invokeBlock(request, response)
      status(testAction) mustBe OK
    }

    "must return BadRequest when 'x-conversation-id' is missing in the request header" in {
      val request                = FakeRequest("", "").withHeaders(FakeHeaders(Seq.empty)).withBody(acceptedXml)
      val result: Future[Result] = action.invokeBlock(request, response)

      status(result) mustBe BAD_REQUEST
    }

    "must return BadRequest when 'x-conversation-id' in the request header does not match the conversationId in the xml" in {

      val request                = FakeRequest("", "").withHeaders(FakeHeaders(Seq("x-conversation-id" -> "uuid"))).withBody(acceptedXml)
      val result: Future[Result] = action.invokeBlock(request, response)

      status(result) mustBe BAD_REQUEST
    }

    "must return BadRequest when the request xml is invalid xml" in {
      val invalidXml             = <test>data</test>
      val request                = FakeRequest("", "").withHeaders(FakeHeaders(Seq("x-conversation-id" -> uuid.toString))).withBody(invalidXml)
      val result: Future[Result] = action.invokeBlock(request, response)

      status(result) mustBe BAD_REQUEST
    }

    "must return BadRequest when the request xml is not an XML" in {

      val response: Request[String] => Future[Result] = { _ =>
        Future.successful(Ok(HtmlFormat.empty))
      }
      val request                = FakeRequest("", "").withHeaders(headers).withBody("test")
      val result: Future[Result] = action.invokeBlock(request, response)
      status(result) mustBe BAD_REQUEST
    }
  }
}
