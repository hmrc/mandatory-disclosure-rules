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

import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import controllers.auth.EISRequest
import models.xml.BREResponse
import play.api.Logging
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ActionRefiner, Request, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class EISResponsePreConditionCheckActionRefiner @Inject() (implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, EISRequest]
    with Logging {

  override protected def refine[A](request: Request[A]): Future[Either[Result, EISRequest[A]]] =
    request.body match {
      case xml: NodeSeq =>
        request.headers.get("x-conversation-id") match {
          case Some(conversationId) =>
            Future.successful(readXmlAsBREResponse(request, xml, conversationId))
          case None =>
            logger.info(s"x-conversation-id is missing in the request header")
            Future.successful(Left(BadRequest("x-conversation-id is missing in the request header")))
        }
      case invalidBody =>
        logger.info(s"Invalid request body, Expected XML but found: ${invalidBody.getClass}")
        Future.successful(Left(BadRequest("request body must be an XML")))
    }

  private def readXmlAsBREResponse[A](request: Request[A], xml: NodeSeq, conversationId: String): Either[Result, EISRequest[A]] =
    XmlReader.of[BREResponse].read(xml) match {

      case ParseSuccess(breResponse: BREResponse) if conversationId.equalsIgnoreCase(breResponse.conversationID) =>
        Right(EISRequest(request, breResponse))

      case ParseSuccess(breResponse: BREResponse) =>
        logger.info(s"x-conversation-id in request header: $conversationId does not match with conversationID: ${breResponse.conversationID} in the xml")
        Left(BadRequest(s"Request header 'x-conversation-id' does not match with xml conversationId"))

      case PartialParseSuccess(_, errors) =>
        logger.info(s"failed to read the xml from EIS: $errors")
        Left(BadRequest(s"Failed to read the xml from EIS: $errors"))

      case ParseFailure(errors) =>
        logger.info(s"ParseFailure:failed to read the xml from EIS: $errors")
        Left(BadRequest(s"failed to read the xml from EIS with errors: $errors"))
    }

}
