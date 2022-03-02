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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (
  config: Configuration,
  servicesConfig: ServicesConfig
) {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")

  val graphiteHost: String =
    config.get[String]("microservice.metrics.graphite.host")

  val maxValidationErrors: Int = config.get[Int]("xml.validation.max-errors")

  def serviceUrl(serviceName: String): String =
    s"${servicesConfig.baseUrl(serviceName)}${servicesConfig.getString(s"microservice.services.$serviceName.context")}"

  val bearerToken: String => String = (serviceName: String) => config.get[String](s"microservice.services.$serviceName.bearer-token")
  val environment: String => String = (serviceName: String) => config.get[String](s"microservice.services.$serviceName.environment")

  val enrolmentKey: String => String = (serviceName: String) => config.get[String](s"enrolmentKeys.$serviceName.key")
  val enrolmentId: String => String  = (serviceName: String) => config.get[String](s"enrolmentKeys.$serviceName.identifier")

  val isotypes  = config.get[String]("schemafiles.isotypes")
  val mdrtypes  = config.get[String]("schemafiles.mdrtypes")
  val mdrschema = config.get[String]("schemafiles.mdrschema")
  val eisSchema = config.get[String]("schemafiles.eisSchema")

  lazy val sendEmailUrl: String = servicesConfig.baseUrl("email")

  lazy val emailSuccessfulTemplate: String   = config.get[String]("emailTemplates.fileUploadSuccessful")
  lazy val emailUnsuccessfulTemplate: String = config.get[String]("emailTemplates.fileUploadUnsuccessful")
}
