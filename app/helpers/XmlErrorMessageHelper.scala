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

package helpers

import models.validation.{GenericError, Message, SaxParseError}

import scala.collection.mutable.ListBuffer

class XmlErrorMessageHelper {

  val defaultMessage = "xml.defaultMessage"

  def generateErrorMessages(errors: ListBuffer[SaxParseError]): List[GenericError] = {
    val errorsGroupedByLineNumber = errors.groupBy(saxParseError => saxParseError.lineNumber)
    println("****************************************")
    println("****************************************")
    println(errorsGroupedByLineNumber)
    println("****************************************")
    println("****************************************")
    errorsGroupedByLineNumber.map { groupedErrors =>
      if (groupedErrors._2.length <= 2) {
        val error1 = groupedErrors._2.head.errorMessage
        val error2 = groupedErrors._2.last.errorMessage

        val error: Option[Message] = extractMissingElementValues(error1, error2)
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
          .orElse(extractBooleanErrorValues(error1, error2))

        GenericError(groupedErrors._1, error.getOrElse(Message(defaultMessage)))
      } else GenericError(groupedErrors._1, Message(defaultMessage))
    }.toList

  }

  def extractMissingAttributeValues(errorMessage: String): Option[Message] = {
    val format = """cvc-complex-type.4: Attribute '(.*?)' must appear on element '(.*?)'.""".stripMargin.r

    errorMessage match {
      case format(attribute, element) =>
        Some(missingInfoMessage(element + " " + attribute))
      case _ => None
    }
  }

  def extractTooLongFieldAttributeValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formatOfFirstError =
      """cvc-maxLength-valid: Value '((?s).*)' with length = '(.*?)' is not facet-valid with respect to maxLength '(.*?)' for type '(.*?)'.""".stripMargin.r
    val formatOfSecondError =
      """cvc-attribute.3: The value '((?s).*)' of attribute '(.*?)' on element '(.*?)' is not valid with respect to its type, '(.*?)'.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_, _, maxLength, _) =>
        errorMessage2 match {
          case formatOfSecondError(_, "INType", _, _) =>
            Some(Message("xml.not.allowed.length", Seq("INType", maxLength)))
          case formatOfSecondError(_, attribute, element, _) =>
            Some(Message("xml.not.allowed.length", Seq(element + " " + attribute, maxLength)))
          case _ => None
        }
      case _ => None

    }
  }

  def extractInvalidEnumAttributeValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formatOfFirstError =
      """cvc-enumeration-valid: Value '((?s).*)' is not facet-valid with respect to enumeration '(.*?)'. It must be a value from the enumeration.""".stripMargin.r
    val formatOfSecondError =
      """cvc-attribute.3: The value '((?s).*)' of attribute '(.*?)' on element '(.*?)' is not valid with respect to its type, '(.*?)'.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_, _) =>
        errorMessage2 match {
          case formatOfSecondError(_, attribute, element, _) =>
            invalidCodeMessage(element + " " + attribute)
          case _ => None
        }
      case _ => None

    }
  }

  def extractMissingElementValues(errorMessage1: String, errorMessage2: String): Option[Message] = {

    val formattedError = errorMessage2.replaceAll("\\[", "").replaceAll("\\]", "")
    val formatOfFirstError =
      """cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type '(.*?)'.""".stripMargin.r
    val formatOfSecondError            = """cvc-type.3.1.3: The value '' of element '(.*?)' is not valid.""".stripMargin.r
    val formatOfAlternativeSecondError = """cvc-complex-type.2.2: Element '(.*?)' must have no element children, and the value must be valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_) =>
        formattedError match {
          case formatOfSecondError(element) =>
            Some(missingInfoMessage(element))
          case formatOfAlternativeSecondError(element) =>
            Some(missingInfoMessage(element))
          case _ => None
        }
      case _ => None
    }
  }

  def extractPercentageErrorTagValues(errorMessage1: String, errorMessage2: String): Option[Message] = {

    val formattedError = errorMessage2.replaceAll("\\[", "").replaceAll("\\]", "")
    val formatOfFirstError =
      """cvc-maxInclusive-valid: Value '((?s).*)' is not facet-valid with respect to maxInclusive '(.*?)' for type '(.*?)'.""".stripMargin.r
    val formatOfAlternativeFirstError = """cvc-datatype-valid.1.2.1: '(.*?)' is not a valid value for 'integer'.""".stripMargin.r

    val formatOfSecondError = """cvc-type.3.1.3: The value '((?s).*)' of element '(.*?)' is not valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_, _, _) =>
        formattedError match {
          case formatOfSecondError(_, element) =>
            Some(Message("xml.not.valid.percentage", Seq(element)))
          case _ => None
        }
      case formatOfAlternativeFirstError(_) =>
        formattedError match {
          case formatOfSecondError(_, element) =>
            Some(Message("xml.not.valid.percentage", Seq(element)))
          case _ => None
        }
      case _ => None
    }
  }

  def extractEmptyTagValues(errorMessage1: String, errorMessage2: String): Option[Message] = {

    val formattedError = errorMessage2.replaceAll("\\[", "").replaceAll("\\]", "")
    val formatOfFirstError =
      """cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '(.*?)' for type '(.*?)'.""".stripMargin.r
    val formatOfSecondError = """cvc-type.3.1.3: The value '' of element '(.*?)' is not valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_, _) =>
        formattedError match {
          case formatOfSecondError(element) =>
            Some(missingInfoMessage(element))
          case _ => None
        }
      case _ => None
    }
  }

  def extractMaxLengthErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formattedError = errorMessage2.replaceAll("\\[", "").replaceAll("\\]", "")
    val formatOfFirstError =
      """cvc-maxLength-valid: Value '((?s).*)' with length = '(.*?)' is not facet-valid with respect to maxLength '(.*?)' for type '(.*?)'.""".stripMargin.r
    val formatOfSecondError = """cvc-type.3.1.3: The value '((?s).*)' of element '(.*?)' is not valid.""".stripMargin.r

    val formatOfAlternativeSecondError = """cvc-complex-type.2.2: Element '(.*?)' must have no element children, and the value must be valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_, _, allowedLength, _) =>
        formattedError match {
          case formatOfSecondError(_, element) =>
            Some(Message("xml.not.allowed.length", Seq(element, allowedLength)))
          case formatOfAlternativeSecondError(element) =>
            Some(Message("xml.not.allowed.length", Seq(element, allowedLength)))
          case _ => None
        }
      case _ => None
    }
  }

  def extractEnumErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formattedError = errorMessage1.replaceAll("\\[", "(").replaceAll("\\]", ")")

    val formatOfFirstError =
      """cvc-enumeration-valid: Value '((?s).*)' is not facet-valid with respect to enumeration '(.*?)'. It must be a value from the enumeration.""".stripMargin.r
    val formatOfSecondError = """cvc-type.3.1.3: The value '((?s).*)' of element '(.*?)' is not valid.""".stripMargin.r

    formattedError match {
      case formatOfFirstError("", "(MDR)") =>
        errorMessage2 match {
          case formatOfSecondError(_, element) =>
            Some(Message("xml.add.line.messageType", Seq(element)))
        }
      case formatOfFirstError(suppliedValue, allowedValues) =>
        errorMessage2 match {
          case formatOfSecondError(_, element) =>
            if (suppliedValue.isEmpty) {
              Some(missingInfoMessage(element))
            } else invalidCodeMessage(element, Some(allowedValues))
          case _ => None
        }
      case _ => None
    }
  }

  def extractBooleanErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formatOfFirstError  = """cvc-datatype-valid.1.2.1: '(.*?)' is not a valid value for 'boolean'.""".stripMargin.r
    val formatOfSecondError = """cvc-type.3.1.3: The value '((?s).*)' of element '(.*?)' is not valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_) =>
        errorMessage2 match {
          case formatOfSecondError(entry, element) =>
            val displayName = if (element.equals("AffectedPerson")) {
              "AssociatedEnterprise/AffectedPerson"
            } else element

            if (entry.isEmpty) {
              Some(missingInfoMessage(displayName))
            } else Some(Message("xml.must.be.boolean", Seq(displayName)))
          case _ => None
        }
      case _ => None
    }
  }

  def extractInvalidIntegerErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formatOfFirstError  = """cvc-datatype-valid.1.2.1: '(.*?)' is not a valid value for 'integer'.""".stripMargin.r
    val formatOfSecondError = """cvc-complex-type.2.2: Element '(.*?)' must have no element (.*?), and the value must be valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_) =>
        errorMessage2 match {
          case formatOfSecondError(element, _) =>
            Some(Message("xml.must.be.whole.number", Seq(element)))
          case _ => None
        }
      case _ => None
    }
  }

  def extractInvalidDateErrorValues(errorMessage1: String, errorMessage2: String): Option[Message] = {
    val formatOfFirstError  = """cvc-datatype-valid.1.2.1: '(.*?)' is not a valid value for 'date'.""".stripMargin.r
    val formatOfSecondError = """cvc-type.3.1.3: The value '((?s).*)' of element '(.*?)' is not valid.""".stripMargin.r

    errorMessage1 match {
      case formatOfFirstError(_) =>
        errorMessage2 match {
          case formatOfSecondError(_, element) =>
            Some(Message("xml.date.format", Seq(element)))
          case _ => None
        }
      case _ => None
    }
  }

  def extractMissingTagValues(errorMessage: String): Option[Message] = {

    val formattedError = errorMessage.replaceAll("[{}]", "")
    val format =
      """cvc-complex-type.2.4.a: Invalid content was found starting with element '(.*?)'. One of '"urn:oecd:ties:mdr:v1":(.*?)' is expected.""".stripMargin.r

    formattedError match {
      case format(_, element) if element.contains(":") =>
        val formattedElement = element.replaceAll("""(.*?), "urn:oecd:ties:mdr:v1":""", "")
        getErrorMessageForMissingTags(formattedElement)
      case format(_, element) =>
        getErrorMessageForMissingTags(element)
      case _ => None
    }
  }

  def extractEmptyTagValues(errorMessage: String): Option[Message] = {

    val formattedError = errorMessage.replaceAll("[{}]", "")
    val format =
      """cvc-complex-type.2.4.b: The content of element '(.*?)' is not complete. One of '"urn:oecd:ties:mdr:v1":(.*?)' is expected.""".stripMargin.r

    formattedError match {
      case format("Arrangement", element) =>
        val formattedElement = element.replaceAll(", \"urn:oecd:ties:mdr:v1\":", " or ")
        Some(Message("xml.empty.tag", Seq("Arrangement", formattedElement)))
      case format("ID", element) =>
        val formattedElement = element.replaceAll(", \"urn:oecd:ties:mdr:v1\":", " or ")
        Some(Message("xml.empty.tag", Seq("ID", formattedElement)))
      case format(parent, element) =>
        val formattedElement = element.replaceAll("(.*?):", "")
        Some(Message("xml.empty.tag", Seq(parent, formattedElement)))
      case _ => None
    }
  }

  private def missingInfoMessage(elementName: String): Message = {
    val vowels = "aeiouAEIOU"
    if (vowels.contains(elementName.head) || elementName.toLowerCase.startsWith("mdr")) {
      Message("xml.add.an.element", Seq(elementName))
    } else if (elementName.contains("Jurisdictions")) {
      Message("xml.add.one.or.more.elements", Seq(elementName))
    } else {
      Message("xml.add.a.element", Seq(elementName))
    }
  }

  def invalidCodeMessage(elementName: String, allowedValues: Option[String] = None): Option[Message] =
    (elementName, allowedValues) match {
      case ("Country" | "CountryExemption" | "TIN issuedBy", _) => Some(Message("xml.not.ISO.code", Seq(elementName)))
      case ("ConcernedMS", _)                                   => Some(Message("xml.not.ISO.code.concernedMS"))
      case ("Capacity" | "Nexus" | "Reason" | "RelevantTaxpayerNexus" | "Hallmark" | "ResCountryCode", _) =>
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
}
