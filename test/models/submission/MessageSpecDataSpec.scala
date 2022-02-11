package models.submission

import base.SpecBase
import play.api.libs.json.Json

class MessageSpecDataSpec extends SpecBase {

  "MessageSpecDataSpec" - {
    "must serialize MessageSpec" in {
      val msd = MessageSpecData("XDSG111111", MDR401)
      val expectedJson = Json.parse("""{"messageRefId":"XDSG111111","messageTypeIndic":{"_type":"MDR401"}}""")
      Json.toJson(msd) mustBe expectedJson
    }
    "must deserialize MessageSpec" in {
      val json = Json.parse("""{"messageRefId":"XDSG333333","messageTypeIndic":{"_type":"MDR402"}}""")
      val expected = MessageSpecData("XDSG333333", MDR402)

      json.as[MessageSpecData] mustEqual expected
    }
  }
}
