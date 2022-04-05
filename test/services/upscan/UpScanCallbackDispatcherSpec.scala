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

package services.upscan

import base.SpecBase
import models.upscan.{ErrorDetails, FailedCallbackBody, Quarantined, ReadyCallbackBody, Reference, UploadDetails, UploadRejected, UploadedSuccessfully}
import play.api.Application
import play.api.inject.bind
import models.upscan.Failed
import java.time.Instant
import scala.concurrent.Future

class UpScanCallbackDispatcherSpec extends SpecBase {

  val mockUploadProgressTracker: UploadProgressTracker =
    mock[UploadProgressTracker]

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[UploadProgressTracker].toInstance(mockUploadProgressTracker)
      )
      .build()

  "UpscanCallbackDispatcher" - {

    "handleCallback must return UploadedSuccessfully for the input ReadyCallbackBody" in {
      val reference = Reference("ref")
      val uploadDetails = UploadDetails(
        Instant.now(),
        "1234",
        "application/xml",
        "test.xml",
        1000
      )

      val readyCallbackBody = ReadyCallbackBody(
        reference,
        "downloadUrl",
        UploadDetails(
          Instant.now(),
          "1234",
          "application/xml",
          "test.xml",
          1000
        )
      )

      val uploadStatus = UploadedSuccessfully(
        uploadDetails.fileName,
        uploadDetails.fileMimeType,
        readyCallbackBody.downloadUrl,
        Some(uploadDetails.size)
      )

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true

    }

    "handleCallback must return Quarantined for the input FailedCallbackBody" in {
      val reference    = Reference("ref")
      val errorDetails = ErrorDetails("QUARANTINE", "message")

      val readyCallbackBody = FailedCallbackBody(reference, errorDetails)

      val uploadStatus = Quarantined

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true

    }

    "handleCallback must return REJECTED for the input FailedCallbackBody" in {
      val reference    = Reference("ref")
      val errorDetails = ErrorDetails("REJECTED", "message")

      val readyCallbackBody = FailedCallbackBody(reference, errorDetails)

      val uploadStatus = UploadRejected(readyCallbackBody.failureDetails)

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true

    }

    "handleCallback must return Failed for the input FailedCallbackBody" in {
      val reference    = Reference("ref")
      val errorDetails = ErrorDetails("Failed", "message")

      val readyCallbackBody = FailedCallbackBody(reference, errorDetails)

      val uploadStatus = Failed

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true

    }

  }
}
