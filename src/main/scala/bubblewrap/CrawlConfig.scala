package bubblewrap

import scala.concurrent.Future

case class CrawlConfig(proxy:Option[Proxy], userAgent:String, maxSize: Long, cookies:Cookies)
case class Cookies(cookies:Map[String,String])
object Cookies {
  def None = Cookies(Map.empty)
}
case class HttpResponse(status:Int, body:Future[String], location:Option[String])
case class Proxy(host:String, port:Int)

