package bubblewrap

import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

class ResponseBodySpec extends FlatSpec {
  "ResponseBody" should "write bytes and append them and give back a string" in {
    val body = new ResponseBody
    body.write("Hello".getBytes)
    body.write(" World".getBytes)

    body.content should be("Hello World")
  }

  it should "keep track of number of bytes written and say if exceeds size" in {
    val body = new ResponseBody
    body.write("Hello".getBytes)

    body.exceedsSize(6) should be(false)
    body.exceedsSize(4) should be(true)

    body.write(" World".getBytes())

    body.exceedsSize(11) should be(false)
    body.exceedsSize(10) should be(true)
  }

  it should "use the content encoding to encode the bytes back to string"  in {
    val koreanString = "동서게임 MS정품유선컨트롤러/PCGTA5호환 - 11번가"
    val body = new ResponseBody

    body.useCharset("EUC-KR")
    body.write(koreanString.getBytes("EUC-KR"))

    body.content should be(koreanString)
  }
}
