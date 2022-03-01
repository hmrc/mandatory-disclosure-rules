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

import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import controllers.actions.EISResponsePreConditionCheckActionFilter
import controllers.auth.IdentifierAuthAction
import models.xml.BREResponse
import play.api.Logging
import play.api.mvc.{Action, ControllerComponents, Result}
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class EISResponseController @Inject() (cc: ControllerComponents,
                                       authenticate: IdentifierAuthAction,
                                       actionFilter: EISResponsePreConditionCheckActionFilter,
                                       fileDetailsRepository: FileDetailsRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def processEISResponse(): Action[NodeSeq] = (authenticate(parse.xml) andThen actionFilter).async { implicit request =>
    XmlReader.of[BREResponse].read(request.body) match {
      case ParseSuccess(breResponse: BREResponse) =>
        Future.successful(Ok)
      case PartialParseSuccess(_, errors) =>
        logger.info(s"failed to read the xml from EIS with errors: $errors")
        Future.successful(BadRequest("Failed to read the xml from EIS to read the input xml $errors"))
      case ParseFailure(errors) =>
        logger.info(s"ParseFailure:failed to read the xml from EIS with errors: $errors")
        Future.successful(BadRequest(s"failed to read the xml from EIS with errors: $errors"))
    }
  }
}
