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

import base.SpecBase

import java.time.LocalDateTime

class DateTimeFormatUtilSpec extends SpecBase {

  private def generateDate(hour: Int) =
    LocalDateTime.of(2022, 1, 1, hour, 1, 0, 0)

  "dateFormatted" - {

    "should format submission timestamp for morning(AM)" in {

      val result = DateTimeFormatUtil.dateFormatted(generateDate(4))
      result mustBe s"4:01am on 1 January 2022"
    }

    "should format submission timestamp for afternoon(PM)" in {

      val result = DateTimeFormatUtil.dateFormatted(generateDate(16))
      result mustBe s"4:01pm on 1 January 2022"
    }
  }
}
