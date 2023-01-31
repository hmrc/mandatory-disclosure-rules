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

package models.xml

import base.SpecBase
import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import models.xml.FileErrorCode.MessageRefIDHasAlreadyBeenUsed
import models.xml.RecordErrorCode.MessageTypeIndic

import java.util.UUID

class BREResponseSpec extends SpecBase {

  "BREResponse" - {

    "must read xml as BREResponse for the status 'Rejected'" in {
      val uuid = UUID.randomUUID().toString
      val xml = <BREResponse>
                  <requestCommon>
                    <receiptDate>2001-12-17T09:30:47Z</receiptDate>
                    <regime>MDR</regime>
                    <conversationID>{uuid}</conversationID>
                    <schemaVersion>1.0.0</schemaVersion>
                  </requestCommon>
                  <requestDetail>
                    <gsm:GenericStatusMessage>
                      <gsm:ValidationErrors>
                        <gsm:FileError>
                          <gsm:Code>50009</gsm:Code>
                          <gsm:Details Language="EN">Duplicate message ref ID</gsm:Details>
                        </gsm:FileError>
                        <gsm:RecordError>
                          <gsm:Code>80010</gsm:Code>
                          <gsm:Details Language="EN">A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both</gsm:Details>
                          <gsm:DocRefIDInError>asjdhjjhjssjhdjshdAJGSJJS</gsm:DocRefIDInError>
                        </gsm:RecordError>
                      </gsm:ValidationErrors>
                      <gsm:ValidationResult>
                        <gsm:Status>Rejected</gsm:Status>
                      </gsm:ValidationResult>
                    </gsm:GenericStatusMessage>
                  </requestDetail>
                </BREResponse>

      val fileErrors = Some(List(FileErrors(MessageRefIDHasAlreadyBeenUsed, Some("Duplicate message ref ID"))))

      val expectedResult = ParseSuccess(
        BREResponse(
          "MDR",
          uuid,
          GenericStatusMessage(
            ValidationErrors(
              fileErrors,
              Some(
                List(
                  RecordError(
                    MessageTypeIndic,
                    Some("A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both"),
                    Some(List("asjdhjjhjssjhdjshdAJGSJJS"))
                  )
                )
              )
            ),
            ValidationStatus.rejected
          )
        )
      )

      XmlReader.of[BREResponse].read(xml) mustBe expectedResult

    }

    "must read xml as BREResponse for the status 'Accepted'" in {
      val uuid = UUID.randomUUID().toString
      val xml = <BREResponse>
                  <requestCommon>
                    <receiptDate>2001-12-17T09:30:47Z</receiptDate>
                    <regime>MDR</regime>
                    <conversationID>{uuid}</conversationID>
                    <schemaVersion>1.0.0</schemaVersion>
                  </requestCommon>
                  <requestDetail>
                    <gsm:GenericStatusMessage>
                      <gsm:ValidationErrors>
                      </gsm:ValidationErrors>
                      <gsm:ValidationResult>
                        <gsm:Status>Accepted</gsm:Status>
                      </gsm:ValidationResult>
                    </gsm:GenericStatusMessage>
                  </requestDetail>
                </BREResponse>

      val expectedResult = ParseSuccess(BREResponse("MDR", uuid, GenericStatusMessage(ValidationErrors(None, None), ValidationStatus.accepted)))

      XmlReader.of[BREResponse].read(xml) mustBe expectedResult

    }

  }
}
