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

package repositories.upscan

import models.upscan._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions}
import play.api.Configuration
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

object UploadSessionRepository {

  def cacheTtl(config: Configuration): Int =
    config.get[Int]("mongodb.timeToLiveInSeconds")

  def indexes(config: Configuration) = Seq(
    IndexModel(
      ascending("lastUpdated"),
      IndexOptions()
        .name("upload-last-updated-index")
        .expireAfter(cacheTtl(config), TimeUnit.SECONDS)
    )
  )
}

class UploadSessionRepository @Inject() (
  val mongo: MongoComponent,
  config: Configuration
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UploadSessionDetails](
      mongoComponent = mongo,
      collectionName = "uploadSessionRepository",
      domainFormat = UploadSessionDetails.format,
      indexes = UploadSessionRepository.indexes(config),
      replaceIndexes = true
    ) {

  def findByUploadId(uploadId: UploadId): Future[Option[UploadSessionDetails]] =
    collection
      .find(equal("uploadId", Codecs.toBson(uploadId)))
      .first()
      .toFutureOption()

  def updateStatus(
    reference: Reference,
    newStatus: UploadStatus
  ): Future[Boolean] = {

    val filter: Bson   = equal("reference.value", Codecs.toBson(reference.value))
    val modifier: Bson = set("status", Codecs.toBson(newStatus))
    val options: FindOneAndUpdateOptions =
      FindOneAndUpdateOptions().upsert(true)

    collection
      .findOneAndUpdate(filter, modifier, options)
      .toFuture
      .map(_ => true)
  }

  def insert(uploadDetails: UploadSessionDetails): Future[Boolean] =
    collection
      .insertOne(uploadDetails)
      .toFuture
      .map(_ => true)

}
