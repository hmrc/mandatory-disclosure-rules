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

import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpReads, HttpResponse}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

package object connectors {

  implicit val httpReads: HttpReads[HttpResponse] =
    (_: String, _: String, response: HttpResponse) => response

  val stripSession: String => String = (input: String) => input.replace("session-", "")

  implicit class SeqOps(val seq: Seq[(String, String)]) {

    def withBearerToken(bearerToken: String): Seq[(String, String)] =
      seq :+ (HeaderNames.authorisation -> s"Bearer $bearerToken")

    def withXForwardedHost(value: Option[String] = None): Seq[(String, String)] =
      seq :+ ("x-forwarded-host" -> s"${value.getOrElse("mdtp")}")

    def withXCorrelationId(value: Option[String] = None): Seq[(String, String)] = {
      val xCorrelationId = value.getOrElse(UUID.randomUUID().toString)
      seq :+ ("x-correlation-id" -> s"$xCorrelationId")
    }

    def withXConversationId(value: Option[String] = None)(implicit headerCarrier: HeaderCarrier): Seq[(String, String)] = {
      val xConversationId = stripSession(value.getOrElse(headerCarrier.sessionId.map(_.value).getOrElse(UUID.randomUUID().toString)))
      seq :+ ("x-conversation-id" -> s"$xConversationId")
    }

    def withDate(value: Option[String] = None): Seq[(String, String)] = {
      //HTTP-date format defined by RFC 7231 e.g. Fri, 01 Aug 2020 15:51:38 GMT+1
      val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O")
      val date      = ZonedDateTime.now().format(formatter)
      seq :+ ("date" -> s"${value.getOrElse(date)}")
    }

    def withContentType(value: Option[String] = None): Seq[(String, String)] =
      seq :+ ("content-type" -> value.getOrElse("application/json"))

    def withAccept(value: Option[String] = None): Seq[(String, String)] =
      seq :+ ("accept" -> value.getOrElse("application/json"))

    def withEnvironment(value: Option[String] = None): Seq[(String, String)] =
      seq :+ ("Environment" -> s"${value.getOrElse("")}")

  }

}
