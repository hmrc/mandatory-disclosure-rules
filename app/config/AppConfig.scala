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

import models.sdes.Algorithm
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject() (
  config: Configuration,
  servicesConfig: ServicesConfig
) {

  val appName: String = config.get[String]("appName")

  val auditFileSubmission: Boolean = config.get[Boolean]("auditing.event.fileSubmission.enabled")

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

  lazy val sdesclientId: String = config.get[String]("sdes.client-id")

  lazy val sdesRecipientOrSender: String =
    config.get[String]("sdes.recipient-or-sender")

  lazy val sdesInformationType: String =
    config.get[String]("sdes.information-type")

  private val apiLocation: Option[String] = Some(config.get[String]("sdes.location")).filter(_.nonEmpty)

  lazy val sdesUrl: String =
    List(Some(servicesConfig.baseUrl("sdes")), apiLocation, Some("notification"), Some("fileready")).flatten.mkString("/")

  lazy val sdesChecksumAlgorithm: Algorithm = Algorithm(config.get[String]("sdes.checksum-algorithm"))

  lazy val maxNormalFileSize: Int = config.get[String]("max-normal-file-size").toInt

  lazy val emailSuccessfulTemplate: String   = config.get[String]("emailTemplates.fileUploadSuccessful")
  lazy val emailUnsuccessfulTemplate: String = config.get[String]("emailTemplates.fileUploadUnsuccessful")

  lazy val cacheTtl: Int      = config.get[Int]("mongodb.timeToLiveInSeconds")
  lazy val submissionTtl: Int = config.get[Int]("mongodb.submission.timeToLiveInDays")

  lazy val sdesFileTransfer: Boolean = config.get[Boolean]("features.sdesFileTransfer")

  val staleTaskInterval: FiniteDuration = config.get[FiniteDuration]("tasks.staleFiles.interval")
  val staleTaskEnabled: Boolean         = config.get[Boolean]("tasks.staleFiles.enabled")
}
