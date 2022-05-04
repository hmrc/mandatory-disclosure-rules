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

package metrics

import com.codahale.metrics.Counter
import com.kenshoo.play.metrics.Metrics
import models.submission.{Accepted, FileStatus, Rejected}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MetricsService @Inject() (metrics: Metrics) {

  val fileStatusAcceptedCounter: Counter = getCounter("file-status-accepted-counter")
  val fileStatusPendingCounter: Counter  = getCounter("file-status-pending-counter")
  val fileStatusRejectedCounter: Counter = getCounter("file-status-rejected-counter")
  val failureCounter: Counter            = getCounter("failure-counter")

  private def getCounter(counterName: String): Counter =
    metrics.defaultRegistry.counter(counterName)

  def getFileStatusCounter(newStatus: FileStatus): Counter =
    newStatus match {
      case Accepted    => fileStatusAcceptedCounter
      case Rejected(_) => fileStatusRejectedCounter
      case _           => failureCounter
    }

  def processFileStatusMetrics[T](fileStatus: FileStatus)(block: => Future[T])(implicit ec: ExecutionContext): Future[T] =
    block map { result =>
      getFileStatusCounter(fileStatus).inc()
      result
    } recover { case e =>
      failureCounter.inc()
      throw e
    }
}
