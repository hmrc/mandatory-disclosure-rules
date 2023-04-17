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

package models.submission

import base.SpecBase
import play.api.libs.json.{JsString, Json}

class ReportTypeSpec extends SpecBase {

  "ReportTypeSpec" - {
    "must serialize ReportType" in {

      val mnew: ReportType         = MultipleNewInformation
      val mncorrection: ReportType = MultipleCorrectionsDeletions
      val snew: ReportType         = SingleNewInformation
      val scorrection: ReportType  = SingleCorrection
      val sdeletion: ReportType    = SingleDeletion
      val sother: ReportType       = SingleOther

      Json.toJson(mnew) mustBe JsString("MultipleNewInformation")
      Json.toJson(mncorrection) mustBe JsString("MultipleCorrectionsDeletions")
      Json.toJson(snew) mustBe JsString("SingleNewInformation")
      Json.toJson(scorrection) mustBe JsString("SingleCorrection")
      Json.toJson(sdeletion) mustBe JsString("SingleDeletion")
      Json.toJson(sother) mustBe JsString("SingleOther")
    }
    "must deserialize ReportType" in {
      val jsonMnew        = JsString("MultipleNewInformation")
      val jsonMCorrection = JsString("MultipleCorrectionsDeletions")
      val jsonSnew        = JsString("SingleNewInformation")
      val jsonSCorrection = JsString("SingleCorrection")
      val jsonSDeletion   = JsString("SingleDeletion")
      val jsonSOther      = JsString("SingleOther")

      jsonMnew.as[ReportType] mustEqual MultipleNewInformation
      jsonMCorrection.as[ReportType] mustEqual MultipleCorrectionsDeletions
      jsonSnew.as[ReportType] mustEqual SingleNewInformation
      jsonSCorrection.as[ReportType] mustEqual SingleCorrection
      jsonSDeletion.as[ReportType] mustEqual SingleDeletion
      jsonSOther.as[ReportType] mustEqual SingleOther

    }
  }
}
