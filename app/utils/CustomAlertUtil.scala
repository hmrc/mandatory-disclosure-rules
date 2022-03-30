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

package utils

import models.xml.FileErrorCode.fileErrorCodesForProblemStatus
import models.xml.RecordErrorCode.DocRefIDFormat
import models.xml.ValidationErrors
import play.api.Logging

class CustomAlertUtil extends Logging {

  private val problemsStatusErrorCodes: Seq[String] = fileErrorCodesForProblemStatus.map(_.code).:+(DocRefIDFormat.code)

  def alertForProblemStatus(errors: ValidationErrors): Unit = {
    val codes: Seq[String] = Seq(errors.fileError.map(_.map(_.code.code)).getOrElse(Nil), errors.recordError.map(_.map(_.code.code)).getOrElse(Nil)).flatten
    if (codes.exists(problemsStatusErrorCodes.contains(_))) {
      logger.warn("a CDAX error has been invoked")
    } else {
      logger.info("File Rejected with business rule errors")
    }
  }

}
