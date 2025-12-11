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
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import models.xml.FileErrorCode.{CustomError as FileCustomError, FailedSchemaValidation, UnknownFileErrorCode}
import models.xml.RecordErrorCode.{CustomError, DocRefIDFormat, UnknownRecordErrorCode}
import models.xml.{FileErrors, RecordError, ValidationErrors}
import org.mockito.Mockito.*
import play.api.Logger
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.ErrorDetails.{error_details_901, error_details_910}

import scala.reflect.ClassTag

class CustomAlertUtilSpec extends SpecBase with LogCapturing {

  def withCaptureOfLoggingFrom[T](body: (=> List[ILoggingEvent]) => Unit)(implicit classTag: ClassTag[T]): Unit =
    withCaptureOfLoggingFrom(Logger(classTag.runtimeClass))(body)

  "alertForProblemStatus" - {

    val loggerMessage = "File Rejected with unexpected error"

    s"should return logger warning with message '$loggerMessage'" - {

      "when an 'unknown file' error occurs" in {
        val errors = ValidationErrors(None, Some(Seq(RecordError(UnknownRecordErrorCode("12345"), None, None))))

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when an 'unknown record' error occurs" in {
        val errors = ValidationErrors(Some(Seq(FileErrors(UnknownFileErrorCode("12345"), None))), None)

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when a 'problem' error occurs" in {
        val errors = ValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(DocRefIDFormat, None, None))))

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when a 'problem' error occurs for unsupported ErrorDetails for CustomError" in {
        val errors = ValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(CustomError, Some("something"), None))))

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when a 'problem' error occurs for CustomError with ErrorDetails 'None'" in {
        val errors = ValidationErrors(None, Some(Seq(RecordError(CustomError, None, None))))

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when a 'Rejected' error occurs for supported ErrorDetails" in {
        val errors = ValidationErrors(None, Some(Seq(RecordError(CustomError, Some(error_details_901), None))))

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)
          logs.isEmpty mustBe true
        }
      }

      "when a 'problem' error occurs for CustomError with ErrorDetails 'None' for FileError" in {
        val errors = ValidationErrors(Some(Seq(FileErrors(FileCustomError, None))), None)

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when a 'problem' error occurs for CustomError with unacceptable ErrorDetails  for FileError" in {
        val errors = ValidationErrors(Some(Seq(FileErrors(FileCustomError, Some("something")))), None)

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)

          logs.exists { event =>
            event.getLevel == Level.WARN &&
            event.getMessage.contains("File Rejected with unexpected error")
          } mustBe true
        }
      }

      "when a 'Rejected' error occurs for supported ErrorDetails for FileError" in {
        val errors = ValidationErrors(Some(Seq(FileErrors(FileCustomError, Some(error_details_910)))), None)

        withCaptureOfLoggingFrom[CustomAlertUtil] { logs =>
          new CustomAlertUtil().alertForProblemStatus(errors)
          logs.isEmpty mustBe true
        }
      }
    }
  }

}
