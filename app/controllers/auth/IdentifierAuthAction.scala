/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.auth

import com.google.inject.ImplementedBy
import config.AppConfig
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc.Results.Status
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentifierAuthActionImpl @Inject() (
  override val authConnector: AuthConnector,
  val parser: BodyParsers.Default,
  config: AppConfig
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAuthAction
    with AuthorisedFunctions {

  private val serviceName: String = "mdr"
  val enrolmentKey: String        = config.enrolmentKey(serviceName)
  val enrolmentId: String         = config.enrolmentId(serviceName)

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(Enrolment(enrolmentKey)).retrieve(Retrievals.authorisedEnrolments) {
      case Enrolments(enrolments) =>
        val enrolmentID =
          (for {
            enrolment           <- enrolments.find(_.key.equals(enrolmentKey))
            enrolmentIdentifier <- enrolment.getIdentifier(enrolmentId)
          } yield enrolmentIdentifier.value)
            .getOrElse(throw new IllegalAccessException("EnrolmentID Required"))

        block(UserRequest(enrolmentID, request))
      case _ => Future.successful(Status(UNAUTHORIZED))
    } recover { case _: NoActiveSession =>
      Status(UNAUTHORIZED)
    }
  }
}

@ImplementedBy(classOf[IdentifierAuthActionImpl])
trait IdentifierAuthAction extends ActionBuilder[UserRequest, AnyContent] with ActionFunction[Request, UserRequest]
