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

import base.SpecBase
import models.xml.FileErrorCode.{FailedSchemaValidation, UnknownFileErrorCode}
import models.xml.RecordErrorCode.{CustomError, DocRefIDFormat, UnknownRecordErrorCode}
import models.xml.FileErrorCode.{CustomError => FileCustomError}
import models.xml.{FileErrors, RecordError, ValidationErrors}
import play.api.Logger
import utils.ErrorDetails.{error_details_901, error_details_910}

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

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(DocRefIDFormat, None, None))))

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when a 'problem' error occurs for unsupported ErrorDetails for CustomError" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(CustomError, Some("something"), None))))

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when a 'problem' error occurs for CustomError with ErrorDetails 'None'" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(None, Some(Seq(RecordError(CustomError, None, None))))

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when a 'Rejected' error occurs for supported ErrorDetails" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(None, Some(Seq(RecordError(CustomError, Some(error_details_901), None))))

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, never).warn(loggerMessage)
      }

      "when a 'problem' error occurs for CustomError with ErrorDetails 'None' for FileError" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(Some(Seq(FileErrors(FileCustomError, None))), None)

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when a 'problem' error occurs for CustomError with unacceptable ErrorDetails  for FileError" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(Some(Seq(FileErrors(FileCustomError, Some("something")))), None)

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, times(1)).warn(loggerMessage)
      }

      "when a 'Rejected' error occurs for supported ErrorDetails for FileError" in {
        val mockLogger = mock[Logger]

        when(mockLogger.isWarnEnabled).thenReturn(true)
        val errors = ValidationErrors(Some(Seq(FileErrors(FileCustomError, Some(error_details_910)))), None)

        val alertUtil = new CustomAlertUtil {
          override val logger: Logger = mockLogger
        }

        alertUtil.alertForProblemStatus(errors)
        verify(mockLogger, never).warn(loggerMessage)
      }
    }
  }

}
