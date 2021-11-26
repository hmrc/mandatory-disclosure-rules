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
import models.validation.{UploadSubmissionValidationFailure, UploadSubmissionValidationInvalid, UploadSubmissionValidationSuccess, ValidationErrors}
import org.codehaus.stax2.validation.XMLValidationSchema
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import schemas.{DAC6XMLSchema, XMLSchema}

import scala.concurrent.ExecutionContext.Implicits.global
import java.net.{ConnectException, URL}

class UploadSubmissionValidationEngineSpec extends SpecBase with BeforeAndAfterEach {

  val mockXmlValidation = mock[XMLValidator]
  val mockXMLSchema     = mock[DAC6XMLSchema]

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[XMLValidator].toInstance(mockXmlValidation),
        bind[XMLSchema].toInstance(mockXMLSchema)
      )
      .build()

  override def beforeEach(): Unit = {
    reset(mockXmlValidation)
    reset(mockXMLSchema)
  }

  val uploadSubmissionValidationEngine: UploadSubmissionValidationEngine = application.injector.instanceOf[UploadSubmissionValidationEngine]

  "UploadSubmissionValidationEngine" - {
    "must return UploadSubmissionValidationSuccess when the xml conforms to the schema" in {
      val url = "http:/localhost/"

      val mockXMLErrorHandler = mock[XmlErrorHandler]
      when(mockXMLErrorHandler.hasErrors).thenReturn(false)
      when(mockXMLErrorHandler.hasFatalErrors).thenReturn(false)
      when(mockXMLErrorHandler.hasWarnings).thenReturn(false)

      when(mockXmlValidation.validateSchema(any[URL], any[XMLValidationSchema])).thenReturn(mockXMLErrorHandler)

      val result = uploadSubmissionValidationEngine.validateUploadSubmission(Some(url))

      result.futureValue mustBe Some(UploadSubmissionValidationSuccess(true))
    }

    "must return UploadSubmissionValidationFailure when the validation fails" in {
      val url = "http:/localhost/"

      val mockXMLErrorHandler = mock[XmlErrorHandler]
      when(mockXMLErrorHandler.hasErrors).thenReturn(true)
      when(mockXMLErrorHandler.hasFatalErrors).thenReturn(false)
      when(mockXMLErrorHandler.hasWarnings).thenReturn(false)
      when(mockXMLErrorHandler.errorsCollection).thenReturn(List("error1", "error2"))
      when(mockXMLErrorHandler.warningsCollection).thenReturn(List())
      when(mockXMLErrorHandler.fatalErrorsCollection).thenReturn(List())

      when(mockXmlValidation.validateSchema(any[URL], any[XMLValidationSchema])).thenReturn(mockXMLErrorHandler)

      val result = uploadSubmissionValidationEngine.validateUploadSubmission(Some(url))

      result.futureValue mustBe Some(UploadSubmissionValidationFailure(ValidationErrors(Seq("error1", "error2"))))

    }
  }

  //ToDo implement when errors are implemented
  "must return UploadSubmissionValidationInvalid otherwise" ignore {}

  "must return UploadSubmissionValidationInvalid when unknown exception occurs" in {

    val url = "http:/localhost/"

    when(mockXmlValidation.validateSchema(any[URL], any[XMLValidationSchema])).thenThrow(new RuntimeException())

    val result = uploadSubmissionValidationEngine.validateUploadSubmission(Some(url))

    result.futureValue mustBe Some(UploadSubmissionValidationInvalid())
  }

  "must return None when a ConnectionException occurs" in {
    val url = "http:/localhost/"

    when(mockXmlValidation.validateSchema(any[URL], any[XMLValidationSchema])).thenThrow(new ConnectException())

    val result = uploadSubmissionValidationEngine.validateUploadSubmission(Some(url))

    result.futureValue mustBe None
  }

}
