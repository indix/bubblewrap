package bubblewrap

import scala.concurrent.Future

case class CrawlConfig(proxy:Option[Proxy], userAgent:String, maxSize: Long, cookies:Cookies = Cookies None, customHeaders: Headers = Headers None)
case class Headers(headers: Map[String,String]) {
  def +(header:(String,String)) = this.copy(headers + header)
}
object Headers {
  def None = Headers(Map.empty)
}
case class ClientSettings(socketTimeout:Int = 20000, connectionTimeout:Int = 30000, maxConnectionPerHost:Int = 16, maxTotalConnections:Int = 128)
case class Cookies(cookies:Map[String,String])
object Cookies {
  def None = Cookies(Map.empty)
}

case class HttpResponse(status:Int, body:Future[String], location:Option[String])

sealed trait Proxy
case class PlainProxy(host:String, port:Int) extends Proxy
case class ProxyWithAuth(host:String, port:Int, user:String, pass:String) extends Proxy

object Proxy {
  def apply(host:String, port:Int) = PlainProxy(host, port)
  def apply(host:String, port:Int, user:String, pass:String) = ProxyWithAuth(host, port, user, pass)
}




