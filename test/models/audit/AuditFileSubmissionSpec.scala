/*
 * Copyright 2023 HM Revenue & Customs
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

package models.audit

import base.SpecBase
import models.submission.{ConversationId, MDR401, MDR402}
import play.api.libs.json.Json

class AuditFileSubmissionSpec extends SpecBase {

  "AuditFileSubmission apply method must create an AuditFileSubmission" - {

    "when more than 1 MdrBody is present" - {

      "if MessageTypeIndic is `MDR401` must have a ReportType of 'MultipleReports' and a SubmissionType of 'NewInformation'" in {
        val mdrBodyCount = 2
        val docTypeIndic = Some("OECD1")
        val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR401, docTypeIndic)

        result mustBe AuditFileSubmission("MDR",
                                          "subscriptionId",
                                          ConversationId("id"),
                                          "Filename.xml",
                                          "1000",
                                          "application/xml",
                                          "MultipleReports",
                                          "NewInformation"
        )
      }

      "if MessageTypeIndic is not `MDR401` must have a ReportType of 'MultipleReports' and a SubmissionType of 'Corrections/Deletions'" in {
        val mdrBodyCount = 2
        val docTypeIndic = Some("OECD1")
        val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR402, docTypeIndic)

        result mustBe AuditFileSubmission("MDR",
                                          "subscriptionId",
                                          ConversationId("id"),
                                          "Filename.xml",
                                          "1000",
                                          "application/xml",
                                          "MultipleReports",
                                          "Corrections/Deletions"
        )
      }
    }

    "when only 1 MdrBody is present" - {

      "if DocTypeIndic is `OECD1` must have a ReportType of 'SingleReport' and a SubmissionType of 'NewInformation'" in {
        val mdrBodyCount = 1
        val docTypeIndic = Some("OECD1")
        val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR401, docTypeIndic)

        result mustBe AuditFileSubmission("MDR",
                                          "subscriptionId",
                                          ConversationId("id"),
                                          "Filename.xml",
                                          "1000",
                                          "application/xml",
                                          "SingleReport",
                                          "NewInformation"
        )
      }

      "if DocTypeIndic is `OECD2` must have a ReportType of 'SingleReport' and a SubmissionType of 'Correction'" in {
        val mdrBodyCount = 1
        val docTypeIndic = Some("OECD2")
        val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR401, docTypeIndic)

        result mustBe AuditFileSubmission("MDR",
                                          "subscriptionId",
                                          ConversationId("id"),
                                          "Filename.xml",
                                          "1000",
                                          "application/xml",
                                          "SingleReport",
                                          "Correction"
        )
      }

      "if DocTypeIndic is `OECD3` must have a ReportType of 'SingleReport' and a SubmissionType of 'Deletion'" in {
        val mdrBodyCount = 1
        val docTypeIndic = Some("OECD3")
        val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR401, docTypeIndic)

        result mustBe AuditFileSubmission("MDR", "subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", "SingleReport", "Deletion")
      }

      "if DocTypeIndic is Anything else must have a ReportType of 'N/A' and a SubmissionType of 'N/A'" in {
        val mdrBodyCount = 1
        val docTypeIndic = Some("OECD1OECD2OECD3")
        val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR401, docTypeIndic)

        result mustBe AuditFileSubmission("MDR", "subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", "N/A", "N/A")
      }
    }

    "and must serialize AuditFileSubmission" in {
      val mdrBodyCount = 1
      val docTypeIndic = Some("OECD1")
      val result = AuditFileSubmission("subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", mdrBodyCount, MDR401, docTypeIndic)
      val expectedJson = Json.parse(
        """{
          |"regime":"MDR",
          |"subscriptionId":"subscriptionId",
          |"conversationId":"id",
          |"filename":"Filename.xml",
          |"fileSize":"1000",
          |"mimeType":"application/xml",
          |"submissionType":"SingleReport",
          |"reportType":"NewInformation"
          |}""".stripMargin
      )
      Json.toJson(result) mustBe expectedJson
    }

    "and must deserialize AuditFileSubmission" in {
      val json = Json.parse(
        """{
          |"regime":"MDR",
          |"subscriptionId":"subscriptionId",
          |"conversationId":"id",
          |"filename":"Filename.xml",
          |"fileSize":"1000",
          |"mimeType":"application/xml",
          |"submissionType":"SingleReport",
          |"reportType":"NewInformation"
          |}""".stripMargin
      )
      val expected =
        AuditFileSubmission("MDR", "subscriptionId", ConversationId("id"), "Filename.xml", "1000", "application/xml", "SingleReport", "NewInformation")

      json.as[AuditFileSubmission] mustEqual expected
    }
  }
}
