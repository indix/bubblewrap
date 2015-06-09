package bubblewrap

import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client.{HttpResponseBodyPart, FluentCaseInsensitiveStringsMap, HttpResponseHeaders, HttpResponseStatus}
import org.mockito.Mockito
import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}
import Mockito._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.{FiniteDuration, DurationInt}
import TestUtils._

class HttpHandlerSpec extends FlatSpec {
  val config: CrawlConfig = CrawlConfig(None, "bubblewrap", 1000, Cookies.None)

  def httpStatus(code:Int) = {
    val status = mock(classOf[HttpResponseStatus])
    when(status.getStatusCode).thenReturn(code)
    status
  }

  def httpHeaders(headers:(String, String)*) = {
    new HttpResponseHeaders {
      override def getHeaders: FluentCaseInsensitiveStringsMap = headers.foldLeft(new FluentCaseInsensitiveStringsMap())((m,t) =>m.add(t._1,t._2))
    }
  }

  def bodyPart(value:Array[Byte]) = {
    val part = mock(classOf[HttpResponseBodyPart])
    when(part.getBodyPartBytes).thenReturn(value)
    part
  }


  "HttpHandler" should "fulfil response once headers received" in {
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future

    handler.onStatusReceived(httpStatus(200))

    response.isCompleted should be(false)
    handler.onHeadersReceived(httpHeaders())

    response.isCompleted should be(true)
    get(response).status should be(200)
    get(response).body.isCompleted should be(false)
  }

  it should "add parts to the body and fulfil body once complete" in {
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future
    handler.onStatusReceived(httpStatus(200))

    handler.onHeadersReceived(httpHeaders()) should be(STATE.CONTINUE)
    handler.onBodyPartReceived(bodyPart("Hello ".getBytes)) should be(STATE.CONTINUE)
    handler.onBodyPartReceived(bodyPart("World".getBytes())) should be(STATE.CONTINUE)

    handler.onCompleted()

    response.isCompleted should be(true)
    get(get(response).body) should be ("Hello World")
  }

  it should "abort in case the content-length header has larger size than in crawl config" in {
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future
    handler.onStatusReceived(httpStatus(200))

    handler.onHeadersReceived(httpHeaders("Content-Length" -> "1001")) should be(STATE.ABORT)
    intercept[RuntimeException](get(get(response).body))
    get(response).body.isCompleted should be(true)
  }

  it should "continue if the content-length is well within max size" in {
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future
    handler.onStatusReceived(httpStatus(200))

    handler.onHeadersReceived(httpHeaders("Content-Length" -> "1000")) should be(STATE.CONTINUE)
    get(response).body.isCompleted should be(false)
  }

  it should "abort if the body content exceeds the max size" in {
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future
    handler.onStatusReceived(httpStatus(200))

    handler.onHeadersReceived(httpHeaders()) should be(STATE.CONTINUE)
    handler.onBodyPartReceived(bodyPart(("h"*500).getBytes)) should be(STATE.CONTINUE)
    handler.onBodyPartReceived(bodyPart(("h"*501).getBytes)) should be(STATE.ABORT)
    intercept[RuntimeException](get(get(response).body))
  }

  it should "have location in the response in case of redirects" in {
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future
    handler.onStatusReceived(httpStatus(302))

    handler.onHeadersReceived(httpHeaders("Location" -> "/home")) should be(STATE.CONTINUE)

    get(response).location should be(Some("/home"))
  }

  it should "use the content-type header to parse the body part" in {
    val koreanString = "동서게임 MS정품유선컨트롤러/PCGTA5호환 - 11번가"
    val handler = new HttpHandler(config)
    val response = handler.httpResponse.future
    handler.onStatusReceived(httpStatus(200))

    handler.onHeadersReceived(httpHeaders("Content-Type" -> "text/html; charset=euc-kr"))
    handler.onBodyPartReceived(bodyPart(koreanString.getBytes("euc-kr")))
    handler.onCompleted()

    get(get(response).body) should be(koreanString)
  }
}
