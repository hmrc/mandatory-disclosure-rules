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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (
  config: Configuration,
  servicesConfig: ServicesConfig
) {

  val appName: String     = config.get[String]("appName")
  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")

  val auditFileSubmission: Boolean = config.get[Boolean]("auditing.event.fileSubmission.enabled")

  val graphiteHost: String =
    config.get[String]("microservice.metrics.graphite.host")

  val maxValidationErrors: Int = config.get[Int]("xml.validation.max-errors")

  val eisResponseWaitTime: Int = config.get[Int]("microservice.services.eis-response.wait-time")

  def serviceUrl(serviceName: String): String =
    s"${servicesConfig.baseUrl(serviceName)}${servicesConfig.getString(s"microservice.services.$serviceName.context")}"

  val bearerToken: String => String = (serviceName: String) => config.get[String](s"microservice.services.$serviceName.bearer-token")
  val environment: String => String = (serviceName: String) => config.get[String](s"microservice.services.$serviceName.environment")

  val enrolmentKey: String => String = (serviceName: String) => config.get[String](s"enrolmentKeys.$serviceName.key")
  val enrolmentId: String => String  = (serviceName: String) => config.get[String](s"enrolmentKeys.$serviceName.identifier")

  val fileUploadXSDFilePath: String  = config.get[String]("xsd-files.fileUpload_MDR_XSDFile")
  val submissionXSDFilePath: String  = config.get[String]("xsd-files.submission_DCT72a_XSDFile")
  val eisResponseXSDFilePath: String = config.get[String]("xsd-files.eisResponse_DCT72B_XSDFile")

  lazy val sendEmailUrl: String = servicesConfig.baseUrl("email")

  lazy val emailSuccessfulTemplate: String   = config.get[String]("emailTemplates.fileUploadSuccessful")
  lazy val emailUnsuccessfulTemplate: String = config.get[String]("emailTemplates.fileUploadUnsuccessful")

  lazy val cacheTtl: Int      = config.get[Int]("mongodb.timeToLiveInSeconds")
  lazy val submissionTtl: Int = config.get[Int]("mongodb.submission.timeToLiveInDays")

  //TODO: Remove this after a successful production deployment
  lazy val dropUpscanSessionCollection: Boolean = config.get[Boolean]("mongodb.upScanSessionRepository.dropCollection")
  lazy val mongodbUri: String                   = config.get[String]("mongodb.uri")
}
