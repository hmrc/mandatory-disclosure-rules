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
import models.submission.{ConversationId, FileDetails, Pending}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty, GET}
import repositories.submission.FileDetailsRepository

import java.time.LocalDateTime
import scala.concurrent.Future

class FileDetailsControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockFileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]

  override def beforeEach(): Unit = reset(mockFileDetailsRepository)

  val application: Application = applicationBuilder()
    .overrides(
      bind[FileDetailsRepository].toInstance(mockFileDetailsRepository),
      bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
    )
    .build()

  "FileDetailsController" - {
    "must return FileDetails for the input 'conversationId'" in {
      val conversationId = ConversationId()
      val fileDetails = FileDetails(
        conversationId,
        subscriptionId = "subscriptionId",
        messageRefId = "messageRefId",
        status = Pending,
        fileName = "test.xml",
        submitted = LocalDateTime.now(),
        lastUpdated = LocalDateTime.now()
      )

      when(mockFileDetailsRepository.findByConversationId(any[String]())).thenReturn(Future.successful(Some(fileDetails)))

      val request =
        FakeRequest(GET, routes.FileDetailsController.getFileDetails(conversationId).url)

      val result = route(application, request).value
      status(result) mustBe OK
    }

    "must return NotFound status when no record found for the input 'conversationId'" in {

      when(mockFileDetailsRepository.findByConversationId(any[String]())).thenReturn(Future.successful(None))

      val request =
        FakeRequest(GET, routes.FileDetailsController.getFileDetails(ConversationId()).url)

      val result = route(application, request).value
      status(result) mustBe NOT_FOUND
    }
  }
}
