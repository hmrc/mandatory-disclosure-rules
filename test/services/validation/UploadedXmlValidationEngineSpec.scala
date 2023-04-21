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

package services.validation

import base.SpecBase
import config.AppConfig
import helpers.XmlErrorMessageHelper
import models.submission.{MDR401, MessageSpecData, MultipleNewInformation}
import models.validation._
import org.mockito.ArgumentMatchers.any
import services.DataExtraction

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.xml.Elem

class UploadedXmlValidationEngineSpec extends SpecBase {

  val xsdError                            = "xsd-error"
  val defaultError                        = "There is a problem with this line number"
  val lineNumber                          = 0
  val noErrors: ListBuffer[SaxParseError] = ListBuffer()
  val messageSpecData: MessageSpecData    = MessageSpecData("XBC99999999999", MDR401, 2, "OECD1", MultipleNewInformation)

  val addressError1: SaxParseError = SaxParseError(20,
                                                   "cvc-minLength-valid: Value '' with length = '0' is " +
                                                     "not facet-valid with respect to minLength '1' for type 'StringMin1Max400_Type'."
  )

  val addressError2: SaxParseError = SaxParseError(20, "cvc-type.3.1.3: The value '' of element 'Street' is not valid.")

  val over400  = "a" * 401
  val over4000 = "a" * 4001

  val maxLengthError1 = SaxParseError(
    116,
    s"cvc-maxLength-valid: Value '$over400' with length = '401' is not facet-valid with respect to maxLength '400' for type 'StringMin1Max400_Type'."
  )
  val maxlengthError2 = SaxParseError(116, s"cvc-type.3.1.3: The value '$over400' of element 'BuildingIdentifier' is not valid.")

  val maxLengthError3 = SaxParseError(
    116,
    s"cvc-maxLength-valid: Value '$over4000' with length = '4001' is not facet-valid with respect to maxLength '4000' for type 'StringMin1Max4000_Type'."
  )
  val maxlengthError4 = SaxParseError(116, s"cvc-type.3.1.3: The value '$over4000' of element 'NationalProvision' is not valid.")

  val countryCodeError1 = SaxParseError(
    123,
    "cvc-enumeration-valid: Value 'Invalid code' is not facet-valid with respect to enumeration '[AF, AX, AL, DZ]'. It must be a value from the enumeration."
  )
  val countryCodeError2 = SaxParseError(123, "cvc-type.3.1.3: The value 'Raneevev' of element 'Country' is not valid.")

  val concernedMsError1 = SaxParseError(
    177,
    "cvc-enumeration-valid: Value 'CdvvdvZ' is not facet-valid with respect to enumeration '[AT, SE, GB]'. It must be a value from the enumeration."
  )
  val concernedMsError2 = SaxParseError(177, "cvc-type.3.1.3: The value 'CdvvdvZ' of element 'ConcernedMS' is not valid.")

  val countryExemptionError1 = SaxParseError(
    133,
    "cvc-enumeration-valid: Value 'eevev' is not facet-valid with respect to enumeration '[AF, VE, VN, VG, VI, WF, EH, YE, ZM, ZW, XK, XX]'. It must be a value from the enumeration."
  )
  val countryExemptionError2 = SaxParseError(133, "cvc-type.3.1.3: The value 'eevev' of element 'CountryExemption' is not valid.")

  val reasonError1 = SaxParseError(
    169,
    "cvc-enumeration-valid: Value 'DAC670vdvdvd4' is not facet-valid with respect to enumeration '[DAC6701, DAC6702, DAC6703, DAC6704]'. It must be a value from the enumeration."
  )
  val reasonError2 = SaxParseError(169, "cvc-type.3.1.3: The value 'DAC670vdvdvd4' of element 'Reason' is not valid.")

  val intermediaryCapacityError1 = SaxParseError(
    129,
    "cvc-enumeration-valid: Value 'DAC61102fefef' is not facet-valid with respect to enumeration '[DAC61101, DAC61102]'. It must be a value from the enumeration."
  )
  val intermediaryCapacityError2 = SaxParseError(129, "cvc-type.3.1.3: The value 'DAC61102fefef' of element 'Capacity' is not valid.")

  val relevantTpDiscloserCapacityError1 = SaxParseError(
    37,
    "cvc-enumeration-valid: Value 'DAC61105hhh' is not facet-valid with respect to enumeration '[DAC61104, DAC61105, DAC61106]'. It must be a value from the enumeration."
  )
  val relevantTpDiscloserCapacityError2 = SaxParseError(37, "cvc-type.3.1.3: The value 'DAC61105hhh' of element 'Capacity' is not valid.")

  val missingAddressErrors = ListBuffer(addressError1, addressError2)

  val cityError1 =
    SaxParseError(27, "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'StringMin1Max400_Type'.")
  val cityError2        = SaxParseError(27, "cvc-type.3.1.3: The value '' of element 'City' is not valid.")
  val missingCityErrors = ListBuffer(cityError1, cityError2)

  val invalidAttributeCodeError =
    SaxParseError(175, "cvc-attribute.3: The value 'VUVs' of attribute 'currCode' on element 'Amount' is not valid with respect to its type, 'currCode_Type'.")

  val issuedByError1 =
    SaxParseError(18, "cvc-enumeration-valid: Value 'GBf' is not facet-valid with respect to enumeration '[AF, AX]'. It must be a value from the enumeration.")

  val issuedByError2 =
    SaxParseError(18, "cvc-attribute.3: The value 'GBf' of attribute 'issuedBy' on element 'TIN' is not valid with respect to its type, 'CountryCode_Type'.")

  val typeError1 = SaxParseError(
    176,
    "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[MDR801, MDR802, MDR803, MDR804, MDR805, MDR806]'. It must be a value from the enumeration."
  )

  val typeError2 = SaxParseError(176, "cvc-type.3.1.3: The value '' of element 'Type' is not valid.")

  val summaryError1 =
    SaxParseError(258, "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'StringMin1Max4000_Type'.")

  val summaryError2 = SaxParseError(258, "cvc-complex-type.2.2: Element 'Summary' must have no element [children], and the value must be valid.")

  val enrolmentId = "123456"

  trait SetUp {
    val doesFileHaveBusinessErrors = false

    val mockXmlValidationService: XMLValidationService   = mock[XMLValidationService]
    val mockXmlErrorMessageHelper: XmlErrorMessageHelper = new XmlErrorMessageHelper
    val appConfig: AppConfig                             = app.injector.instanceOf[AppConfig]
    val mockDataExtraction: DataExtraction               = mock[DataExtraction]

    val validationEngine = new UploadedXmlValidationEngine(mockXmlValidationService, mockXmlErrorMessageHelper, mockDataExtraction, appConfig)

    val source        = "src"
    val elem: Elem    = <dummyElement>Test</dummyElement>
    val mockXML: Elem = <DisclosureImportInstruction>DAC6NEW</DisclosureImportInstruction>
  }

  "ValidateUploadSubmission" - {

    "must return UploadSubmissionValidationSuccess when xml with no errors received" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]())).thenReturn(Right(elem))
      when(mockDataExtraction.messageSpecData(any[Elem])).thenReturn(Some(messageSpecData))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationSuccess(messageSpecData)
    }

    "must return ValidationFailure for file which multiple pieces of mandatory information missing" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(typeError1, typeError2, summaryError1, summaryError2)))

      val expectedErrors =
        Seq(GenericError(176, Message("xml.add.a.element", List("Type"))), GenericError(258, Message("xml.add.a.element", List("Summary"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file missing mandatory attributes" in new SetUp {

      val missingAttributeError: SaxParseError = SaxParseError(175, "cvc-complex-type.4: Attribute 'currCode' must appear on element 'Amount'.")

      when(mockXmlValidationService.validate(any[String](), any[String]())).thenReturn(Left(ListBuffer(missingAttributeError)))

      val expectedErrors = Seq(GenericError(175, Message("xml.add.an.element", List("Amount currCode"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file where element is too long (1-400 allowed)" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(maxLengthError1, maxlengthError2)))

      val expectedErrors = Seq(GenericError(116, Message("xml.not.allowed.length", List("BuildingIdentifier", "400"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file where element is too long (1-4000 allowed) and show the number correctly formatted" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(maxLengthError3, maxlengthError4)))

      val expectedErrors = Seq(GenericError(116, Message("xml.not.allowed.length", List("NationalProvision", "4,000"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file with invalid country code" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(countryCodeError1, countryCodeError2)))

      val expectedErrors = Seq(GenericError(123, Message("xml.not.ISO.code", List("Country"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file with invalid countryExemption code" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(countryExemptionError1, countryExemptionError2)))

      val expectedErrors = Seq(GenericError(133, Message("xml.not.ISO.code", List("CountryExemption"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file with invalid Reason entry code" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(reasonError1, reasonError2)))

      val expectedErrors = Seq(GenericError(169, Message("xml.not.allowed.value", List("Reason"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure for file with invalid issuedBy code" in new SetUp {

      when(mockXmlValidationService.validate(any[String](), any[String]()))
        .thenReturn(Left(ListBuffer(issuedByError1, issuedByError2)))

      val expectedErrors = Seq(GenericError(18, Message("xml.not.ISO.code", List("TIN issuedBy"))))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }

    "must return ValidationFailure with generic error message if parse error is not in an expected format" in new SetUp {

      val randomParseError: SaxParseError = SaxParseError(lineNumber, xsdError)
      when(mockXmlValidationService.validate(any[String](), any[String]())).thenReturn(Left(ListBuffer(randomParseError)))

      val expectedErrors = Seq(GenericError(lineNumber, Message("xml.defaultMessage")))

      Await.result(validationEngine.validateUploadSubmission(source), 10.seconds) mustBe SubmissionValidationFailure(ValidationErrors(expectedErrors))
    }
  }
}
