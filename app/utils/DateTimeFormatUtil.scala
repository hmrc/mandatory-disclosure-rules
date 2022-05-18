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

package utils

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

object DateTimeFormatUtil {

  private val euLondonZoneId: ZoneId = ZoneId.of("Europe/London")

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma")

  def zonedDateTimeNow: ZonedDateTime = ZonedDateTime.now(euLondonZoneId)

  def displayFormattedDate(dateTime: LocalDateTime): String =
    s"${dateTime.atZone(euLondonZoneId).format(timeFormatter).toLowerCase} on ${dateTime.atZone(euLondonZoneId).format(dateFormatter)}"

}
