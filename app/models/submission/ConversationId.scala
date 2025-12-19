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

package models.submission

import models.upscan.UploadId
import play.api.libs.json.{__, JsString, Reads, Writes}
import play.api.mvc.PathBindable

import java.util.UUID

case class ConversationId(value: String)

object ConversationId {
  def apply(): ConversationId                          = ConversationId(UUID.randomUUID().toString)
  def fromUploadId(uploadId: UploadId): ConversationId = ConversationId(uploadId.value)
  implicit val writes: Writes[ConversationId]          = conversationId => JsString(conversationId.value)
  implicit val reads: Reads[ConversationId]            = __.read[String].map(id => ConversationId(id))

  implicit lazy val pathBindable: PathBindable[ConversationId] = new PathBindable[ConversationId] {
    override def bind(key: String, value: String): Either[String, ConversationId] =
      implicitly[PathBindable[String]].bind(key, value).right.map(ConversationId(_))

    override def unbind(key: String, value: ConversationId): String =
      value.value
  }
}
