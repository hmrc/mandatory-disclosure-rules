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

package repositories.submission
import models.submission.{FileStatus, SubmissionDetails}
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
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class SubmissionRepository @Inject() (
  val mongo: MongoComponent,
  config: Configuration
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[SubmissionDetails](
      mongoComponent = mongo,
      collectionName = "submission-details",
      domainFormat = SubmissionDetails.format,
      indexes = SubmissionRepository.indexes(config),
      replaceIndexes = true
    ) {

  def updateStatus(
    conversationId: String,
    newStatus: FileStatus
  ): Future[Boolean] = {

    val filter: Bson   = equal("_id", conversationId)
    val modifier: Bson = set("status", Codecs.toBson(newStatus))
    val options: FindOneAndUpdateOptions =
      FindOneAndUpdateOptions().upsert(true)

    collection
      .findOneAndUpdate(filter, modifier, options)
      .toFuture
      .map(_ => true)
  }

  def findByConversationId(conversationId: String): Future[Seq[SubmissionDetails]] = {
    val filter: Bson = equal("_id", conversationId)
    collection
      .find(filter)
      .toFuture
  }

  def findBySubscriptionId(subscriptionId: String): Future[Seq[SubmissionDetails]] = {
    val filter: Bson = equal("subscriptionId", subscriptionId)
    collection
      .find(filter)
      .toFuture
  }

  def insert(submissionDetails: SubmissionDetails): Future[Boolean] =
    collection
      .insertOne(submissionDetails)
      .toFuture
      .map(_ => true)

}

object SubmissionRepository {

  def cacheTtl(config: Configuration): Long =
    Duration(config.get[Int]("mongodb.submission.timeToLiveInDays"), "days").toSeconds

  def indexes(config: Configuration) = Seq(
    IndexModel(
      ascending("lastUpdated"),
      IndexOptions()
        .name("submission-last-updated-index")
        .expireAfter(cacheTtl(config), TimeUnit.SECONDS)
    ),
    IndexModel(ascending("subscriptionId"),
               IndexOptions()
                 .name("subscriptionId-index")
                 .unique(false)
    )
  )
}
