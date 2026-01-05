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

package services

import connectors.EmailConnector
import models.email.{EmailRequest, EmailTemplate}
import models.error.ReadSubscriptionError
import models.subscription.{ContactType, IndividualDetails, OrganisationDetails}
import play.api.Logging
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import services.subscription.SubscriptionService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector, emailTemplate: EmailTemplate, subscriptionService: SubscriptionService)(implicit
  executionContext: ExecutionContext
) extends Logging {

  def sendAndLogEmail(subscriptionId: String, submissionTime: String, messageRefId: String, isUploadSuccessful: Boolean, reportTypeMessage: String)(implicit
    hc: HeaderCarrier
  ): Future[Int] =
    sendEmail(subscriptionId, submissionTime, messageRefId, isUploadSuccessful, reportTypeMessage) map {
      case Some(resp) =>
        resp.status match {
          case NOT_FOUND   => logger.warn("The template cannot be found within the email service")
          case BAD_REQUEST => logger.warn("Missing email or name parameter")
          case ACCEPTED    => logger.info("Email queued")
          case _           => logger.warn(s"Unhandled status received from email service ${resp.status}")
        }
        resp.status
      case _ =>
        logger.warn("Failed to send email")
        INTERNAL_SERVER_ERROR
    }

  private def getContactName(contactType: ContactType) =
    contactType match {
      case OrganisationDetails(organisationName)     => organisationName
      case IndividualDetails(firstName, _, lastName) => s"$firstName $lastName"
    }

  def sendEmail(subscriptionId: String, submissionTime: String, messageRefId: String, isUploadSuccessful: Boolean, reportTypeMessage: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[HttpResponse]] =
    subscriptionService.getContactInformation(subscriptionId).flatMap {
      case Right(responseDetail) =>
        val emailAddress          = Some(responseDetail.primaryContact.email)
        val contactName           = Some(getContactName(responseDetail.primaryContact.contactType))
        val secondaryEmailAddress = responseDetail.secondaryContact.map(_.email)
        val secondaryName         = responseDetail.secondaryContact.map(contactInfo => getContactName(contactInfo.contactType))

        for {
          primaryResponse <- emailAddress
            .filter(EmailAddress.isValid)
            .fold(Future.successful(Option.empty[HttpResponse])) { email =>
              emailConnector
                .sendEmail(
                  EmailRequest
                    .fileUploadSubmission(email, contactName, emailTemplate.getTemplate(isUploadSuccessful), submissionTime, messageRefId, reportTypeMessage)
                )
                .map(Some.apply)
            }

          _ <- secondaryEmailAddress
            .filter(EmailAddress.isValid)
            .fold(Future.successful(Option.empty[HttpResponse])) { secondaryEmailAddress =>
              emailConnector
                .sendEmail(
                  EmailRequest
                    .fileUploadSubmission(secondaryEmailAddress,
                                          secondaryName,
                                          emailTemplate.getTemplate(isUploadSuccessful),
                                          submissionTime,
                                          messageRefId,
                                          reportTypeMessage
                    )
                )
                .map(Some.apply)
            }
        } yield primaryResponse
      case Left(ReadSubscriptionError(value)) =>
        logger.warn(s"Failed to get contact information, received ReadSubscriptionError: $value")
        Future.successful(None)
    }
}

object EmailAddress {
  val validEmail = """^([a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)@([a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)$""".r

  def isValid(email: String) = email match {
    case validEmail(_, _) => true
    case _                => false
  }

}
