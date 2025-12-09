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

import scala.xml.NodeSeq

object XmlPath {

  final case class Path(run: NodeSeq => NodeSeq) { self =>

    def \(label: String): Path =
      Path(ns => self.run(ns) \ label)

    def read[A](implicit xr: XmlReads[A]): XmlReads[A] =
      XmlReads.from { root =>
        val selected = run(root)
        xr.reads(selected)
      }

    def readOpt[A](implicit xr: XmlReads[A]): XmlReads[Option[A]] =
      XmlReads.from { root =>
        val selected = run(root)
        if (selected.isEmpty || selected.text.trim.isEmpty)
          JsSuccess(None)
        else
          xr.reads(selected).map(Some(_))
      }

    def readsList[A](implicit xr: XmlReads[A]): XmlReads[List[A]] =
      XmlReads.from { root =>
        val nodes = run(root)

        if (nodes.isEmpty) {
          JsSuccess(Nil)
        } else {
          val results = nodes.map(node => xr.reads(node))
          val errors  = results.collect { case JsError(errs) => errs }.flatten
          if (errors.nonEmpty) {
            JsError(errors)
          } else {
            JsSuccess(results.collect { case JsSuccess(v, _) => v }.toList)
          }
        }
      }

    def readListOpt[A](implicit xr: XmlReads[A]): XmlReads[Option[List[A]]] =
      XmlReads.from { root =>
        val nodes = run(root)
        if (nodes.isEmpty) {
          JsSuccess(None)
        } else {
          readsList[A].reads(root).map(Some(_))
        }
      }

  }

  val __ : Path = Path(identity)
}
