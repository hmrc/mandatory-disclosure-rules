/*
 * Copyright 2025 HM Revenue & Customs
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

import config.AppConfig
import metrics.MetricsService
import models.submission.{ConversationId, FileDetails, FileStatus, Pending}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.set
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileDetailsRepository @Inject() (
  val mongo: MongoComponent,
  appConfig: AppConfig,
  metricsService: MetricsService
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[FileDetails](
      mongoComponent = mongo,
      collectionName = "file-details",
      domainFormat = FileDetails.format,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions()
            .name("submission-last-updated-index")
            .expireAfter(appConfig.submissionTtl.toLong, TimeUnit.DAYS)
        ),
        IndexModel(ascending("subscriptionId"),
                   IndexOptions()
                     .name("subscriptionId-index")
                     .unique(false)
        ),
        IndexModel(ascending("status"),
                   IndexOptions()
                     .name("status-index")
                     .unique(false)
        )
      ),
      replaceIndexes = true
    ) {

  def updateStatus(
    conversationId: String,
    newStatus: FileStatus
  ): Future[Option[FileDetails]] = {

    val filter: Bson = equal("_id", conversationId)
    val modifier     = Updates.combine(
      set("status", Codecs.toBson(newStatus)),
      set("lastUpdated", LocalDateTime.now)
    )
    val options: FindOneAndUpdateOptions =
      FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)

    metricsService.processFileStatusMetrics(newStatus) {
      collection
        .findOneAndUpdate(filter, modifier, options)
        .toFutureOption()
    }
  }

  def findByConversationId(conversationId: ConversationId): Future[Option[FileDetails]] = {
    val filter: Bson = equal("_id", conversationId.value)
    collection
      .find(filter)
      .first()
      .toFutureOption()
  }

  def findStatusByConversationId(conversationId: ConversationId): Future[Option[FileStatus]] = {
    val filter: Bson = equal("_id", conversationId.value)
    collection
      .find(filter)
      .first()
      .toFutureOption()
      .map(_.map(_.status))
  }

  def findBySubscriptionId(subscriptionId: String): Future[Seq[FileDetails]] = {
    val filter: Bson = equal("subscriptionId", subscriptionId)
    collection
      .find(filter)
      .toFuture()
  }

  def insert(fileDetails: FileDetails): Future[Boolean] =
    collection
      .insertOne(fileDetails)
      .toFuture()
      .map { _ =>
        metricsService.fileStatusPendingCounter.inc()
        true
      }

  def findStaleSubmissions(): Future[Seq[FileDetails]] = {
    val filter: Bson = and(
      equal("status", Codecs.toBson(Pending.asInstanceOf[FileStatus])),
      lt("submitted", LocalDateTime.now().minusSeconds(appConfig.staleTaskAlertAfter.toSeconds))
    )

    collection.find(filter).toFuture()
  }

}
