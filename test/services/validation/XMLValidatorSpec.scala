/*
 * Copyright 2021 HM Revenue & Customs
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

package services.validation

import base.SpecBase
import org.apache.xerces.stax.ImmutableLocation
import org.codehaus.stax2.validation.XMLValidationProblem
import play.api.Configuration
import schemas.DAC6XMLSchema

class XMLValidatorSpec extends SpecBase {

  val application = applicationBuilder()
    .configure(
      Configuration(
        "metrics.enabled"           -> "false",
        "xml.validation.max-errors" -> 3
      )
    )
    .build()
  val service   = application.injector.instanceOf[XMLValidator]
  val xmlSchema = application.injector.instanceOf[DAC6XMLSchema]

  "XMLValidator" - {
    "must validate Valid file" in {
      val xmlHandler: XmlErrorHandler = service.validateSchema(
        getClass.getResource("/valid.xml"),
        xmlSchema.xmlValidationSchema
      )
      xmlHandler.hasErrors mustBe false
    }
    "must return a ValidationFailure with errors for invalid file" in {

      val result = service.validateSchema(
        getClass.getResource("/invalid.xml"),
        xmlSchema.xmlValidationSchema
      )

      result.errorsCollection.length mustBe 2
    }
    "must only return a max of 3 errors for manyinvalid file" in {
      val result = service.validateSchema(
        getClass.getResource("/manyinvalid.xml"),
        xmlSchema.xmlValidationSchema
      )

      result.errorsCollection.length mustBe 3
    }
  }

  "XmlErrorHandler" - {

    def addException(
      errorMessage: String,
      line: Int,
      column: Int,
      severity: Int
    ) =
      new XMLValidationProblem(
        new ImmutableLocation(0, column, line, "", ""),
        errorMessage,
        severity
      )

    val warnings: List[XMLValidationProblem] =
      List(
        addException("WarningOne", 5, 2, 1),
        addException("WarningTwo", 13, 14, 1)
      )
    val errors: List[XMLValidationProblem] = List(
      addException("ErrorOne", 5, 2, 2),
      addException("ErrorTwo", 13, 14, 2)
    )
    val fatalErrors: List[XMLValidationProblem] =
      List(addException("FatalErrorOne", 5, 2, 3))

    "must report multiple errors and multiple warnings" in {
      val xmlErorHandler = new XmlErrorHandler(4)

      warnings.foreach(spe => xmlErorHandler.reportProblem(spe))
      errors.foreach(spe => xmlErorHandler.reportProblem(spe))
      fatalErrors.foreach(spe => xmlErorHandler.reportProblem(spe))

      xmlErorHandler.hasErrors mustBe true
      xmlErorHandler.errorsCollection.size mustBe errors.size

      for (i <- errors.indices) {
        val message =
          s"${errors(i).getMessage} on line ${errors(i).getLocation.getLineNumber}"
        assert(message == xmlErorHandler.errorsCollection(i))
      }

      xmlErorHandler.hasWarnings mustBe true
      xmlErorHandler.warningsCollection.size mustBe warnings.size

      for (i <- warnings.indices) {
        val message =
          s"${warnings(i).getMessage} on line ${warnings(i).getLocation.getLineNumber}"
        assert(message == xmlErorHandler.warningsCollection(i))
      }

      xmlErorHandler.hasFatalErrors mustBe true
      xmlErorHandler.fatalErrorsCollection.size mustBe fatalErrors.size

      for (i <- fatalErrors.indices) {
        val message =
          s"${fatalErrors(i).getMessage} on line ${fatalErrors(i).getLocation.getLineNumber}"
        assert(message == xmlErorHandler.fatalErrorsCollection(i))
      }
    }
  }
}
