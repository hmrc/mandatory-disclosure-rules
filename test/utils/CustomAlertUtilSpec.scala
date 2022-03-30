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
import models.xml.FileErrorCode.{FailedSchemaValidation, MessageRefIDHasAlreadyBeenUsed, UnknownFileErrorCode}
import models.xml.RecordErrorCode.{DocRefIDFormat, MissingCorrDocRefId, UnknownRecordErrorCode}
import models.xml.{FileErrors, RecordError, ValidationErrors}
import play.api.Logger

class CustomAlertUtilSpec extends SpecBase {

  "alertForProblemStatus" - {

    val loggerMessage = "File Rejected with unexpected error"

    s"should return logger warning with message '$loggerMessage'" - {

      "when an 'unknown file' error occurs" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(None, Some(Seq(RecordError(UnknownRecordErrorCode("12345"), None, None))))

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when an 'unknown record' error occurs" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(Some(Seq(FileErrors(UnknownFileErrorCode("12345"), None))), None)

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when a 'problem' error occurs" in {
        val mockLogger = mock[Logger]

      val errors =
        ValidationErrors(Some(Seq(FileErrors(MessageRefIDHasAlreadyBeenUsed, None))), Some(Seq(RecordError(MissingCorrDocRefId, None, None))))

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }
    }
  }

}
