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
import play.api.inject.ApplicationLifecycle
import repositories.submission.FileDetailsRepository
import uk.gov.hmrc.mongo.TimestampSupport
import uk.gov.hmrc.mongo.lock.{MongoLockRepository, ScheduledLockService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class StaleFileTask @Inject() (actorSystem: ActorSystem,
                               repository: FileDetailsRepository,
                               lifecycle: ApplicationLifecycle,
                               config: AppConfig,
                               mongoLockRepository: MongoLockRepository,
                               timestampSupport: TimestampSupport
)(implicit
  executionContext: ExecutionContext
) extends Logging {

  private val enabled  = config.staleTaskEnabled
  private val interval = config.staleTaskInterval

  if (enabled) {

    val lockService = ScheduledLockService(
      lockRepository = mongoLockRepository,
      lockId = "stale-file-task",
      timestampSupport = timestampSupport,
      schedulerInterval = interval
    )

    val cancellable = actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = 1.second, interval) { () =>
      lockService
        .withLock {
          logger.info("StaleFileTask: Started")

          repository
            .findStaleSubmissions()
            .map { files =>
              files.foreach(file => logger.warn(s"StaleFileTask: Stale file found - conversationId: ${file._id.value}, filename: ${file.name}"))
              logger.info("StaleFileTask: Complete")
            }
        }
        .onComplete {
          case Success(_) => ()
          case Failure(e) => logger.warn(s"StaleFileTask: An error occurred: ${e.getMessage}", e)
        }
    }

    lifecycle.addStopHook(() => Future.successful(cancellable.cancel()))

  }

}
