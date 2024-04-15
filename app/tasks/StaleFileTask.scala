/*
 * Copyright 2024 HM Revenue & Customs
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

package tasks

import akka.actor.ActorSystem
import config.AppConfig
import play.api.Logging
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.mongo.TimestampSupport
import uk.gov.hmrc.mongo.lock.{MongoLockRepository, ScheduledLockService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StaleFileTask @Inject() (actorSystem: ActorSystem,
                               repository: FileDetailsRepository,
                               config: AppConfig,
                               mongoLockRepository: MongoLockRepository,
                               timestampSupport: TimestampSupport
)(implicit
  executionContext: ExecutionContext
) extends Logging {

  private val interval = config.staleTaskInterval

  val lockService =
    ScheduledLockService(
      lockRepository = mongoLockRepository,
      lockId = "stale-file-task",
      timestampSupport = timestampSupport,
      schedulerInterval = interval
    )

  actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = 0.microseconds, interval) { () =>
    lockService
      .withLock {
        Future {
          repository.findStaleSubmissions().map(_.map(file => logger.warn(s"Stale file found - conversationId: ${file._id}, filename: ${file.name}")))
        }
      }
//      .map {
  //        case Some(res) => logger.debug(s"Finished with $res. Lock has been released.")
  //        case None      => logger.debug("Failed to take lock")
  //      }

  }
}
