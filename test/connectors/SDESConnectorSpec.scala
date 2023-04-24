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

package connectors

import base.{SpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import models.sdes._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}

import scala.concurrent.ExecutionContext.Implicits.global
class SDESConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {
  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.sdes.port" -> server.port()
    )
    .build()

  lazy val connector: SDESConnector =
    app.injector.instanceOf[SDESConnector]

  "SDESConnector" - {
    "fileready must return status as NO-CONTENT for success" in {
      stubResponse(
        "/notification/fileready",
        NO_CONTENT
      )

      forAll(arbitrary[FileTransferNotification]) { ftn =>
        val result = connector.fileReady(ftn)

        result.futureValue.status mustBe NO_CONTENT

      }
    }

    "fileready must BAD-REQUEST for an incorrect submission" in {
      stubResponse(
        "/notification/fileready",
        BAD_REQUEST
      )

      forAll(arbitrary[FileTransferNotification]) { ftn =>
        val result = connector.fileReady(ftn)

        result.futureValue.status mustBe BAD_REQUEST

      }
    }

    "fileready must INTERNAL_SERVER_ERROR when a server error occurs" in {
      stubResponse(
        "/notification/fileready",
        INTERNAL_SERVER_ERROR
      )

      forAll(arbitrary[FileTransferNotification]) { ftn =>
        val result = connector.fileReady(ftn)

        result.futureValue.status mustBe INTERNAL_SERVER_ERROR

      }
    }
  }

  private def stubResponse(
    expectedUrl: String,
    expectedStatus: Int
  ): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )

}
