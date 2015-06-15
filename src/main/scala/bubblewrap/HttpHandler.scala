package bubblewrap


import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client.{AsyncHandler, HttpResponseBodyPart, HttpResponseHeaders, HttpResponseStatus}

import scala.concurrent.Promise

class HttpHandler(config:CrawlConfig, url: WebUrl) extends AsyncHandler[Unit]{
  var body = new ResponseBody
  var statusCode:Int = 200
  val httpResponse = Promise[HttpResponse]()
  var headers: ResponseHeaders = null
  val startTime = System.currentTimeMillis()

  override def onThrowable(error: Throwable) = httpResponse.failure(error)

  override def onCompleted(): Unit = {
    httpResponse.success(HttpResponse(statusCode, SuccessResponse(Page(url, body.content, headers)), headers, responseTime))
  }

  override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): STATE = {
    val part = bodyPart.getBodyPartBytes
    body.write(part)
    if (body.exceedsSize(config.maxSize)){
      httpResponse.success(HttpResponse(statusCode, FailureResponse(s"Exceeds allowed max size ${config.maxSize}"), headers, responseTime))
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
    headers.contentCharset.foreach(body.useCharset)
    if(headers.exceedsSize(config.maxSize)) {
      httpResponse.success(HttpResponse(statusCode, FailureResponse(s"Exceeds allowed max size ${config.maxSize}"), headers, responseTime))
      return STATE.ABORT
    }
    STATE.CONTINUE
  }

  private def responseTime = System.currentTimeMillis() - startTime
}
