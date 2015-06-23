package bubblewrap

import org.scalatest.FlatSpec
import org.scalatest.Inside
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

class CrawlConfigSpec extends FlatSpec {
  "RequestHeaders" should "get appended on +" in {
    (RequestHeaders(Map("FOO" -> "1")) + ("BAR" -> "2")) should be (RequestHeaders(Map("FOO" -> "1", "BAR" -> "2")))
  }
}
