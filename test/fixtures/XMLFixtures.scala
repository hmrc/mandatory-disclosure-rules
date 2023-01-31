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

package fixtures

import scala.xml.Elem

object XMLFixtures {

  val validMessageSpec: Elem = <MDR_OECD >
    <MessageSpec>
      <MessageRefId>GBXAMDR1234567</MessageRefId>
      <MessageTypeIndic>MDR401</MessageTypeIndic>
    </MessageSpec>
  </MDR_OECD>

  val missingMessageRefId: Elem = <MDR_OECD >
    <MessageSpec>
      <ID>GBXAMDR1234567</ID>
      <MessageTypeIndic>MDR401</MessageTypeIndic>
    </MessageSpec>
  </MDR_OECD>

  val missingMessageTypeIndic: Elem = <MDR_OECD >
    <MessageSpec>
      <MessageRefId>GBXAMDR1234567</MessageRefId>
    </MessageSpec>
  </MDR_OECD>

  val invalidMessageTypeIndic: Elem = <MDR_OECD >
    <MessageSpec>
      <MessageRefId>GBXAMDR1234567</MessageRefId>
      <MessageTypeIndic>MDR403</MessageTypeIndic>
    </MessageSpec>
  </MDR_OECD>

}
