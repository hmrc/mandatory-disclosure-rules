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

package controllers.submission

import models.subscription.{ContactInformation, IndividualDetails, OrganisationDetails, ResponseDetail}

object SubmissionFixture {
  val basicXml =
    <MDR_OECD xmlns="urn:oecd:ties:mdr:v1" >
          <MessageSpec>
            <TransmittingCountry>AF</TransmittingCountry>
            <ReceivingCountry>AF</ReceivingCountry>
            <MessageType>MDR</MessageType>
            <Language>EN</Language>
            <Warning>This is a warning</Warning>
            <Contact>This is a Contact</Contact>
            <MessageRefId>GBXAMDR1234567</MessageRefId>
            <MessageTypeIndic>MDR401</MessageTypeIndic>
            <Timestamp>2020-05-14T17:10:00</Timestamp>
          </MessageSpec>
          <MdrBody>
            <Disclosing>
              <ID>
                <Organisation>
                  <ResCountryCode>VU</ResCountryCode>
                  <TIN>AA000000D</TIN>
                  <IN>AA000000D</IN>
                  <Name>organisationName</Name>
                  <Address>
                    <CountryCode>GB</CountryCode>
                    <AddressFix>
                      <Street>Downing Street</Street>
                      <BuildingIdentifier>No 10</BuildingIdentifier>
                      <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                      <FloorIdentifier>Second</FloorIdentifier>
                      <DistrictName>Westminster</DistrictName>
                      <POB>48</POB>
                      <PostCode>SW1A 4GG</PostCode>
                      <City>London</City>
                      <CountrySubentity>GB</CountrySubentity>
                    </AddressFix>
                    <AddressFree>Address is Free</AddressFree>
                  </Address>
                </Organisation>
              </ID>
              <Capacity>MDR501</Capacity>
              <Nexus>MDR601</Nexus>
              <Nexus>MDR602</Nexus>
              <DocSpec>
                <DocTypeIndic>OECD0</DocTypeIndic>
                <DocRefId>GB123456</DocRefId>
                <CorrDocRefId>This is corr ref ID</CorrDocRefId>
              </DocSpec>
            </Disclosing>
            <MdrReport>
              <ReportableTaxPayer>
                <ID>
                  <Individual>
                    <ResCountryCode>VU</ResCountryCode>
                    <TIN>AA000000D</TIN>
                    <Name>
                      <PrecedingTitle>His Excellency</PrecedingTitle>
                      <Title>MR</Title>
                      <FirstName>Larry</FirstName>
                      <MiddleName>David</MiddleName>
                      <NamePrefix>van</NamePrefix>
                      <LastName>David</LastName>
                      <GenerationIdentifier>Jnr</GenerationIdentifier>
                      <Suffix>(Cat)</Suffix>
                      <GeneralSuffix>Deceased</GeneralSuffix>
                    </Name>
                    <Address>
                      <CountryCode>GB</CountryCode>
                      <AddressFix>
                        <Street>Downing Street</Street>
                        <BuildingIdentifier>No 10</BuildingIdentifier>
                        <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                        <FloorIdentifier>Second</FloorIdentifier>
                        <DistrictName>Westminster</DistrictName>
                        <POB>48</POB>
                        <PostCode>SW1A 4GG</PostCode>
                        <City>London</City>
                        <CountrySubentity>GB</CountrySubentity>
                      </AddressFix>
                      <AddressFree>Address is Free</AddressFree>
                    </Address>
                    <BirthDate>2007-01-14</BirthDate>
                  </Individual>
                </ID>
              </ReportableTaxPayer>
              <ReportableTaxPayer>
                <ID>
                  <Organisation>
                    <ResCountryCode>VU</ResCountryCode>
                    <TIN>AA000000D</TIN>
                    <IN>AA000000D</IN>
                    <Name>organisationName</Name>
                    <Address>
                      <CountryCode>GB</CountryCode>
                      <AddressFix>
                        <Street>Downing Street</Street>
                        <BuildingIdentifier>No 10</BuildingIdentifier>
                        <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                        <FloorIdentifier>Second</FloorIdentifier>
                        <DistrictName>Westminster</DistrictName>
                        <POB>48</POB>
                        <PostCode>SW1A 4GG</PostCode>
                        <City>London</City>
                        <CountrySubentity>GB</CountrySubentity>
                      </AddressFix>
                      <AddressFree>Address is Free</AddressFree>
                    </Address>
                  </Organisation>
                </ID>
              </ReportableTaxPayer>
              <Intermediaries>
                <ID>
                  <Individual>
                    <ResCountryCode>VU</ResCountryCode>
                    <TIN>AA000000D</TIN>
                    <Name>
                      <PrecedingTitle>His Excellency</PrecedingTitle>
                      <Title>MR</Title>
                      <FirstName>Larry</FirstName>
                      <MiddleName>David</MiddleName>
                      <NamePrefix>van</NamePrefix>
                      <LastName>David</LastName>
                      <GenerationIdentifier>Jnr</GenerationIdentifier>
                      <Suffix>(Cat)</Suffix>
                      <GeneralSuffix>Deceased</GeneralSuffix>
                    </Name>
                    <Address>
                      <CountryCode>GB</CountryCode>
                      <AddressFix>
                        <Street>Downing Street</Street>
                        <BuildingIdentifier>No 10</BuildingIdentifier>
                        <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                        <FloorIdentifier>Second</FloorIdentifier>
                        <DistrictName>Westminster</DistrictName>
                        <POB>48</POB>
                        <PostCode>SW1A 4GG</PostCode>
                        <City>London</City>
                        <CountrySubentity>GB</CountrySubentity>
                      </AddressFix>
                      <AddressFree>Address is Free</AddressFree>
                    </Address>
                    <BirthDate>2007-01-14</BirthDate>
                  </Individual>
                </ID>
                <Role>MDR1101</Role>
              </Intermediaries>
              <Intermediaries>
                <ID>
                  <Organisation>
                    <ResCountryCode>VU</ResCountryCode>
                    <TIN>AA000000D</TIN>
                    <IN>AA000000D</IN>
                    <Name>organisationName</Name>
                    <Address>
                      <CountryCode>GB</CountryCode>
                      <AddressFix>
                        <Street>Downing Street</Street>
                        <BuildingIdentifier>No 10</BuildingIdentifier>
                        <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                        <FloorIdentifier>Second</FloorIdentifier>
                        <DistrictName>Westminster</DistrictName>
                        <POB>48</POB>
                        <PostCode>SW1A 4GG</PostCode>
                        <City>London</City>
                        <CountrySubentity>GB</CountrySubentity>
                      </AddressFix>
                      <AddressFree>Address is Free</AddressFree>
                    </Address>
                  </Organisation>
                </ID>
                <Role>MDR1102</Role>
              </Intermediaries>
              <Structure>
                <Arrangement>
                  <CrsAvoidance>
                    <DisclosureDate>2021-12-04</DisclosureDate>
                    <Reason>MDR701</Reason>
                    <Type>MDR801</Type>
                    <OtherInfo>Other inforamtion</OtherInfo>
                    <StructureChart>
                      <ID>
                        <Individual>
                          <ResCountryCode>VU</ResCountryCode>
                          <TIN>AA000000D</TIN>
                          <Name>
                            <PrecedingTitle>His Excellency</PrecedingTitle>
                            <Title>MR</Title>
                            <FirstName>Larry</FirstName>
                            <MiddleName>David</MiddleName>
                            <NamePrefix>van</NamePrefix>
                            <LastName>David</LastName>
                            <GenerationIdentifier>Jnr</GenerationIdentifier>
                            <Suffix>(Cat)</Suffix>
                            <GeneralSuffix>Deceased</GeneralSuffix>
                          </Name>
                          <Address>
                            <CountryCode>GB</CountryCode>
                            <AddressFix>
                              <Street>Downing Street</Street>
                              <BuildingIdentifier>No 10</BuildingIdentifier>
                              <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                              <FloorIdentifier>Second</FloorIdentifier>
                              <DistrictName>Westminster</DistrictName>
                              <POB>48</POB>
                              <PostCode>SW1A 4GG</PostCode>
                              <City>London</City>
                              <CountrySubentity>GB</CountrySubentity>
                            </AddressFix>
                            <AddressFree>Address is Free</AddressFree>
                          </Address>
                          <BirthDate>2007-01-14</BirthDate>
                        </Individual>
                      </ID>
                      <Ownership>90</Ownership>
                      <InvestAmount currCode="VUV">2000000</InvestAmount>
                      <OtherInfo>Other inforamtion</OtherInfo>
                      <ListChilds>
                        <ChildRTP>
                          <ID>
                            <Individual>
                              <ResCountryCode>VU</ResCountryCode>
                              <TIN>AA000000D</TIN>
                              <Name>
                                <PrecedingTitle>His Excellency</PrecedingTitle>
                                <Title>MR</Title>
                                <FirstName>Larry</FirstName>
                                <MiddleName>David</MiddleName>
                                <NamePrefix>van</NamePrefix>
                                <LastName>David</LastName>
                                <GenerationIdentifier>Jnr</GenerationIdentifier>
                                <Suffix>(Cat)</Suffix>
                                <GeneralSuffix>Deceased</GeneralSuffix>
                              </Name>
                              <Address>
                                <CountryCode>GB</CountryCode>
                                <AddressFix>
                                  <Street>Downing Street</Street>
                                  <BuildingIdentifier>No 10</BuildingIdentifier>
                                  <SuiteIdentifier>Sir Humphrey Suite</SuiteIdentifier>
                                  <FloorIdentifier>Second</FloorIdentifier>
                                  <DistrictName>Westminster</DistrictName>
                                  <POB>48</POB>
                                  <PostCode>SW1A 4GG</PostCode>
                                  <City>London</City>
                                  <CountrySubentity>GB</CountrySubentity>
                                </AddressFix>
                                <AddressFree>Address is Free</AddressFree>
                              </Address>
                              <BirthDate>2007-01-14</BirthDate>
                            </Individual>
                          </ID>
                          <Ownership>90</Ownership>
                          <InvestAmount currCode="VUV">2000000</InvestAmount>
                          <OtherInfo>Other inforamtion</OtherInfo>
                        </ChildRTP>
                      </ListChilds>
                    </StructureChart>
                    <Narrative>this is narrative</Narrative>
                    <Jurisdictions>IN</Jurisdictions>
                    <Summary>this is a summary of narrative CSR AVOIDANCE</Summary>
                  </CrsAvoidance>
                </Arrangement>
              </Structure>
              <DocSpec>
                <DocTypeIndic>OECD0</DocTypeIndic>
                <DocRefId>GB123456</DocRefId>
                <CorrDocRefId>This is corr ref ID</CorrDocRefId>
              </DocSpec>
            </MdrReport>
          </MdrBody>
        </MDR_OECD>

  val responseDetail =
    ResponseDetail(
      subscriptionID = "subscriptionID",
      tradingName = Some("tradingName"),
      isGBUser = true,
      primaryContact = ContactInformation(
        email = "aaa@test.com",
        phone = Some("1234567"),
        mobile = None,
        contactType = OrganisationDetails(
          organisationName = "Example"
        )
      ),
      secondaryContact = Some(
        ContactInformation(
          email = "ddd",
          phone = Some("12345678"),
          mobile = Some("1222222"),
          contactType = OrganisationDetails(
            organisationName = "AnotherExample"
          )
        )
      )
    )

  val responseDetailForIndividual =
    ResponseDetail(
      subscriptionID = "subscriptionID",
      tradingName = Some("tradingName"),
      isGBUser = true,
      primaryContact = ContactInformation(
        email = "aaa@test.com",
        phone = Some("1234567"),
        mobile = None,
        contactType = IndividualDetails(
          firstName = "\n        Individual without id first name\n      ",
          middleName = None,
          lastName = "\n        Individual without id last name\n      "
        )
      ),
      secondaryContact = Some(
        ContactInformation(
          email = "ddd",
          phone = Some("12345678"),
          mobile = Some("1222222"),
          contactType = OrganisationDetails(
            organisationName = "AnotherExample"
          )
        )
      )
    )
}
