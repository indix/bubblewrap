package bubblewrap

import io.netty.handler.codec.http.{HttpHeaders => HttpResponseHeaders}

import scala.collection.JavaConversions._


class ResponseHeaders(httpResponseHeaders: HttpResponseHeaders) {
  val Charset = ".*charset\\s*=\\s*\\\"?([^;\\\"\\s]+)\\\"?\\s*".r

  def exceedsSize(size:Long) = httpResponseHeaders.getAll("Content-Length").exists(_.toLong > size)
  def redirectLocation = httpResponseHeaders.getAll("Location").headOption
  def contentCharset = contentType.collect {
    case Charset(enc) => enc
  }
  def contentType = httpResponseHeaders.getAll("Content-Type").headOption
  def contentEncoding = httpResponseHeaders.getAll("Content-Encoding").headOption
  def headers:Map[String,List[String]] = httpResponseHeaders.map(t => t.getKey -> httpResponseHeaders.getAll(t.getKey).toList).toMap
}