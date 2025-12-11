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

package controllers.actions

import config.AppConfig
import controllers.auth.EISRequest
import models.xml.{BREResponse, XmlReads}
import play.api.Logging
import play.api.libs.json.{JsError, JsResult, JsSuccess}
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ActionRefiner, Request, Result}
import services.validation.XMLValidationService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class EISResponsePreConditionCheckActionRefiner @Inject() (validationService: XMLValidationService, appConfig: AppConfig)(implicit
  val executionContext: ExecutionContext
) extends ActionRefiner[Request, EISRequest]
    with Logging {

  override protected def refine[A](request: Request[A]): Future[Either[Result, EISRequest[A]]] =
    request.body match {
      case xml: NodeSeq =>
        request.headers.get("x-conversation-id") match {
          case Some(conversationId) =>
            validationService.validate(xml, appConfig.eisResponseXSDFilePath) match {
              case Right(xml)   => Future.successful(readXmlAsBREResponse(request, xml, conversationId))
              case Left(errors) =>
                logger.warn(s"XML parsing failed with error: $errors")
                Future.successful(Left(BadRequest))
            }
          case None =>
            logger.warn(s"x-conversation-id is missing in the request header")
            Future.successful(Left(BadRequest("x-conversation-id is missing in the request header")))
        }
      case invalidBody =>
        logger.warn(s"Invalid request body, Expected XML but found: ${invalidBody.getClass}")
        Future.successful(Left(BadRequest("request body must be an XML")))
    }

  private def readXmlAsBREResponse[A](request: Request[A], xml: NodeSeq, conversationId: String)(implicit
    xmlReads: XmlReads[BREResponse]
  ): Either[Result, EISRequest[A]] = {
    val result: JsResult[BREResponse] = xmlReads.reads(xml)

    result match {
      case JsSuccess(breResponse, _) if conversationId.equalsIgnoreCase(breResponse.conversationID.trim) => Right(EISRequest(request, breResponse))

      case JsSuccess(breResponse, _) =>
        logger.warn(s"x-conversation-id in request header: $conversationId does not match with conversationID: ${breResponse.conversationID} in the xml")
        Left(BadRequest(s"Request header 'x-conversation-id' does not match with xml conversationId"))

      case JsError(errors) =>
        logger.warn(s"ParseFailure: failed to read the xml from EIS: $errors")
        Left(BadRequest(s"failed to read the xml from EIS with errors: $errors"))
    }
  }

}
