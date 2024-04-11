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

/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */

package tasks

import akka.actor.ActorSystem
import config.AppConfig
import play.api.Logging
import repositories.submission.FileDetailsRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class StaleFileTask @Inject() (actorSystem: ActorSystem, repository: FileDetailsRepository, config: AppConfig)(implicit
  executionContext: ExecutionContext
) extends Logging {
  actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = 0.microseconds, config.staleTaskInterval) { () =>
    repository.findStaleSubmissions().map(_.map(file => logger.warn(s"Stale file found - conversationId: ${file._id}, filename: ${file.name}")))
  }
}
