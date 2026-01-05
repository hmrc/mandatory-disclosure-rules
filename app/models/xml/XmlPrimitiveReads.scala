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

import play.api.libs.json.{JsError, JsSuccess}

object XmlPrimitiveReads {

  implicit val stringReads: XmlReads[String] =
    XmlReads.from { ns =>
      val txt = ns.text.trim
      if (txt.nonEmpty) JsSuccess(txt)
      else JsError("error.required")
    }

  implicit val intReads: XmlReads[Int] =
    XmlReads.from { ns =>
      val txt = ns.text.trim
      if (txt.isEmpty) JsError("error.required")
      else
        try JsSuccess(txt.toInt)
        catch { case _: NumberFormatException => JsError("error.numberformat") }
    }

  implicit val booleanReads: XmlReads[Boolean] =
    XmlReads.from { ns =>
      ns.text.trim.toLowerCase match {
        case "true" | "1"  => JsSuccess(true)
        case "false" | "0" => JsSuccess(false)
        case ""            => JsError("error.required")
        case other         => JsError(s"error.boolean($other)")
      }
    }

  def enumReads[E <: Enumeration](enumeration: E): XmlReads[enumeration.Value] =
    XmlReads.from { ns =>
      val txt = ns.text.trim
      enumeration.values.find(_.toString == txt) match {
        case Some(v) => JsSuccess(v)
        case None    => JsError(s"error.enum.invalid: $txt")
      }
    }
}
