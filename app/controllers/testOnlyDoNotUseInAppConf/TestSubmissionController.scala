/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.testOnlyDoNotUseInAppConf

import controllers.auth.IdentifierAuthAction
import models.upscan.UploadId
import play.api.Logging
import play.api.mvc.{Action, ControllerComponents}
import services.DataExtraction
import services.submission.SubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import javax.inject.Inject
import scala.xml.{Elem, NodeSeq}

class TestSubmissionController @Inject() (
  authenticate: IdentifierAuthAction,
  cc: ControllerComponents,
  dataExtraction: DataExtraction,
  submissionService: SubmissionService
)() extends BackendController(cc)
    with Logging {

  def submitDisclosureXML: Action[NodeSeq] = authenticate.async(parse.xml) { implicit request =>
    val xml      = request.body
    val fileName = (xml \ "fileName").text.trim
    val fileSize = (xml \ "fileSize").text.trim.toLong
    val uploadId = UploadId(UUID.randomUUID().toString)

    val msd = dataExtraction.messageSpecData(xml.asInstanceOf[Elem])

    submissionService.processSubmission(xml, uploadId, request.subscriptionId, fileName, fileSize, msd.get)
  }
}
