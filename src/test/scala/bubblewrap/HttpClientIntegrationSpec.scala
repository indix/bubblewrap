package bubblewrap

import org.scalatest.FlatSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import scala.concurrent.ExecutionContext.Implicits.global

class HttpClientIntegrationSpec extends FlatSpec {
  ignore should "fetch some page" in {
    val client: HttpClient = new HttpClient()
    val body = client.get(WebUrl.from("www.google.com"), CrawlConfig(None, "bubblewrap", 100000000, Cookies.None)).map { response =>
      response.pageResponse match {
        case SuccessResponse(page) => PageParser().parse(page).outgoingLinks
        case FailureResponse(error) => List.empty[WebUrl]
      }
    }

    Await.result(body, 10000 millis).foreach(println)
  }
}