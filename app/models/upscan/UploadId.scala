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

package models.upscan

import play.api.libs.json.*
import play.api.mvc.QueryStringBindable

case class UploadId(value: String) extends AnyVal

object UploadId {

  implicit def queryBinder(implicit
    stringBinder: QueryStringBindable[String]
  ): QueryStringBindable[UploadId] =
    stringBinder.transform(UploadId(_), _.value)

  implicit val uploadIdFormat: OFormat[UploadId] = new OFormat[UploadId] {
    override def writes(id: UploadId): JsObject = Json.obj("value" -> id.value)

    override def reads(json: JsValue): JsResult[UploadId] = (json \ "value").validate[String].map(UploadId.apply)
  }

  implicit def readsUploadId: Reads[UploadId] =
    Reads.StringReads.map(UploadId(_))

  implicit def writesUploadId: Writes[UploadId] =
    Writes[UploadId](x => JsString(x.value))
}
