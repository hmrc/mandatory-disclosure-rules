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

package helpers

import models.validation.{GenericError, Message, SaxParseError}

import java.time.format.{DateTimeFormatter, DateTimeParseException}
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Try}

class XmlErrorMessageHelper extends SaxParseErrorRegExConstants {

  val defaultMessage = "xml.defaultMessage"

  def generateErrorMessages(errors: ListBuffer[SaxParseError]): List[GenericError] = {
    val errorsGroupedByLineNumber = errors.groupBy(saxParseError => saxParseError.lineNumber)

    errorsGroupedByLineNumber.map { groupedErrors =>
      if (groupedErrors._2.length <= 2) {
        val error1 = groupedErrors._2.head.errorMessage
        val error2 = groupedErrors._2.last.errorMessage

        val error: Option[Message] = extractMissingElementDeclaration(error1)
          .orElse(extractMissingElementValues(error1, error2))
          .orElse(extractPercentageErrorTagValues(error1, error2))
          .orElse(extractEmptyTagValues(error1, error2))
          .orElse(extractTooLongFieldAttributeValues(error1, error2))
          .orElse(extractInvalidEnumAttributeValues(error1, error2))
          .orElse(extractMaxLengthErrorValues(error1, error2))
          .orElse(extractEnumErrorValues(error1, error2))
          .orElse(extractMissingAttributeValues(groupedErrors._2.head.errorMessage))
          .orElse(extractInvalidIntegerErrorValues(error1, error2))
          .orElse(extractInvalidDateErrorValues(error1, error2))
          .orElse(extractMissingTagValues(error1))
          .orElse(extractEmptyTagValues(error1))
          .orElse(extractUnorderedTagValues(error1))

        GenericError(groupedErrors._1, error.getOrElse(Message(defaultMessage)))
      } else GenericError(groupedErrors._1, Message(defaultMessage))
    }.toList
  }

  def extractMissingAttributeValues(errorMessage: String): Option[Message] =
    errorMessage match {
      case missingAttributeErrorFormat(attribute, element) =>
        Some(missingInfoMessage(element + " " + attribute))
      case _ => None
    }

  def extractTooLongFieldAttributeValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case fieldTooLongErrorFormat(_, _, maxLength, _) =>
        errorMessage2 match {
          case invalidTypeErrorFormat(_, "INType", _, _) =>
            Some(Message("xml.not.allowed.length", Seq("INType", maxLength)))
          case invalidTypeErrorFormat(_, attribute, element, _) =>
            Some(Message("xml.not.allowed.length", Seq(element + " " + attribute, maxLength)))
          case _ => None
        }
      case _ => None
    }

  def extractInvalidEnumAttributeValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case invalidEnumErrorFormat(_, _) =>
        errorMessage2 match {
          case invalidTypeErrorFormat("", attribute, _, _) =>
            Some(Message("xml.optional.field.empty", Seq(attribute)))
          case invalidTypeErrorFormat(_, attribute, element, _) =>
            invalidCodeMessage(element + " " + attribute)
          case _ => None
        }
      case _ => None
    }

  def extractMissingElementDeclaration(errorMessage1: String): Option[Message] = {
    val declaration = "urn:oecd:ties:mdr:v1"

    errorMessage1 match {
      case missingDeclarationErrorFormat(element) =>
        Some(Message("xml.must.have.element.declaration", Seq(element, declaration)))
      case _ => None
    }
  }

  def extractMissingElementValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case emptyTagErrorFormat(_, _) =>
        formattedError(errorMessage2) match {
          case missingOrInvalidErrorFormat("", element) =>
            Some(missingInfoMessage(element))
          case genericInvalidSecondErrorFormat(element) =>
            Some(missingInfoMessage(element))
          case _ => None
        }
      case _ => None
    }

  def extractPercentageErrorTagValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case outOfRangeErrorFormat(_, _, _) | genericInvalidErrorFormat(_, "integer") =>
        formattedError(errorMessage2) match {
          case missingOrInvalidErrorFormat("", "Ownership") =>
            Some(Message("xml.optional.field.empty", Seq("Ownership")))
          case missingOrInvalidErrorFormat(_, element) =>
            Some(Message("xml.not.valid.percentage", Seq(element)))
          case _ => None
        }
      case _ => None
    }

  def extractEmptyTagValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case emptyTagErrorFormat(_, _) =>
        formattedError(errorMessage2) match {
          case missingOrInvalidErrorFormat("", element) =>
            Some(missingInfoMessage(element))
          case _ => None
        }
      case _ => None
    }

  def extractMaxLengthErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val numberFormatter = java.text.NumberFormat.getIntegerInstance

    errorMessage1 match {
      case fieldTooLongErrorFormat(_, _, allowedLength, _) =>
        formattedError(errorMessage2) match {
          case missingOrInvalidErrorFormat(_, "MessageRefId") =>
            Some(Message("xml.not.allowed.length", Seq("MessageRefId", "85")))
          case missingOrInvalidErrorFormat(_, "DocRefId") =>
            Some(Message("xml.not.allowed.length", Seq("DocRefId", "100")))
          case missingOrInvalidErrorFormat(_, element) =>
            Some(Message("xml.not.allowed.length", Seq(element, numberFormatter.format(allowedLength.toInt))))
          case genericInvalidSecondErrorFormat(element) =>
            if (List("Narrative", "Summary", "OtherInfo").contains(element)) {
              Some(Message("xml.not.allowed.length.repeatable", Seq(element, numberFormatter.format(allowedLength.toInt))))
            } else {
              Some(Message("xml.not.allowed.length", Seq(element, numberFormatter.format(allowedLength.toInt))))
            }
          case _ => None
        }
      case _ => None
    }
  }

  def extractEnumErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    formattedError(errorMessage1) match {
      case invalidEnumErrorFormat(suppliedValue, "MDR") =>
        errorMessage2 match {
          case missingOrInvalidErrorFormat(_, element) =>
            if (suppliedValue == "") {
              Some(Message("xml.add.line.messageType", Seq(element)))
            } else {
              Some(Message("xml.add.mdr"))
            }
          case _ => None
        }
      case invalidEnumErrorFormat(suppliedValue, allowedValues) =>
        errorMessage2 match {
          case missingOrInvalidErrorFormat(_, element) =>
            if (suppliedValue.isEmpty) {
              Some(missingInfoMessage(element))
            } else invalidCodeMessage(element, Some(allowedValues))
          case _ => None
        }
      case _ => None
    }

  def extractInvalidIntegerErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case genericInvalidErrorFormat(value, "integer") =>
        formattedError(errorMessage2) match {
          case genericInvalidSecondErrorFormat("InvestAmount") if value == "" =>
            Some(Message("xml.optional.field.empty", Seq("InvestAmount")))
          case genericInvalidSecondErrorFormat(element) =>
            Some(Message("xml.must.be.whole.number", Seq(element)))
          case _ => None
        }
      case _ => None
    }

  def extractInvalidDateErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] =
    errorMessage1 match {
      case genericInvalidErrorFormat(dateStr, attribute) if attribute == "date" || attribute == "dateTime" =>
        errorMessage2 match {
          case missingOrInvalidErrorFormat("", "BirthDate") => Some(Message("xml.optional.field.empty", Seq("BirthDate")))
          case missingOrInvalidErrorFormat(_, element) =>
            Try {
              DateTimeFormatter.ISO_DATE.parse(dateStr)
            } match {
              case Failure(e: DateTimeParseException) =>
                if (e.getMessage contains "could not be parsed at index") {
                  Some(Message(s"xml.$attribute.format", Seq(element)))
                } else {
                  Some(Message("xml.date.format.real", Seq(element)))
                }
              case _ => Some(Message(s"xml.$attribute.format", Seq(element)))
            }
          case _ => None
        }
      case _ => None
    }

  def extractMissingTagValues(errorMessage: String): Option[Message] = {
    val formattedError = errorMessage.replaceAll("[{}]", "")

    formattedError match {
      case missingTagErrorFormat(_, element) if element.contains(":") =>
        val formattedElement = element.replaceAll("""(.*?), "urn:oecd:ties:mdr:v1":""", "")
        getErrorMessageForMissingTags(formattedElement)
      case missingTagErrorFormat(_, element) =>
        getErrorMessageForMissingTags(element)
      case _ => None
    }
  }

  def extractEmptyTagValues(errorMessage: String): Option[Message] = {
    val formattedError = errorMessage.replaceAll("[{}]", "")

    formattedError match {
      case emptySubTagErrorFormat(parent, element) if parent == "Arrangement" | parent == "ID" =>
        val formattedElement = element.replaceAll(", \"urn:oecd:ties:mdr:v1\":", " or ")
        Some(Message("xml.empty.tag", Seq(parent, formattedElement)))
      case emptySubTagErrorFormat(parent, element) =>
        val formattedElement = element.replaceAll("(.*?):", "")
        Some(Message("xml.empty.tag", Seq(parent, formattedElement)))
      case _ => None
    }
  }

  def extractUnorderedTagValues(errorMessage: String): Option[Message] =
    errorMessage match {
      case unorderedTagErrorFormat("AddressFix") =>
        Some(Message("xml.addressFix.error"))
      case _ => None
    }

  private def missingInfoMessage(elementName: String): Message = {
    val vowels = "aeiouAEIOU"
    elementName match {
      case "Jurisdictions" => Message("xml.add.one.or.more.elements", Seq(elementName))
      case "CorrDocRefId" | "Reason" | "OtherInfo" | "PrecedingTitle" | "Title" | "MiddleName" | "NamePrefix" | "GenerationIdentifier" | "Suffix" |
          "GeneralSuffix" | "AddressFree" | "Street" | "BuildingIdentifier" | "SuiteIdentifier" | "FloorIdentifier" | "DistrictName" | "POB" | "PostCode" |
          "CountrySubentity" =>
        Message("xml.optional.field.empty", Seq(elementName))
      case _ if vowels.contains(elementName.head) || elementName.toLowerCase.startsWith("mdr") => Message("xml.add.an.element", Seq(elementName))
      case _                                                                                   => Message("xml.add.a.element", Seq(elementName))
    }
  }

  def invalidCodeMessage(elementName: String, allowedValues: Option[String] = None): Option[Message] =
    (elementName, allowedValues) match {
      case ("Country" | "CountryCode" | "CountryExemption" | "TIN issuedBy" | "IN issuedBy" | "Jurisdictions" | "ResCountryCode" | "TransmittingCountry" |
            "ReceivingCountry",
            _
          ) =>
        Some(Message("xml.not.ISO.code", Seq(elementName)))
      case ("OtherInfo language" | "Narrative language" | "Summary language" | "Name language" | "Address language" | "Language", _) =>
        Some(Message("xml.not.ISO.language.code", Seq(elementName)))
      case ("InvestAmount currCode", _) => Some(Message("xml.not.ISO.currency.code", Seq(elementName)))
      case ("Capacity" | "Nexus" | "Reason" | "MessageTypeIndic" | "ResCountryCode" | "Role" | "Type" | "DocTypeIndic" | "Address legalAddressType" |
            "Name nameType",
            _
          ) =>
        Some(Message("xml.not.allowed.value", Seq(elementName)))
      case _ => None
    }

  private def getErrorMessageForMissingTags(element: String): Option[Message] =
    element match {
      case "ID" | "DocSpec" | "ReportableTaxPayer" | "Structure" | "Address" | "MessageSpec" | "MdrBody" =>
        Some(missingInfoMessage(element))
      case "Disclosing" => Some(Message("xml.add.element", Seq(element)))
      case _            => Some(Message("xml.add.line", Seq(element)))
    }

  private def formattedError(errorMessage: String): String =
    errorMessage.replaceAll("\\[", "").replaceAll("\\]", "")
}
