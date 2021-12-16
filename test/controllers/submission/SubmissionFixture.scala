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

package controllers.submission

import models.subscription.{ContactInformationForOrganisation, OrganisationDetails, PrimaryContact, ResponseDetail, SecondaryContact}

object SubmissionFixture {
  val basicXml =
    <submission>
      <fileName>my-file.xml</fileName>
      <enrolmentID>enrolmentID</enrolmentID>
      <file>
        <DAC6_Arrangement version="First">
          <Header>
            <MessageRefId>GB0000000XXX</MessageRefId>
            <Timestamp>2020-05-14T17:10:00</Timestamp>
          </Header>
          <ArrangementID>GBA20200601AAA000</ArrangementID>
          <DAC6Disclosures>
            <DisclosureImportInstruction>DAC6ADD</DisclosureImportInstruction>
            <Disclosing>
              <ID>
                <Individual>
                  <IndividualName><FirstName>a</FirstName><LastName>b</LastName></IndividualName>
                  <BirthDate>2020-05-14</BirthDate>
                  <BirthPlace>a</BirthPlace>
                  <ResCountryCode>VU</ResCountryCode>
                </Individual>
              </ID>
            </Disclosing>
            <InitialDisclosureMA>false</InitialDisclosureMA>
          </DAC6Disclosures>
        </DAC6_Arrangement>
      </file>
    </submission>

  val responseDetail =
    ResponseDetail(
      subscriptionID = "subscriptionID",
      tradingName = Some("tradingName"),
      isGBUser = true,
      primaryContact = PrimaryContact(
        Seq(
          ContactInformationForOrganisation(
            email = "aaa",
            phone = Some("bbb"),
            mobile = None,
            organisation = OrganisationDetails(
              organisationName = "Example"
            )
          )
        )
      ),
      secondaryContact = Some(
        SecondaryContact(
          Seq(
            ContactInformationForOrganisation(
              email = "ddd",
              phone = Some("eee"),
              mobile = Some("fff"),
              organisation = OrganisationDetails(
                organisationName = "AnotherExample"
              )
            )
          )
        )
      )
    )
}
