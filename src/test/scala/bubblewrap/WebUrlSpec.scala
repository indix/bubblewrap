package bubblewrap

import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

class WebUrlSpec extends FlatSpec {
  "WebUrl" should "convert upper case host name to lower case" in {
    WebUrl.from("http://www.Example.com/").toString should be("http://www.example.com/")
  }

  it should "convert upper case scheme to lower case" in {
    WebUrl.from("HTTp://www.example.com/").toString should be("http://www.example.com/")
  }

  it should "replace percent encoded octets in the ranges ALPHA(%41-%5A and %61 - %7A) DIGITS(%30 - %39) HYPHEN (%2D), PERIOD(%2E) UNDERSCORE(%5F) or TILDE(%7E)" in {
    WebUrl.from("http://www.example.com/%7E%41%62%30%31%32%5F%2D%2E").toString should be("http://www.example.com/~Ab012_-.")
  }

  it should "remove default port 80 from the url" in {
    WebUrl.from("http://www.example.com:80/").toString should be("http://www.example.com/")
  }

  it should "replace capitals in hex codes in path" in {
    WebUrl.from("http://www.example.com/%3c%3F").toString should be("http://www.example.com/%3C%3F")
  }

  it should "remove /../ and /./" in {
    WebUrl.from("http://www.example.com/../a/b/../c/./d.html").toString() should be("http://www.example.com/a/b/c/d.html")
  }

  it should "replace duplicate / with a single /" in {
    WebUrl.from("http://www.example.com//a/b/////c//d.html").toString should be("http://www.example.com/a/b/c/d.html")
  }

  it should "remove sessionid query params" in {
    WebUrl.from("http://www.example.com/a/?jsessionid=123&a=2&b=3").toString should be("http://www.example.com/a/?a=2&b=3")
    WebUrl.from("http://www.example.com/a/?phpsessid=123&a=2&b=3").toString should be("http://www.example.com/a/?a=2&b=3")
    WebUrl.from("http://www.example.com/a/?aspsessionid=123&a=2&b=3").toString should be("http://www.example.com/a/?a=2&b=3")
  }
}
