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

package services.upscan

import models.upscan.*
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.Future

class UpScanCallbackDispatcher @Inject() (sessionStorage: UploadProgressTracker) extends Logging {

  def handleCallback(callback: CallbackBody): Future[Boolean] = {
    val uploadStatus = callback match {
      case s: ReadyCallbackBody =>
        UploadedSuccessfully(
          s.uploadDetails.fileName,
          s.uploadDetails.fileMimeType,
          s.downloadUrl,
          s.uploadDetails.size,
          s.uploadDetails.checksum
        )
      case s: FailedCallbackBody if s.failureDetails.failureReason == "QUARANTINE" =>
        logger.warn(s"FailedCallbackBody, QUARANTINE: $s")
        Quarantined
      case s: FailedCallbackBody if s.failureDetails.failureReason == "REJECTED" =>
        logger.warn(s"FailedCallbackBody, REJECTED: $s")
        UploadRejected(s.failureDetails)
      case f: FailedCallbackBody =>
        logger.warn(s"FailedCallbackBody: $f")
        Failed
    }
    sessionStorage.registerUploadResult(callback.reference, uploadStatus)
  }

}
