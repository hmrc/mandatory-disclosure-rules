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

package services

import base.SpecBase
import connectors.EmailConnector
import generators.Generators
import models.email.EmailRequest
import models.error.ReadSubscriptionError
import models.subscription.{ContactInformation, OrganisationDetails, ResponseDetail}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.OK
import services.subscription.SubscriptionService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.DateTimeFormatUtil

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class EmailServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks with BeforeAndAfterEach {

  override def beforeEach: Unit =
    reset(
      mockEmailConnector,
      mockSubscriptionService
    )

  val mockEmailConnector: EmailConnector           = mock[EmailConnector]
  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[EmailConnector].toInstance(mockEmailConnector),
      bind[SubscriptionService].toInstance(mockSubscriptionService)
    )
    .build()

  val emailService: EmailService = app.injector.instanceOf[EmailService]

  val primaryContact = ContactInformation(
    OrganisationDetails("organisationName"),
    "test@email.com",
    None,
    None
  )

  val secondaryContact = ContactInformation(
    OrganisationDetails("OtherName"),
    "second@email.com",
    None,
    None
  )

  val submissionTime    = DateTimeFormatUtil.displayFormattedDate(LocalDateTime.now)
  val messageRefId      = "messageRefId"
  val subscriptionId    = "subscriptionId"
  val reportTypeMessage = "test content"

  "Email Service" - {
    "sendAndLogEmail" - {
      "must submit to the email connector with valid details and return 202" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(ACCEPTED, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact, None)))
          )

        val result = emailService.sendAndLogEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result mustBe ACCEPTED

          verify(mockEmailConnector, times(1)).sendEmail(any[EmailRequest])(any[HeaderCarrier])
        }
      }
      "must submit to the email connector and return NOT_FOUND when the template is missing" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(NOT_FOUND, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact, None)))
          )

        val result = emailService.sendAndLogEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result mustBe NOT_FOUND

          verify(mockEmailConnector, times(1)).sendEmail(any[EmailRequest])(any[HeaderCarrier])
        }
      }
      "must submit to the email connector and return BAD_REQUEST email service rejects request" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(BAD_REQUEST, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact, None)))
          )

        val result = emailService.sendAndLogEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result mustBe BAD_REQUEST

          verify(mockEmailConnector, times(1)).sendEmail(any[EmailRequest])(any[HeaderCarrier])
        }
      }
    }
    "sendEmail" - {

      "must submit to the email connector when 1 set of valid details provided" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact, None)))
          )

        val result = emailService.sendEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result.map(_.status) mustBe Some(OK)

          verify(mockEmailConnector, times(1)).sendEmail(any[EmailRequest])(any[HeaderCarrier])
        }
      }

      "must submit to the email connector twice when 2 sets of valid details provided" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact, Some(secondaryContact))))
          )

        val result = emailService.sendEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result.map(_.status) mustBe Some(OK)

          verify(mockEmailConnector, times(2)).sendEmail(any[EmailRequest])(any[HeaderCarrier])
        }
      }
      "must submit to the email connector when file upload fails" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact, Some(secondaryContact))))
          )

        val result = emailService.sendEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = false, reportTypeMessage)

        whenReady(result) { result =>
          result.map(_.status) mustBe Some(OK)

          verify(mockEmailConnector, times(2)).sendEmail(any[EmailRequest])(any[HeaderCarrier])
        }
      }

      "must fail to submit to the email connector when invalid email address provided" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Right(ResponseDetail(subscriptionId, None, isGBUser = true, primaryContact.copy(email = "test"), Some(secondaryContact))))
          )

        val result =
          emailService.sendEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result.map(_.status) mustBe None
        }
      }

      "must fail to submit to the email connector when failing to retrieve contact details" in {

        when(mockEmailConnector.sendEmail(any[EmailRequest])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        when(mockSubscriptionService.getContactInformation(any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Left(ReadSubscriptionError(INTERNAL_SERVER_ERROR)))
          )

        val result =
          emailService.sendEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful = true, reportTypeMessage)

        whenReady(result) { result =>
          result.map(_.status) mustBe None
        }
      }
    }
  }
}
