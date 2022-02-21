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

package connectors

import base.{SpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import models.submission.ConversationId
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder

class SubmissionConnectorSpec extends SpecBase with GuiceOneAppPerSuite with WireMockServerHandler with ScalaFutures {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.submission.port"         -> server.port(),
      "microservice.services.submission.environment"  -> "local",
      "microservice.services.submission.bearer-token" -> "token"
    )
    .build()

  lazy val connector: SubmissionConnector = app.injector.instanceOf[SubmissionConnector]

  "Submission Connector" - {
    "should return OK" - {
      "when the backend returns a valid successful response" in {
        server.stubFor(
          post(urlEqualTo("/mdr/dct06/v1"))
            .willReturn(
              aResponse()
                .withStatus(OK)
            )
        )

        val xml = <test></test>

        whenReady(connector.submitDisclosure(xml, ConversationId())) { result =>
          result.status mustBe OK
        }
      }
    }

    "throw an exception" - {
      "when upscan returns a 4xx response" in {
        server.stubFor(
          post(
            urlEqualTo(
              "" +
                "/mdr/dct06/v1"
            )
          )
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
            )
        )

        val xml    = <test></test>
        val result = connector.submitDisclosure(xml, ConversationId())

        result.futureValue.status mustBe BAD_REQUEST
      }

      "when upscan returns 5xx response" in {
        server.stubFor(
          post(urlEqualTo("/mdr/dct06/v1"))
            .willReturn(
              aResponse()
                .withStatus(SERVICE_UNAVAILABLE)
            )
        )

        val xml    = <test></test>
        val result = connector.submitDisclosure(xml, ConversationId())
        result.futureValue.status mustBe SERVICE_UNAVAILABLE
      }
    }

  }
}
