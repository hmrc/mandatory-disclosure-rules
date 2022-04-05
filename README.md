# Mandatory Disclosure Rules

This service takes a users XML from [mandatory-disclosure-rules-frontend](https://github.com/hmrc/mandatory-disclosure-rules-frontend) that has been checked by upscan. It validates the submission against XML schema and submits it to EIS.

## Run Locally

Run the following command to start services locally:

    sm --start MDR_ALL -f
    
Mandatory Disclosure Rules runs on port 10018

### License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
