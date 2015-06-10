package bubblewrap

import com.ning.http.client.HttpResponseHeaders
import scala.collection.JavaConversions._


class ResponseHeaders(httpResponseHeaders: HttpResponseHeaders) {
  val Charset = ".*charset\\s*=\\s*([^;\\s]+)\\s*".r

  def exceedsSize(size:Long) = httpResponseHeaders.getHeaders.get("Content-Length").exists(_.toLong > size)
  def redirectLocation = httpResponseHeaders.getHeaders.get("Location").headOption
  def encoding = contentType.collect {
    case Charset(enc) => enc
  }
  def contentType = httpResponseHeaders.getHeaders.get("Content-Type").headOption
}