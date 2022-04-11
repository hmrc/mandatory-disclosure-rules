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

object ErrorDetails {
  val error_details_901 =
    "The CorrDocRefId does not match a DocRefId from the same type of section (either Disclosing or MdrReport). It must refer to the same element"
  val error_details_902 = "The MdrReport CorrDocRefId does not refer to the same previously sent MdrBody as the Disclosing element"
  val error_details_903 = "The Disclosing section contains resent data (DocTypeIndic = OECD0) so it must not have a CorrDocRefId"
  val error_details_904 = "Disclosing Capacity is not one of the allowed values for the MdrReport CrsAvoidance or OOS Reason provided"
  val error_details_905 = "Since the DocTypeIndic of Disclosing is OECD0, the DocTypeIndic of MdrReport must be OECD2"

  val error_details_906 =
    "Since the MdrReport has a DocTypeIndic of OECD3, indicating this section must be deleted, this Disclosing section must be deleted too"

  val error_details_907 =
    "Since the MessageTypeIndic contains the value of MDR401 for new information, the Disclosing DocTypeIndic must contain the value of OECD1 for new information"

  val error_details_908 =
    "Since the MessageTypeIndic contains the value of MDR401 for new information, an MdrReport section must be provided with a DocTypeIndic of OECD1 for new information"
  val error_details_909 = "DocRefId must be 100 characters or less, start with your 15-character MDR ID and include up to 85 other characters of your choice"
  val error_details_910 = "MessageRefId must be 85 characters or less, start with your 15-character MDR ID and include up to 70 other characters of your choice"
  val error_details_911 = "Provide an issuedBy for every TIN that has a value other than NOTIN"
  val error_details_912 = "The top level of the StructureChart must not have an Ownership or InvestAmount"

  val errorList: Seq[String] = Seq(
    error_details_901,
    error_details_902,
    error_details_903,
    error_details_904,
    error_details_905,
    error_details_906,
    error_details_907,
    error_details_908,
    error_details_909,
    error_details_910,
    error_details_911,
    error_details_912
  )
}
