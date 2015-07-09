package bubblewrap


import java.io.IOException
import java.util.concurrent.TimeoutException

import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client._

import scala.concurrent.Promise

class HttpHandler(config:CrawlConfig, url: WebUrl) extends AsyncHandler[Unit]{
  var body = new ResponseBody
  var statusCode:Int = 200
  val httpResponse = Promise[HttpResponse]()
  var headers: ResponseHeaders = new ResponseHeaders(new HttpResponseHeaders() {
    override def getHeaders: FluentCaseInsensitiveStringsMap = new FluentCaseInsensitiveStringsMap()
  })
  val startTime = System.currentTimeMillis()
  val UNKNOWN_ERROR = 1006
  val TIMEOUT_ERROR = 9999
  val REMOTE_CLOSE = 9998

  private def errorCode(error: Throwable) = error match {
    case timeout:TimeoutException => TIMEOUT_ERROR
    case other:IOException if other.getMessage.contains("Remotely closed") => REMOTE_CLOSE
    case other => UNKNOWN_ERROR
  }

  override def onThrowable(error: Throwable) = httpResponse.success(HttpResponse(errorCode(error), FailureResponse(error), headers, responseTime))


  override def onCompleted(): Unit = {
    httpResponse.success(HttpResponse(statusCode, SuccessResponse(Content(url, body.content, headers)), headers, responseTime))
  }

  override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): STATE = {
    val part = bodyPart.getBodyPartBytes
    body.write(part)
    if (body.exceedsSize(config.maxSize)){
      httpResponse.success(HttpResponse(statusCode, FailureResponse(ExceededSize(config.maxSize)), headers, responseTime))
      return STATE.ABORT
    }
    STATE.CONTINUE
  }

  override def onStatusReceived(status: HttpResponseStatus): STATE = {
    statusCode = status.getStatusCode
    STATE.CONTINUE
  }

  override def onHeadersReceived(httpResponseHeaders: HttpResponseHeaders): STATE = {
    headers = new ResponseHeaders(httpResponseHeaders)
    if(headers.exceedsSize(config.maxSize)) {
      httpResponse.success(HttpResponse(statusCode, FailureResponse(ExceededSize(config.maxSize)), headers, responseTime))
      return STATE.ABORT
    }
    STATE.CONTINUE
  }

  private def responseTime = System.currentTimeMillis() - startTime
}
