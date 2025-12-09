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

import play.api.libs.json.{JsError, JsResult, JsSuccess}

import scala.xml.NodeSeq

trait XmlReads[A] {
  def reads(xml: NodeSeq): JsResult[A]

  def map[B](f: A => B): XmlReads[B] =
    XmlReads.from(xml => reads(xml).map(f))

  def flatMap[B](f: A => XmlReads[B]): XmlReads[B] =
    XmlReads.from { xml =>
      reads(xml) match {
        case JsSuccess(a, p) => f(a).reads(xml)
        case e @ JsError(_)  => e
      }
    }
}

object XmlReads {
  def apply[A](implicit xr: XmlReads[A]): XmlReads[A] = xr

  def from[A](f: NodeSeq => JsResult[A]): XmlReads[A] = { (xml: NodeSeq) =>
    f(xml)
  }
}
