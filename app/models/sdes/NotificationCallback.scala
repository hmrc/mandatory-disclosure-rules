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

package models.sdes

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

final case class NotificationCallback(
  notification: NotificationType,
  filename: String,
  checksumAlgorithm: Algorithm,
  checksum: String,
  correlationID: String,
  dateTime: Option[LocalDateTime],
  failureReason: Option[String]
)

object NotificationCallback {
  implicit val format: OFormat[NotificationCallback] = Json.format
}
