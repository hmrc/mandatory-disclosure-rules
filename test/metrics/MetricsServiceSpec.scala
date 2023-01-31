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

package metrics

import base.SpecBase
import com.kenshoo.play.metrics.MetricsImpl
import models.submission.{Accepted, Rejected}
import models.xml.ValidationErrors
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import play.api.test.Injecting

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MetricsServiceSpec extends SpecBase with Injecting with BeforeAndAfterEach {
  val metrics: MetricsImpl           = inject[MetricsImpl]
  val metricsService: MetricsService = new MetricsService(metrics)

  "MetricsService" - {
    "increment counter file Status accepted counter" in {
      val acceptedCounter = metricsService.getFileStatusCounter(Accepted)
      acceptedCounter.inc()
      acceptedCounter.getCount mustBe 1
      acceptedCounter.dec()
    }

    "increment counter file Status rejected counter" in {
      val rejectedCounter = metricsService.getFileStatusCounter(Rejected(ValidationErrors(None, None)))
      rejectedCounter.inc()
      rejectedCounter.getCount mustBe 1
      rejectedCounter.dec()
    }

    "increment counter file Status pending counter" in {
      val pendingCounter = metricsService.fileStatusPendingCounter
      pendingCounter.inc()
      pendingCounter.getCount mustBe 1

    }

    "processFileStatus" in {
      val status = Gen.oneOf(Seq(Accepted, Rejected(ValidationErrors(None, None)))).sample.value
      metricsService.processFileStatusMetrics(status)(Future(Some("test"))).map(_ => metricsService.getFileStatusCounter(status).getCount mustBe 1)

    }

  }

}
