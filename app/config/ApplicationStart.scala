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

package config
import org.mongodb.scala.MongoClient
import play.api.Logging

import javax.inject._
import scala.concurrent.ExecutionContext

//TODO: Remove this whole class after a successful production deployment

@Singleton
class ApplicationStart @Inject() (appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def dropMongoCollection(db: String, collection: String): Unit = {

    val mongoClient: MongoClient = MongoClient(appConfig.mongodbUri)

    val dropCollection = mongoClient
      .getDatabase(db)
      .getCollection(collection)
      .drop()
      .head()
    logger.info(s"Attempting to drop $collection...")
    dropCollection
      .map { res =>
        logger.info(s"Dropping of $collection complete: $res")
      }
      .recover { case e: Exception =>
        logger.error(s"Error dropping $collection", e)
      }
      .map { _ =>
        logger.info(s"Closing connection for $db...")
        mongoClient.close()
      }
  }

  if (appConfig.dropUpscanSessionCollection) {
    dropMongoCollection("mandatory-disclosure-rules", "upScanSessionRepository")
  }
}
