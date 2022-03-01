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

import controllers.auth.UserRequest
import play.api.Logging
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ActionFilter, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class EISResponsePreConditionCheckActionFilter @Inject() (implicit val executionContext: ExecutionContext) extends ActionFilter[UserRequest] with Logging {

  override protected def filter[A](request: UserRequest[A]): Future[Option[Result]] =
    request.request.body match {
      case xml: NodeSeq =>
        val xmlConversationId: String = (xml \\ "conversationID").text

        (request.request.headers.get("x-conversation-id"), xmlConversationId.nonEmpty) match {
          case (Some(conversationId), true) if xmlConversationId.equalsIgnoreCase(conversationId) =>
            Future.successful(None)

          case (Some(conversationId), true) =>
            logger.info(s"x-conversation-id in request header: $conversationId does not match with conversationID: $xmlConversationId in the xml")
            Future.successful(Some(BadRequest(s"Request header 'x-conversation-id' does not match with xml conversationId")))

          case (Some(_), false) =>
            logger.info(s"conversationId missing in the EIS xml")
            Future.successful(Some(BadRequest("conversationId missing in the EIS xml")))

          case (None, _) =>
            logger.info(s"x-conversation-id is missing in the request header")
            Future.successful(Some(BadRequest("x-conversation-id is missing in the request header")))
        }

      case invalidBody =>
        logger.info(s"Invalid request body, Expected XML but found: ${invalidBody.getClass}")
        Future.successful(Some(BadRequest("request body must be an XML")))
    }

}
