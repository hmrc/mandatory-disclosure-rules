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

import base.SpecBase
import models.xml.FileErrorCode.{FailedSchemaValidation, MessageRefIDHasAlreadyBeenUsed}
import models.xml.RecordErrorCode.{DocRefIDFormat, MissingCorrDocRefId}
import models.xml.{FileErrors, RecordError, ValidationErrors}
import play.api.Logger

class CustomAlertUtilSpec extends SpecBase {

  "alertForProblemStatus" - {

    "should return true when error messages have a 'problem' error" in {
      val mockLogger = mock[Logger]

      when(mockLogger.isWarnEnabled).thenReturn(true)
      val errors = ValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(DocRefIDFormat, None, None))))

      val alertUtil = new CustomAlertUtil {
        override val logger: Logger = mockLogger
      }

      alertUtil.alertForProblemStatus(errors)
      verify(mockLogger).warn("a CDAX error has been invoked")
    }

    "should return a when error messages have a 'problem' error" in {
      val mockLogger = mock[Logger]
      when(mockLogger.isInfoEnabled).thenReturn(true)

      val errors =
        ValidationErrors(Some(Seq(FileErrors(MessageRefIDHasAlreadyBeenUsed, None))), Some(Seq(RecordError(MissingCorrDocRefId, None, None))))

      val alertUtil = new CustomAlertUtil {
        override val logger: Logger = mockLogger
      }

      alertUtil.alertForProblemStatus(errors)
      verify(mockLogger).info("File Rejected with business rule errors")
    }
  }

}
