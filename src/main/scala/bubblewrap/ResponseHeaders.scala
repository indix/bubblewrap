package bubblewrap

import com.ning.http.client.HttpResponseHeaders
import scala.collection.JavaConversions._


class ResponseHeaders(httpResponseHeaders: HttpResponseHeaders) {
  def exceedsSize(size:Long) = httpResponseHeaders.getHeaders.get("Content-Length").exists(_.toLong > size)
  def redirectLocation = httpResponseHeaders.getHeaders.get("Location").headOption
}