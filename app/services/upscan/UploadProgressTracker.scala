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
import org.bson.types.ObjectId
import play.api.Logging
import repositories.upscan.UpScanSessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait UploadProgressTracker {

  def requestUpload(
    uploadId: UploadId,
    fileReference: Reference
  ): Future[Boolean]

  def registerUploadResult(
    reference: Reference,
    uploadStatus: UploadStatus
  ): Future[Boolean]

  def getUploadResult(id: UploadId): Future[Option[UploadStatus]]

}

class MongoBackedUploadProgressTracker @Inject() (
  repository: UpScanSessionRepository
)(implicit ec: ExecutionContext)
    extends UploadProgressTracker
    with Logging {

  override def requestUpload(
    uploadId: UploadId,
    fileReference: Reference
  ): Future[Boolean] =
    repository.insert(
      UploadSessionDetails(ObjectId.get(), uploadId, fileReference, InProgress)
    )

  override def registerUploadResult(
    fileReference: Reference,
    uploadStatus: UploadStatus
  ): Future[Boolean] =
    repository.updateStatus(fileReference, uploadStatus)

  override def getUploadResult(id: UploadId): Future[Option[UploadStatus]] =
    for (result <- repository.findByUploadId(id)) yield result map { x =>
      x.status
    }

}
