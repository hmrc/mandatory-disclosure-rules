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

import play.api.libs.json._

sealed trait ReportType

case object MultipleNewInformation extends ReportType
case object MultipleCorrectionsDeletions extends ReportType
case object SingleNewInformation extends ReportType
case object SingleCorrection extends ReportType
case object SingleDeletion extends ReportType
case object SingleOther extends ReportType

object ReportType {
  def fromString(ReportType: String): ReportType = ReportType.toUpperCase match {
    case "MULTIPLENEWINFORMATION"       => MultipleNewInformation
    case "MULTIPLECORRECTIONSDELETIONS" => MultipleCorrectionsDeletions
    case "SINGLENEWINFORMATION"         => SingleNewInformation
    case "SINGLECORRECTION"             => SingleCorrection
    case "SINGLEDELETION"               => SingleDeletion
    case "SINGLEOTHER"                  => SingleOther
    case _                              => throw new NoSuchElementException
  }

  implicit val writes: Writes[ReportType] = Writes[ReportType] {
    case MultipleNewInformation       => JsString("MultipleNewInformation")
    case MultipleCorrectionsDeletions => JsString("MultipleCorrectionsDeletions")
    case SingleNewInformation         => JsString("SingleNewInformation")
    case SingleCorrection             => JsString("SingleCorrection")
    case SingleDeletion               => JsString("SingleDeletion")
    case SingleOther                  => JsString("SingleOther")
  }

  implicit val reads: Reads[ReportType] = Reads[ReportType] {
    case JsString("MultipleNewInformation")       => JsSuccess(MultipleNewInformation)
    case JsString("MultipleCorrectionsDeletions") => JsSuccess(MultipleCorrectionsDeletions)
    case JsString("SingleNewInformation")         => JsSuccess(SingleNewInformation)
    case JsString("SingleCorrection")             => JsSuccess(SingleCorrection)
    case JsString("SingleDeletion")               => JsSuccess(SingleDeletion)
    case JsString("SingleOther")                  => JsSuccess(SingleOther)
    case value                                    => JsError(s"Unexpected value of _type: $value")
  }

  def getMessage(reportType: Option[ReportType]): String =
    reportType match {
      case Some(MultipleNewInformation)       => "The file contains new information in multiple reports."
      case Some(MultipleCorrectionsDeletions) => "The file contains corrections or deletions for multiple reports."
      case Some(SingleNewInformation)         => "The file contains new information in one report."
      case Some(SingleCorrection)             => "The file contains corrections in one report."
      case Some(SingleDeletion)               => "The file contains a deletion of a previous report."
      case _                                  => ""
    }
}

sealed trait MessageTypeIndic
case object MDR401 extends MessageTypeIndic
case object MDR402 extends MessageTypeIndic

object MessageTypeIndic {

  def fromString(typeIndic: String): MessageTypeIndic = typeIndic.toUpperCase match {
    case "MDR401" => MDR401
    case "MDR402" => MDR402
    case _        => throw new NoSuchElementException
  }

  implicit val writes: Writes[MessageTypeIndic] = Writes[MessageTypeIndic] {
    case MDR401 => JsString("MDR401")
    case MDR402 => JsString("MDR402")
  }
  implicit val reads: Reads[MessageTypeIndic] = Reads[MessageTypeIndic] {
    case JsString("MDR401") => JsSuccess(MDR401)
    case JsString("MDR402") => JsSuccess(MDR402)
    case value              => JsError(s"Unexpected value of _type: $value")
  }
}

case class MessageSpecData(messageRefId: String, messageTypeIndic: MessageTypeIndic, mdrBodyCount: Int, docTypeIndic: String, reportType: ReportType)

object MessageSpecData {
  implicit val format: OFormat[MessageSpecData] = Json.format[MessageSpecData]
}
