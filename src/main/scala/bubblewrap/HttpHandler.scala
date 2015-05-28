package bubblewrap


import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client.{AsyncHandler, HttpResponseBodyPart, HttpResponseHeaders, HttpResponseStatus}

import scala.concurrent.Promise

class HttpHandler(config:CrawlConfig) extends AsyncHandler[Unit]{
  val MAX_SIZE_EXCEEDED: String = "Exceeds allowed max size"
  var body = new ResponseBody
  val httpBody = Promise[String]()
  var statusCode:Int = 200
  val httpResponse = Promise[HttpResponse]()

  override def onThrowable(error: Throwable) = httpResponse.failure(error)

  override def onCompleted(): Unit = httpBody.success(body.content)

  override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): STATE = {
    val part = bodyPart.getBodyPartBytes
    body.write(part)
    if (body.exceedsSize(config.maxSize)){
      httpBody.failure(new RuntimeException(MAX_SIZE_EXCEEDED))
      return STATE.ABORT
    }
    STATE.CONTINUE
  }

  override def onStatusReceived(status: HttpResponseStatus): STATE = {
    statusCode = status.getStatusCode
    STATE.CONTINUE
  }

  override def onHeadersReceived(httpResponseHeaders: HttpResponseHeaders): STATE = {
    val headers = new ResponseHeaders(httpResponseHeaders)
    httpResponse.success(HttpResponse(statusCode, httpBody.future, headers.redirectLocation))
    if(headers.exceedsSize(config.maxSize)) {
      httpBody.failure(new RuntimeException(MAX_SIZE_EXCEEDED))
      return STATE.ABORT
    }
    STATE.CONTINUE
  }
}
