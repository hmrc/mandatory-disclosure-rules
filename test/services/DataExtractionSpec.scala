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

package services

import base.SpecBase
import fixtures.XMLFixtures
import models.submission.{
  MDR401,
  MDR402,
  MessageSpecData,
  MultipleCorrectionsDeletions,
  MultipleNewInformation,
  SingleCorrection,
  SingleDeletion,
  SingleNewInformation,
  SingleOther
}

class DataExtractionSpec extends SpecBase {

  "DataExtraction" - {
    val extractor = app.injector.instanceOf[DataExtraction]
    "messageSpecData must return messageSpec MultipleNewInformation data from a valid XML file" in {
      extractor.messageSpecData(XMLFixtures.validMessageMulitpleNewSpec) mustBe Some(
        MessageSpecData("GBXAMDR1234567", MDR401, 2, "OECD0", MultipleNewInformation)
      )
    }

    "messageSpecData must return messageSpec MultipleCorrectionsDeletions data from a valid XML file" in {
      extractor.messageSpecData(XMLFixtures.validMessageMultipleCorrectionSpec) mustBe Some(
        MessageSpecData("GBXAMDR1234567", MDR402, 2, "OECD0", MultipleCorrectionsDeletions)
      )
    }

    "messageSpecData must return messageSpec SingleNewInformation data from a valid XML file" in {
      extractor.messageSpecData(XMLFixtures.validMessageSingleNewSpec) mustBe Some(
        MessageSpecData("GBXAMDR1234567", MDR401, 1, "OECD1", SingleNewInformation)
      )
    }

    "messageSpecData must return messageSpec SingleDeletion data from a valid XML file" in {
      extractor.messageSpecData(XMLFixtures.validMessageSingleDeletionSpec) mustBe Some(
        MessageSpecData("GBXAMDR1234567", MDR401, 1, "OECD3", SingleDeletion)
      )
    }

    "messageSpecData must return messageSpec SingleCorrection data from a valid XML file" in {
      extractor.messageSpecData(XMLFixtures.validMessageSingleCorrectionSpec) mustBe Some(
        MessageSpecData("GBXAMDR1234567", MDR402, 1, "OECD2", SingleCorrection)
      )
    }

    "messageSpecData must return messageSpec SingleOther data from a valid XML file" in {
      extractor.messageSpecData(XMLFixtures.validMessageSingleOtherSpec) mustBe Some(
        MessageSpecData("GBXAMDR1234567", MDR402, 1, "OECD5", SingleOther)
      )
    }

    "messageSpecData must return None when messageRefId is missing" in {
      extractor.messageSpecData(XMLFixtures.missingMessageRefId) mustBe None
    }

    "messageSpecData must return None when messageTypeIndic is missing" in {
      extractor.messageSpecData(XMLFixtures.missingMessageTypeIndic) mustBe None
    }

    "messageSpecData must throw NoSuchElement when messageTypeIndic invalid " in {
      assertThrows[NoSuchElementException] {
        extractor.messageSpecData(XMLFixtures.invalidMessageTypeIndic) mustBe None
      }
    }
  }
}
