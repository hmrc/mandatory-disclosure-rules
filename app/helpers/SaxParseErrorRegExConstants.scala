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

trait SaxParseErrorRegExConstants {
  val missingDeclarationErrorFormat =
    """cvc-elt.1: Cannot find the declaration of element '(.*?)'.""".stripMargin.r

  val missingAttributeErrorFormat = """cvc-complex-type.4: Attribute '(.*?)' must appear on element '(.*?)'.""".stripMargin.r

  val missingOrInvalidErrorFormat = """cvc-type.3.1.3: The value '((?s).*)' of element '(.*?)' is not valid.""".stripMargin.r

  val emptyTagErrorFormat =
    """cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '(.*?)' for type '(.*?)'.""".stripMargin.r

  val emptySubTagErrorFormat =
    """cvc-complex-type.2.4.b: The content of element '(.*?)' is not complete. One of '"urn:oecd:ties:mdr:v1":(.*?)' is expected.""".stripMargin.r

  val missingTagErrorFormat =
    """cvc-complex-type.2.4.a: Invalid content was found starting with element '(.*?)'. One of '"urn:oecd:ties:mdr:v1":(.*?)' is expected.""".stripMargin.r

  val fieldTooLongErrorFormat =
    """cvc-maxLength-valid: Value '((?s).*)' with length = '(.*?)' is not facet-valid with respect to maxLength '(.*?)' for type '(.*?)'.""".stripMargin.r

  val invalidTypeErrorFormat =
    """cvc-attribute.3: The value '((?s).*)' of attribute '(.*?)' on element '(.*?)' is not valid with respect to its type, '(.*?)'.""".stripMargin.r

  val invalidEnumErrorFormat =
    """cvc-enumeration-valid: Value '((?s).*)' is not facet-valid with respect to enumeration '(.*?)'. It must be a value from the enumeration.""".stripMargin.r

  val genericInvalidErrorFormat = """cvc-datatype-valid.1.2.1: '((?s).*)' is not a valid value for '(.*?)'.""".stripMargin.r

  val genericInvalidSecondErrorFormat = """cvc-complex-type.2.2: Element '(.*?)' must have no element children, and the value must be valid.""".stripMargin.r

  val outOfRangeErrorFormat =
    """cvc-maxInclusive-valid: Value '((?s).*)' is not facet-valid with respect to maxInclusive '(.*?)' for type '(.*?)'.""".stripMargin.r

  val unorderedTagErrorFormat =
    """cvc-complex-type.2.4.d: Invalid content was found starting with element '(.*?)'. No child element is expected at this point.""".stripMargin.r
}
