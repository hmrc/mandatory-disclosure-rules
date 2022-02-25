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

package models

import com.lucidchart.open.xtract.XmlReader.strictReadSeq
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, XmlReader}

package object xml {
  implicit def strictReadOptionSeq[A](implicit reader: XmlReader[A]): XmlReader[Option[Seq[A]]] =
    XmlReader { xml =>
      strictReadSeq[A].read(xml) match {
        case ParseSuccess(x) if x.nonEmpty => ParseSuccess(x)
        case errors                        => ParseFailure()
      }
    }.optional
}
