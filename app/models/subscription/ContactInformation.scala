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

package models.subscription

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{__, Json, OWrites, Reads, Writes}
sealed trait ContactType

object ContactType {

  implicit lazy val reads: Reads[ContactType] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {
      def or[B >: A](b: Reads[B]): Reads[B] =
        a.map[B](identity).orElse(b)
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    OrganisationDetails.reads or
      IndividualDetails.reads
  }

  implicit val writes: Writes[ContactType] = Writes[ContactType] {
    case o: OrganisationDetails => Json.toJson(o)
    case i: IndividualDetails   => Json.toJson(i)
  }
}

case class OrganisationDetails(organisationName: String) extends ContactType

object OrganisationDetails {

  implicit lazy val reads: Reads[OrganisationDetails] = {
    import play.api.libs.functional.syntax._
    (__ \ "organisation" \ "organisationName").read[String] fmap OrganisationDetails.apply
  }

  implicit val writes: Writes[OrganisationDetails] =
    (__ \ "organisation" \ "organisationName").write[String] contramap unlift(OrganisationDetails.unapply)

  def convertTo(contactName: Option[String]): Option[OrganisationDetails] =
    contactName.map(OrganisationDetails(_))
}

case class IndividualDetails(firstName: String, middleName: Option[String], lastName: String) extends ContactType

object IndividualDetails {
  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[IndividualDetails] =
    (
      (__ \ "individual" \ "firstName").read[String] and
        (__ \ "individual" \ "middleName").readNullable[String] and
        (__ \ "individual" \ "lastName").read[String]
    )(IndividualDetails.apply _)

  implicit val writes: OWrites[IndividualDetails] =
    ((__ \ "individual" \ "firstName").write[String] and
      (__ \ "individual" \ "middleName").writeNullable[String] and
      (__ \ "individual" \ "lastName").write[String])(unlift(IndividualDetails.unapply))
}

case class ContactInformation(contactType: ContactType, email: String, phone: Option[String], mobile: Option[String])

object ContactInformation {

  implicit lazy val reads: Reads[ContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.read[ContactType] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
    )(ContactInformation.apply _)
  }

  implicit lazy val writes: OWrites[ContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.write[ContactType] and
        (__ \ "email").write[String] and
        (__ \ "phone").writeNullable[String] and
        (__ \ "mobile").writeNullable[String]
    )(unlift(ContactInformation.unapply))
  }
}

//sealed trait ContactInformation
//
//case class OrganisationDetails(organisationName: String)
//
//object OrganisationDetails {
//  implicit val format: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]
//}
//
//case class IndividualDetails(firstName: String, lastName: String, middleName: Option[String])
//
//object IndividualDetails {
//  implicit val format: OFormat[IndividualDetails] = Json.format[IndividualDetails]
//}
//
//case class ContactInformationForIndividual(individual: IndividualDetails, email: String, phone: Option[String], mobile: Option[String])
//    extends ContactInformation
//
//object ContactInformationForIndividual {
//  implicit val format: OFormat[ContactInformationForIndividual] = Json.format[ContactInformationForIndividual]
//}
//
//case class ContactInformationForOrganisation(organisation: OrganisationDetails, email: String, phone: Option[String], mobile: Option[String])
//    extends ContactInformation
//
//object ContactInformationForOrganisation {
//  implicit val format: OFormat[ContactInformationForOrganisation] = Json.format[ContactInformationForOrganisation]
//}
//
//case class PrimaryContact(contactInformation: Seq[ContactInformation])
//
//object PrimaryContact {
//
//  implicit lazy val reads: Reads[PrimaryContact] = {
//    import play.api.libs.functional.syntax._
//    (
//      (__ \\ "organisation").readNullable[OrganisationDetails] and
//        (__ \\ "individual").readNullable[IndividualDetails] and
//        (__ \\ "email").read[String] and
//        (__ \\ "phone").readNullable[String] and
//        (__ \\ "mobile").readNullable[String]
//    )((organisation, individual, email, phone, mobile) =>
//      (organisation.isDefined, individual.isDefined) match {
//        case (true, false) => PrimaryContact(Seq(ContactInformationForOrganisation(organisation.get, email, phone, mobile)))
//        case (false, true) => PrimaryContact(Seq(ContactInformationForIndividual(individual.get, email, phone, mobile)))
//        case _             => throw new Exception("Primary Contact must have either an organisation or individual element")
//      }
//    )
//  }
//
//  //API accepts one item for contact information
//  implicit lazy val writes: OWrites[PrimaryContact] = OWrites[PrimaryContact] {
//    case PrimaryContact(Seq(contactInformationForInd @ ContactInformationForIndividual(_, _, _, _))) =>
//      Json.toJsObject(contactInformationForInd)
//    case PrimaryContact(Seq(contactInformationForOrg @ ContactInformationForOrganisation(_, _, _, _))) =>
//      Json.toJsObject(contactInformationForOrg)
//  }
//}
//
//case class SecondaryContact(contactInformation: Seq[ContactInformation])
//
//object SecondaryContact {
//
//  implicit lazy val reads: Reads[SecondaryContact] = {
//    import play.api.libs.functional.syntax._
//    (
//      (__ \\ "organisation").readNullable[OrganisationDetails] and
//        (__ \\ "individual").readNullable[IndividualDetails] and
//        (__ \\ "email").read[String] and
//        (__ \\ "phone").readNullable[String] and
//        (__ \\ "mobile").readNullable[String]
//    )((organisation, individual, email, phone, mobile) =>
//      (organisation.isDefined, individual.isDefined) match {
//        case (true, false) => SecondaryContact(Seq(ContactInformationForOrganisation(organisation.get, email, phone, mobile)))
//        case (false, true) => SecondaryContact(Seq(ContactInformationForIndividual(individual.get, email, phone, mobile)))
//        case _             => throw new Exception("Secondary Contact must have either an organisation or individual element")
//      }
//    )
//  }
//
//  //API accepts one item for contact information
//  implicit lazy val writes: OWrites[SecondaryContact] = {
//    case SecondaryContact(Seq(contactInformationForInd @ ContactInformationForIndividual(_, _, _, _))) =>
//      Json.toJsObject(contactInformationForInd)
//    case SecondaryContact(Seq(contactInformationForOrg @ ContactInformationForOrganisation(_, _, _, _))) =>
//      Json.toJsObject(contactInformationForOrg)
//  }
//}
