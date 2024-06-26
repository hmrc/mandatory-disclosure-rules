# Mandatory disclosure rules

This microservice is used to submit an XML file to EIS.

## Overview:

This microservice receives an upscan url from [mandatory-disclosure-rules-frontend](https://github.com/hmrc/mandatory-disclosure-rules-frontend). It loads the XML and then validates it against XML schema. If this fails validation we pass errors back to the frontend to be displayed to the user. If it passes schema validation the submission undergoes a second layer of validation against business rules handled by CADX. If the submission passes this validation and is submitted successfully an Accepted status is returned. If the submission fails business rule validation a Rejected status containing error codes is returned which is then passed back to the frontend.

This service notifies users if the submission is successfully submitted or has problems.

This service interacts with [mandatory disclosure rules frontend](https://github.com/hmrc/mandatory-disclosure-rules-frontend), [Upscan](https://github.com/hmrc/upscan-initiate),  [Email Service](https://github.com/hmrc/email) & EIS/CADX.

### API 
| PATH | Supported Methods | Description                                                                                                                   |
|------|-------------------|-------------------------------------------------------------------------------------------------------------------------------|
|```/callback ``` | POST | Upscan callback                                                                                                               |
|```/upscan/details/:uploadId``` | GET | Retrieves upscan session details containing UploadId, Reference & Status                                                      |
|```/upscan/status/:uploadId``` | GET | Retrieves only the upload Status                                                                                              |
|```/upscan/upload``` | POST | Request an upload                                                                                                             |
|```/validate-submission``` | POST | Performs XML schema validation & returns either messageSpecData containing messageRefID & messageTypeIndic or errors          |
|```/files/:conversationId/details``` | GET | Retrieves specific file details from Mongo store containing subscriptionID, messageRefID, file status, file name & timestamps |
|```/files/details``` | GET | Retrieves details of all submitted files from Mongo store                                                                     |
|```/files/:conversationId/status``` | GET | Retrieves file status for a specific file from Mongo store                                                                    |
|```/dac6/dct72a/v1``` | POST | Submits disclosure to EIS                                                                                                     |
|```/dac6/dct70d/v1``` | POST | Retrieves Subscription Details (Contact Details)                                                                              |
|```/dac6/dct70e/v1``` | POST | Updates Subscription Details (Contact Details)                                                                                |
|```/validation-result``` | POST | Retrieves & Processes result from CADX validation                                                                             |
|```/sdes/callback``` | POST | Retrieves the status of the file as it passes through SDES and updates Mongo                                                  |
|```/sdes-stub/notification/fileready``` | POST | Notifies SDES we have a file ready for transfer and provides metadata and a location for the file                             |

#### *API specs*: 

 - [MDR Read Subscription API](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/434373869/AEOI-DCT70d-1.2-EISAPISpecification-MDRSubscriptionDisplay.pdf)
 - [MDR Update Subscription API](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/434373871/AEOI-DCT70e-1.2-EISAPISpecification-MDRSubscriptionAmend.pdf)
 - [File Submission MDTP to CADX](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/434373874/AEOI-DCT72a-v0.3-EISAPISpecification-MDRCustomerFileSubmissionfromMDTPtoCADX.pdf)
  - [Result of MDR Business Rule Check](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/420709843/DCT72b.pdf)

## Run Locally
This service runs on port 10019 and is named MANDATORY_DISCLOSURE_RULES in service manager. 

Run the following command to start services locally:

 
```bash
sm2 --start MDR_ALL
```

#### *Acceptance test repo*:  
[mandatory-disclosure-rules-file-upload-ui-tests](https://github.com/hmrc/mandatory-disclosure-rules-file-upload-ui-tests)

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), and requires at least Java 11 [JRE] to run.

## SDES Info

For more information on how to set up SDES stubs for a specific callback visit the readme here: https://github.com/hmrc/sdes-stub

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
