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

trait SaxParseErrorRegExConstants {
  final val missingDeclarationErrorFormat =
    """cvc-elt.1.a: Cannot find the declaration of element '(?:mdr:)?(.*?)'.""".stripMargin.r

  final val missingAttributeErrorFormat = """cvc-complex-type.4: Attribute '(.*?)' must appear on element '(?:mdr:)?(.*?)'.""".stripMargin.r

  final val missingOrInvalidErrorFormat = """cvc-type.3.1.3: The value '((?s).*)' of element '(?:mdr:)?(.*?)' is not valid.""".stripMargin.r

  final val emptyTagErrorFormat =
    """cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '(.*?)' for type '(.*?)'.""".stripMargin.r

  final val emptySubTagErrorFormat =
    """cvc-complex-type.2.4.b: The content of element '(?:mdr:)?(.*?)' is not complete. One of '"urn:oecd:ties:mdr:v1":(?:mdr:)?(.*?)' is expected.""".stripMargin.r

  final val missingTagErrorFormat =
    """cvc-complex-type.2.4.a: Invalid content was found starting with element '(?:mdr:)?(.*?)'. One of '"urn:oecd:ties:mdr:v1":(?:mdr:)?(.*?)' is expected.""".stripMargin.r

  final val fieldTooLongErrorFormat =
    """cvc-maxLength-valid: Value '((?s).*)' with length = '(.*?)' is not facet-valid with respect to maxLength '(.*?)' for type '(.*?)'.""".stripMargin.r

  final val invalidTypeErrorFormat =
    """cvc-attribute.3: The value '((?s).*)' of attribute '(.*?)' on element '(?:mdr:)?(.*?)' is not valid with respect to its type, '(.*?)'.""".stripMargin.r

  final val invalidEnumErrorFormat =
    """cvc-enumeration-valid: Value '((?s).*)' is not facet-valid with respect to enumeration '(.*?)'. It must be a value from the enumeration.""".stripMargin.r

  final val genericInvalidErrorFormat = """cvc-datatype-valid.1.2.1: '((?s).*)' is not a valid value for '(.*?)'.""".stripMargin.r

  final val genericInvalidSecondErrorFormat =
    """cvc-complex-type.2.2: Element '(?:mdr:)?(.*?)' must have no element children, and the value must be valid.""".stripMargin.r

  final val outOfRangeErrorFormat =
    """cvc-maxInclusive-valid: Value '((?s).*)' is not facet-valid with respect to maxInclusive '(.*?)' for type '(.*?)'.""".stripMargin.r

  final val unorderedTagErrorFormat =
    """cvc-complex-type.2.4.d: Invalid content was found starting with element '(?:mdr:)?(.*?)'. No child element is expected at this point.""".stripMargin.r
}
