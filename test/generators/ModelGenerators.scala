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

package generators

import models.email.EmailRequest
import models.subscription.{
  ContactInformation,
  ContactType,
  DisplaySubscriptionDetails,
  DisplaySubscriptionForMDRRequest,
  IndividualDetails,
  OrganisationDetails,
  ReadSubscriptionRequestDetail,
  RequestCommonForSubscription,
  RequestCommonForUpdate,
  RequestDetailForUpdate,
  UpdateSubscriptionDetails,
  UpdateSubscriptionForMDRRequest
}
import models.xml.{FileErrorCode, FileErrors, RecordError, RecordErrorCode, ValidationErrors}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDate

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
  }

  implicit val arbitraryRequestCommonForSubscription: Arbitrary[RequestCommonForSubscription] =
    Arbitrary {
      for {
        receiptDate        <- arbitrary[String]
        acknowledgementRef <- stringsWithMaxLength(32)
      } yield RequestCommonForSubscription(
        regime = "MDR",
        receiptDate = receiptDate,
        acknowledgementReference = acknowledgementRef,
        originatingSystem = "MDTP",
        None
      )
    }

  implicit val arbitraryReadSubscriptionRequestDetail: Arbitrary[ReadSubscriptionRequestDetail] = Arbitrary {
    for {
      idType   <- arbitrary[String]
      idNumber <- arbitrary[String]
    } yield ReadSubscriptionRequestDetail(
      IDType = idType,
      IDNumber = idNumber
    )
  }
  implicit val arbitraryReadSubscriptionForMDRRequest: Arbitrary[DisplaySubscriptionForMDRRequest] =
    Arbitrary {
      for {
        requestCommon <- arbitrary[RequestCommonForSubscription]
        requestDetail <- arbitrary[ReadSubscriptionRequestDetail]
      } yield DisplaySubscriptionForMDRRequest(
        DisplaySubscriptionDetails(requestCommon, requestDetail)
      )
    }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] = Arbitrary {
    for {
      orgName <- arbitrary[String]
    } yield OrganisationDetails(orgName)
  }

  implicit val arbitraryIndividualDetails: Arbitrary[IndividualDetails] = Arbitrary {
    for {
      firstName  <- arbitrary[String]
      middleName <- Gen.option(arbitrary[String])
      lastName   <- arbitrary[String]
    } yield IndividualDetails(firstName, middleName, lastName)
  }

  implicit val arbitraryContactType: Arbitrary[ContactType] = Arbitrary {
    Gen.oneOf[ContactType](arbitrary[OrganisationDetails], arbitrary[IndividualDetails])
  }

  implicit val arbitraryContactInformation: Arbitrary[ContactInformation] = Arbitrary {
    for {
      contactType <- arbitrary[ContactType]
      email       <- arbitrary[String]
      phone       <- Gen.option(arbitrary[String])
      mobile      <- Gen.option(arbitrary[String])
    } yield ContactInformation(contactType, email, phone, mobile)
  }

  implicit val arbitraryRequestDetail: Arbitrary[RequestDetailForUpdate] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield RequestDetailForUpdate(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryRequestCommonForUpdateSubscription: Arbitrary[RequestCommonForUpdate] =
    Arbitrary {
      for {
        receiptDate        <- arbitrary[String]
        acknowledgementRef <- stringsWithMaxLength(32)
      } yield RequestCommonForUpdate(
        regime = "MDR",
        receiptDate = receiptDate,
        acknowledgementReference = acknowledgementRef,
        originatingSystem = "MDTP",
        None
      )
    }

  implicit val arbitraryUpdateSubscriptionRequestDetail: Arbitrary[UpdateSubscriptionDetails] = Arbitrary {
    for {
      reqCommonForUpdate        <- arbitrary[RequestCommonForUpdate]
      reqRequestDetailForUpdate <- arbitrary[RequestDetailForUpdate]
    } yield UpdateSubscriptionDetails(
      reqCommonForUpdate,
      reqRequestDetailForUpdate
    )
  }

  implicit val arbitraryUpdateSubscriptionForMDRRequest: Arbitrary[UpdateSubscriptionForMDRRequest] =
    Arbitrary {
      for {
        request <- arbitrary[UpdateSubscriptionDetails]
      } yield UpdateSubscriptionForMDRRequest(
        request
      )
    }

  implicit val arbitraryFileErrorCode: Arbitrary[FileErrorCode] = Arbitrary {
    Gen.oneOf[FileErrorCode](FileErrorCode.values)
  }

  implicit val arbitraryRecordErrorCode: Arbitrary[RecordErrorCode] = Arbitrary {
    Gen.oneOf[RecordErrorCode](RecordErrorCode.values)
  }

  implicit val arbitraryUpdateFileErrors: Arbitrary[FileErrors] = Arbitrary {
    for {
      fileErrorCode <- arbitrary[FileErrorCode]
      details       <- Gen.option(nonEmptyString)
    } yield FileErrors(fileErrorCode, details)
  }

  implicit val arbitraryUpdateRecordErrors: Arbitrary[RecordError] = Arbitrary {
    for {
      recordErrorCode <- arbitrary[RecordErrorCode]
      details         <- Gen.option(nonEmptyString)
      docRefIdRef     <- Gen.option(listWithMaxLength(5, nonEmptyString))
    } yield RecordError(recordErrorCode, details, docRefIdRef)
  }

  implicit val arbitraryUpdateValidationErrors: Arbitrary[ValidationErrors] =
    Arbitrary {
      for {
        fileErrors   <- Gen.option(listWithMaxLength(5, arbitrary[FileErrors]))
        recordErrors <- Gen.option(listWithMaxLength(5, arbitrary[RecordError]))
      } yield ValidationErrors(fileErrors, recordErrors)
    }

  implicit val arbitraryEmailRequest: Arbitrary[EmailRequest] = Arbitrary {
    for {
      to     <- arbitrary[List[String]]
      id     <- arbitrary[String]
      params <- arbitrary[Map[String, String]]

    } yield EmailRequest(to, id, params)
  }
}
