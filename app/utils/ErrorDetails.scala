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

package utils

object ErrorDetails {
  val error_details_901  = "CorrDocRefID element type does not match original DocRefID element type"
  val error_details_902  = "Correction is not for the relevant Disclosing element"
  val error_details_903  = "CorrDocRefID must not be present for OECD0 at Disclosing Element"
  val error_details_904a = "If the only Capacity entry is MDR501 the Reason must be either MDR701 or MDR901"
  val error_details_904b = "If the only Capacity entry is MDR502 the Reason must be either MDR702 or MDR902"
  val error_details_904c = "If the only Capacity entry is MDR503 the Reason must be either MDR701, MDR702, MDR901 or MDR902"
  val error_details_904d = "If the only Capacity entry is MDR504 then Reason must not be provided"
  val error_details_905  = "If DocTypeIndic of Disclosing is OECD0 the DocTypeIndic of MdrReport must OECD2"
  val error_details_906  = "If DocTypeIndic of MdrReport is OECD3 the DocTypeIndic of Disclosing must be OECD3"
  val error_details_907  = "If MessageTypeIndic is MDR401, Disclosing DocTypeIndic can only be OECD1"
  val error_details_908  = "If MessageTypeIndic is MDR401 and Diclosing DocTypeIndic is OECD1, the MdrReport must be present and have DocTypeIndic of OECD1"
  val error_details_909  = "DocRefID format does not match the format as set out in the HMRC MDR user guide"
  val error_details_910  = "MessageRefID format does not match the format as set out in the HMRC MDR user guide"
  val error_details_911  = """TIN issuedby must be provided where a TIN has been reported. The only exception is where "NOTIN" has been reported"""
  val error_details_912  =
    "The top level of the structure chart must not include the elements: mdr:ownership and mdr:InvestAmount. These should only be provided in the  mdr:ListChilds tag"

  val errorList: Seq[String] = Seq(
    error_details_901,
    error_details_902,
    error_details_903,
    error_details_904a,
    error_details_904b,
    error_details_904c,
    error_details_904d,
    error_details_905,
    error_details_906,
    error_details_907,
    error_details_908,
    error_details_909,
    error_details_911,
    error_details_912
  )
}
