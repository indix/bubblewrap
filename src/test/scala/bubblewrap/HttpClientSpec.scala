package bubblewrap

import org.scalatest.AsyncFlatSpec

class HttpClientSpec extends AsyncFlatSpec {
  it should "download a page" in {
    val client = new HttpClient()
    val request = client.get(WebUrl("http://www.example.com"), CrawlConfig(None, "bubblewrap", 1000000, 5, Cookies.None))

    request map {
      case HttpResponse(_, SuccessResponse(content), _, _) => assert(content.length > 0)
      case _ => assert(false)
    }
  }
}
