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

import models.xml.XmlPath._
import models.xml.XmlPrimitiveReads._
import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.json.JsSuccess

class XmlPathSpec extends AnyFunSuite {

  private val xml =
    <root>
      <user>
        <name>Alice</name>
        <age>30</age>
        <nickname>Al</nickname>
      </user>
      <errors>
        <RecordError><Code>1</Code></RecordError>
        <RecordError><Code>2</Code></RecordError>
      </errors>
    </root>

  test("Path read should select and read a single element") {
    val reads = (__ \ "user" \ "name").read[String]

    val result = reads.reads(xml)

    assert(result == JsSuccess("Alice"))
  }

  test("Path readOpt should return Some(value) when present") {
    val reads = (__ \ "user" \ "nickname").readOpt[String]

    val result = reads.reads(xml)

    assert(result == JsSuccess(Some("Al")))
  }

  test("Path readOpt should return None when element is missing") {
    val reads = (__ \ "user" \ "missing").readOpt[String]

    val result = reads.reads(xml)

    assert(result == JsSuccess(None))
  }

  test("Path readList should read multiple child elements") {
    case class SimpleError(code: String)

    val simpleErrorReads: XmlReads[SimpleError] =
      (__ \ "Code").read[String].map(c => SimpleError(c))

    val reads = (__ \ "errors" \ "RecordError").readsList[SimpleError](simpleErrorReads)

    val result = reads.reads(xml)

    assert(result == JsSuccess(List(SimpleError("1"), SimpleError("2"))))
  }
}
