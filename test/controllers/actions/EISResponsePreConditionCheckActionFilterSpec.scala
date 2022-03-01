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
import controllers.auth.{IdentifierAuthAction, UserRequest}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.HtmlFormat

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class EISResponsePreConditionCheckActionFilterSpec extends SpecBase with BeforeAndAfterEach {

  val uuid: UUID           = UUID.randomUUID()
  val headers: FakeHeaders = FakeHeaders(Seq("x-conversation-id" -> uuid.toString))

  val xml: NodeSeq = <BREResponse>
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47Z</receiptDate>
      <regime>MDR</regime>
      <conversationID>{uuid}</conversationID>
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

  class Harness(authAction: IdentifierAuthAction, filter: EISResponsePreConditionCheckActionFilter) extends InjectedController {

    def onPageLoad(): Action[AnyContent] = (authAction andThen filter) { _ =>
      Ok
    }
  }

  private lazy val action = new EISResponsePreConditionCheckActionFilter()

  private val response: Request[NodeSeq] => Future[Result] = { _ =>
    Future.successful(Ok(HtmlFormat.empty))
  }

  "EISResponsePreConditionCheckActionFilter" - {

    "must return Ok when 'x-conversation-id' matches with conversationId in the xml" in {

      val request = UserRequest("", FakeRequest("", "").withHeaders(headers).withBody(xml))

      val testAction: Future[Result] = action.invokeBlock(request, response)

      status(testAction) mustBe OK
    }

    "must return BadRequest when 'x-conversation-id' is missing in the request header" in {
      val request                = UserRequest("", FakeRequest("", "").withHeaders(FakeHeaders(Seq.empty)).withBody(xml))
      val result: Future[Result] = action.invokeBlock(request, response)

      status(result) mustBe BAD_REQUEST
    }

    "must return BadRequest when 'x-conversation-id' in the request header does not match the conversationId in the xml" in {

      val request                = UserRequest("", FakeRequest("", "").withHeaders(FakeHeaders(Seq("x-conversation-id" -> "uuid"))).withBody(xml))
      val result: Future[Result] = action.invokeBlock(request, response)

      status(result) mustBe BAD_REQUEST
    }

    "must return BadRequest when the request xml is invalid xml" in {
      val invalidXml             = <test>data</test>
      val request                = UserRequest("", FakeRequest("", "").withHeaders(FakeHeaders(Seq("x-conversation-id" -> "uuid"))).withBody(invalidXml))
      val result: Future[Result] = action.invokeBlock(request, response)

      status(result) mustBe BAD_REQUEST
    }

    "must return BadRequest when the request xml is not an XML" in {

      val response: Request[String] => Future[Result] = { _ =>
        Future.successful(Ok(HtmlFormat.empty))
      }
      val request                = UserRequest("", FakeRequest("", "").withHeaders(headers).withBody("test"))
      val result: Future[Result] = action.invokeBlock(request, response)
      status(result) mustBe BAD_REQUEST
    }
  }
}
