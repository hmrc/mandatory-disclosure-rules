/*
 * Copyright 2025 HM Revenue & Customs
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

package models.xml

import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.json._
import scala.xml.NodeSeq
import XmlPrimitiveReads._

class XmlPrimitiveReadsSpec extends AnyFunSuite {

  test("stringReads should read non-empty text") {
    val xml: NodeSeq = <value>Hello</value>

    val result = XmlReads[String].reads(xml)

    assert(result == JsSuccess("Hello"))
  }

  test("stringReads should fail on empty text") {
    val xml: NodeSeq = <value>  </value>

    val result = XmlReads[String].reads(xml)

    assert(result.isInstanceOf[JsError])
  }

  test("intReads should parse valid integers") {
    val xml: NodeSeq = <age>42</age>

    val result = XmlReads[Int].reads(xml)

    assert(result == JsSuccess(42))
  }

  test("intReads should fail on invalid integers") {
    val xml: NodeSeq = <age>abc</age>

    val result = XmlReads[Int].reads(xml)

    assert(result.isInstanceOf[JsError])
  }

  test("intReads should fail on empty value") {
    val xml: NodeSeq = <age></age>

    val result = XmlReads[Int].reads(xml)

    assert(result.isInstanceOf[JsError])
  }

  test("booleanReads should parse valid boolean values") {
    val overThreshold: NodeSeq = <overThreshold>true</overThreshold>
    val bookAvailable: NodeSeq = <bookAvailable>false</bookAvailable>
    val edible: NodeSeq        = <edible>1</edible>
    val displayable: NodeSeq   = <edible>0</edible>

    val overThresholdResult    = XmlReads[Boolean].reads(overThreshold)
    val bookAvailableThreshold = XmlReads[Boolean].reads(bookAvailable)
    val edibleResult           = XmlReads[Boolean].reads(edible)
    val displayableResult      = XmlReads[Boolean].reads(displayable)

    assert(overThresholdResult == JsSuccess(true))
    assert(bookAvailableThreshold == JsSuccess(false))
    assert(edibleResult == JsSuccess(true))
    assert(displayableResult == JsSuccess(false))
  }
}
