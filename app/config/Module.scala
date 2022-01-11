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

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import services.upscan.{MongoBackedUploadProgressTracker, UploadProgressTracker}
import services.validation.{Dac6SchemaValidatingParser, MDRSchemaValidatingParser, SaxParser}

class Module(environment: Environment, config: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    val useMDR = config.get[Boolean]("xmlparser.useMDR")

    bind(classOf[UploadProgressTracker]).to(classOf[MongoBackedUploadProgressTracker])
    if (useMDR)
      bind(classOf[SaxParser]).to(classOf[MDRSchemaValidatingParser])
    else
      bind(classOf[SaxParser]).to(classOf[Dac6SchemaValidatingParser])
  }

}
