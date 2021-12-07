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

package helpers

import base.SpecBase
import models.validation.{GenericError, SaxParseError}

import scala.collection.mutable.ListBuffer

class XmlErrorMessageHelperSpec extends SpecBase {

  val lineNumber = 20
  val over400    = "s" * 401
  val over200    = "s" * 201

  val helper = new XmlErrorMessageHelper
  "ErrorMessageHelper" - {

    "generateErrorMessages" - {

      "must return correct error for missing attribute error'" in {
        val missingAttributeError = SaxParseError(lineNumber, "cvc-complex-type.4: Attribute 'currCode' must appear on element 'Amount'.")
        val result                = helper.generateErrorMessages(ListBuffer(missingAttributeError))
        result mustBe List(GenericError(lineNumber, "Enter an Amount currCode"))
      }

      "must return correct error for unexpected error message'" in {
        val randomError = SaxParseError(lineNumber, "random error.")
        val result      = helper.generateErrorMessages(ListBuffer(randomError))
        result mustBe List(GenericError(lineNumber, "There is a problem with this line number"))
      }

      "must return correct error for invalid enum error for attribute" in {
        val invalidEnumError1 =
          SaxParseError(lineNumber,
                        "cvc-enumeration-valid: Value 'GBf' is not facet-valid with respect to enumeration '[AF, AX]'. It must be a value from the enumeration."
          )
        val invalidEnumError2 =
          SaxParseError(lineNumber,
                        "cvc-attribute.3: The value 'GBf' of attribute 'issuedBy' on element 'TIN' is not valid with respect to its type, 'CountryCode_Type'."
          )
        val result = helper.generateErrorMessages(ListBuffer(invalidEnumError1, invalidEnumError2))
        result mustBe List(GenericError(lineNumber, "TIN issuedBy is not one of the ISO country codes"))
      }

      "must return correct error for missing element error'" in {

        val missingElementError1 =
          SaxParseError(lineNumber,
                        "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'StringMin1Max400_Type'."
          )

        val missingElementError2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '' of element 'Street' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(missingElementError1, missingElementError2))
        result mustBe List(GenericError(lineNumber, "Enter a Street"))
      }

      "must return correct error for missing ConcernedMS'" in {

        val error1 = SaxParseError(
          lineNumber,
          "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[AT, BE, BG, CY, CZ, DK, EE, FI, FR, DE, GR, HU, HR, IE, IT, LV, LT, LU, MT, NL, PL, PT, RO, SK, SI, ES, SE, GB]'. It must be a value from the enumeration."
        )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '' of element 'ConcernedMS' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter a ConcernedMS"))
      }

      "must return correct error for missing birthplace'" in {

        val error1 =
          SaxParseError(lineNumber,
                        "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'StringMin1Max200_Type'."
          )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '' of element 'BirthPlace' is not valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter a BirthPlace"))
      }

      "must return correct error when length exceed for TIN'" in {

        val error1 = SaxParseError(
          lineNumber,
          s"cvc-maxLength-valid: Value '$over200' with length = '201' is not facet-valid with respect to maxLength '200' for type 'StringMin1Max200_Type'."
        )
        val error2 = SaxParseError(lineNumber, "cvc-complex-type.2.2: Element 'TIN' must have no element [children], and the value must be valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "TIN must be 200 characters or less"))
      }

      "must return correct error for missing org name'" in {

        val error1 =
          SaxParseError(lineNumber,
                        "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'StringMin1Max200_Type'."
          )
        val error2 = SaxParseError(lineNumber, "cvc-complex-type.2.2: Element 'OrganisationName' must have no element [children], and the value must be valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter an OrganisationName"))
      }

      "must return correct error when allowed length exceeded" in {

        val maxLengthError1 = SaxParseError(
          lineNumber,
          s"cvc-maxLength-valid: Value '$over400' with length = '401' is not facet-valid with respect to maxLength '400' for type 'StringMin1Max400_Type'."
        )
        val maxlengthError2 = SaxParseError(lineNumber, s"cvc-type.3.1.3: The value '$over400' of element 'BuildingIdentifier' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(maxLengthError1, maxlengthError2))
        result mustBe List(GenericError(lineNumber, "BuildingIdentifier must be 400 characters or less"))
      }

      "must return correct error when invalid enum given for element" in {

        val invalidEnumError1 = SaxParseError(
          lineNumber,
          "cvc-enumeration-valid: Value 'Invalid code' is not facet-valid with respect to enumeration '[AF, AX, AL, DZ]'. It must be a value from the enumeration."
        )
        val invalidEnumError2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value 'Raneevev' of element 'Country' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(invalidEnumError1, invalidEnumError2))
        result mustBe List(GenericError(lineNumber, "Country is not one of the ISO country codes"))
      }

      "must return correct error when when pence included on amount field" in {

        val error1 = SaxParseError(lineNumber, "cvc-datatype-valid.1.2.1: '4000.02' is not a valid value for 'integer'.")
        val error2 = SaxParseError(lineNumber, "cvc-complex-type.2.2: Element 'Amount' must have no element [children], and the value must be valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Amount must not include pence, like 123 or 156"))
      }

      "must return correct error for invalid date format" in {

        val error1 = SaxParseError(lineNumber, "cvc-datatype-valid.1.2.1: '14-01-2007' is not a valid value for 'date'.")
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '14-01-2007' of element 'BirthDate' is not valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter a BirthDate in the format YYYY-MM-DD"))
      }

      "must return correct error for invalid date format (ImplementingDate)" in {
        val error1 = SaxParseError(lineNumber, "cvc-datatype-valid.1.2.1: '2020-05-oo' is not a valid value for 'date'.")
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '2020-05-oo' of element 'ImplementingDate' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter a DisclosureInformation/ImplementingDate in the format YYYY-MM-DD"))
      }

      "must return correct error for invalid Intermediary Capacity" in {
        val error1 = SaxParseError(
          lineNumber,
          "cvc-enumeration-valid: Value 'DAC61102fggg' is not facet-valid with respect to enumeration '[DAC61101, DAC61102]'. It must be a value from the enumeration."
        )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value 'DAC61102fggg' of element 'Capacity' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Capacity is not one of the allowed values (DAC61101, DAC61102) for Intermediary"))
      }

      "must return correct error for invalid TaxPayer Capacity" in {
        val error1 = SaxParseError(
          lineNumber,
          "cvc-enumeration-valid: Value 'DAC61105hjkjk' is not facet-valid with respect to enumeration '[DAC61104, DAC61105, DAC61106]'. It must be a value from the enumeration."
        )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value 'DAC61105hjkjk' of element 'Capacity' is not valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Capacity is not one of the allowed values (DAC61104, DAC61105, DAC61106) for Taxpayer"))
      }

      "must return correct error for line (value and tags)" in {

        val error1 = SaxParseError(
          lineNumber,
          "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"urn:ukdac6:v0.1\":BuildingIdentifier}'. One of '{\"urn:ukdac6:v0.1\":Street}' is expected."
        )
        val result = helper.generateErrorMessages(ListBuffer(error1))
        result mustBe List(GenericError(lineNumber, "Enter a line for Street"))
      }

      "must return correct error when 3 errors present for same line" in {

        val error1 = SaxParseError(lineNumber, "random error 1")
        val error2 = SaxParseError(lineNumber, "random error 2")
        val error3 = SaxParseError(lineNumber, "random error 3")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2, error3))
        result mustBe List(GenericError(20, "There is a problem with this line number"))
      }

      "must return correct error for missing boolean value (affected Person)" in {

        val error1 = SaxParseError(lineNumber, "cvc-datatype-valid.1.2.1: '' is not a valid value for 'boolean'.")
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '' of element 'AffectedPerson' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter an AssociatedEnterprise/AffectedPerson"))
      }

      "must return correct error for missing other boolean value" in {

        val error1 = SaxParseError(lineNumber, "cvc-datatype-valid.1.2.1: '' is not a valid value for 'boolean'.")
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '' of element 'InitialDisclosureMA' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter an InitialDisclosureMA"))
      }

      "must return correct error for invalid boolean value" in {

        val error1 = SaxParseError(lineNumber, "cvc-datatype-valid.1.2.1: 'yes' is not a valid value for 'boolean'.")
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value 'yes' of element 'InitialDisclosureMA' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "InitialDisclosureMA must be true or false"))
      }

      "must return correct error for incorrectly formatted arrangement id" in {

        val error1 = SaxParseError(
          lineNumber,
          "cvc-pattern-valid: Value 'njinjin' is not facet-valid with respect to pattern '[A-Z]{2}[A]([2]\\d{3}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01]))([A-Z0-9]{6})' for type '#AnonType_ArrangementIDDAC6_Arrangement'."
        )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value 'njinjin' of element 'ArrangementID' is not valid.")

        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter ArrangementID in the format CCAYYYYMMDDXXXXXX"))
      }

      "must return correct error for incorrectly formatted disclosure id" in {

        val error1 = SaxParseError(
          lineNumber,
          "cvc-pattern-valid: Value 'rbrbrbrbrb' is not facet-valid with respect to pattern '[A-Z]{2}[D]([2]\\d{3}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01]))([A-Z0-9]{6})' for type '#AnonType_DisclosureIDDAC6Disclosure_Type'."
        )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value 'rbrbrbrbrb' of element 'DisclosureID' is not valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter DisclosureID in the format CCDYYYYMMDDXXXXXX"))
      }

      "must return correct error for missing MessageRefId" in {

        val error1 =
          SaxParseError(lineNumber,
                        "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '2' for type 'StringMin2Max200_Type'."
          )
        val error2 = SaxParseError(lineNumber, "cvc-type.3.1.3: The value '' of element 'MessageRefId' is not valid.")
        val result = helper.generateErrorMessages(ListBuffer(error1, error2))
        result mustBe List(GenericError(lineNumber, "Enter a MessageRefId"))
      }

    }

    "invalidCodeMessage" - {

      "must return correct message for 'Country'" in {
        val result = helper.invalidCodeMessage("Country")
        result mustBe Some("Country is not one of the ISO country codes")
      }

      "must return correct message for 'CountryExemption'" in {
        val result = helper.invalidCodeMessage("CountryExemption")
        result mustBe Some("CountryExemption is not one of the ISO country codes")
      }

      "must return correct message for 'ConcernedMS'" in {
        val result = helper.invalidCodeMessage("ConcernedMS")
        result mustBe Some("ConcernedMS is not one of the ISO EU Member State country codes")
      }

      "must return correct message for 'Reason'" in {
        val result = helper.invalidCodeMessage("Reason")
        result mustBe Some("Reason is not one of the allowed values")
      }

      "must return correct message for 'Capacity'" in {
        val result = helper.invalidCodeMessage("Capacity", Some("(DAC61101, DAC61102)"))
        result mustBe Some("Capacity is not one of the allowed values (DAC61101, DAC61102) for Intermediary")
      }

      "must return correct message for 'IntermediaryNexus'" in {
        val result = helper.invalidCodeMessage("IntermediaryNexus")
        result mustBe Some("IntermediaryNexus is not one of the allowed values")
      }

      "must return correct message for 'RelevantTaxpayerNexus'" in {
        val result = helper.invalidCodeMessage("RelevantTaxpayerNexus")
        result mustBe Some("RelevantTaxpayerNexus is not one of the allowed values")
      }

      "must return correct message for 'Hallmark'" in {
        val result = helper.invalidCodeMessage("Hallmark")
        result mustBe Some("Hallmark is not one of the allowed values")
      }

      "must return correct message for 'ResCountryCode'" in {
        val result = helper.invalidCodeMessage("ResCountryCode")
        result mustBe Some("ResCountryCode is not one of the allowed values")
      }

      "must return None for unexpected elementName" in {
        val result = helper.invalidCodeMessage("Unexpected-name")
        result mustBe None
      }

    }
  }
}
