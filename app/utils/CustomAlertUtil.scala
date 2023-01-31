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

package utils

import models.xml.FileErrorCode.{fileErrorCodesForProblemStatus, CustomError => FileCustomError}
import models.xml.RecordErrorCode.{CustomError, DocRefIDFormat}
import models.xml.{FileErrorCode, FileErrors, RecordError, RecordErrorCode, ValidationErrors}
import play.api.Logging
import utils.ErrorDetails.{errorList, error_details_910}

class CustomAlertUtil extends Logging {

  private val expectedErrorCodes: Seq[String]       = FileErrorCode.values.map(_.code) ++ RecordErrorCode.values.map(_.code)
  private val problemsStatusErrorCodes: Seq[String] = fileErrorCodesForProblemStatus.map(_.code).:+(DocRefIDFormat.code)

  def alertForProblemStatus(errors: ValidationErrors): Unit = {
    val errorCodes = Seq(errors.fileError.map(_.map(_.code.code)).getOrElse(Nil), errors.recordError.map(_.map(_.code.code)).getOrElse(Nil)).flatten
    if (
      errorCodes.exists(
        !expectedErrorCodes.contains(_)
          || errorCodes.exists(problemsStatusErrorCodes.contains(_))
          || recordErrorDetailNotAllowed(errors.recordError)
          || fileErrorDetailNotAllowed(errors.fileError)
      )
    ) {
      logger.warn("File Rejected with unexpected error")
    }
  }

  private def recordErrorDetailNotAllowed(errors: Option[Seq[RecordError]]): Boolean =
    errors.exists(
      _.exists(error => error.code == CustomError && !errorList.exists(error.details.getOrElse("").contains(_)))
    )

  private def fileErrorDetailNotAllowed(errors: Option[Seq[FileErrors]]): Boolean =
    errors.exists(
      _.exists(error => error.code == FileCustomError && !error.details.getOrElse("").contains(error_details_910))
    )

}
