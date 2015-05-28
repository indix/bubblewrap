package bubblewrap

import java.net.URL

import com.ning.http.client._
import com.ning.http.client.cookie.Cookie
import org.jboss.netty.handler.codec.http.HttpHeaders
import scala.collection.JavaConverters._
import HttpHeaders.Names._
import HttpHeaders.Values._


class HttpClient(config:CrawlConfig) {
  val client = new AsyncHttpClient()

  def get(url: String) = {
    val handler = new HttpHandler(config)
    val request = client.prepareGet(url)
    config.proxy.foreach(proxy => request.setProxyServer(new ProxyServer(proxy.host,proxy.port)))
    request
      .addHeader(ACCEPT, "*/*")
      .addHeader(ACCEPT_ENCODING, GZIP)
      .addHeader(USER_AGENT, config.userAgent)
      .setCookies(HttpClient.cookies(config,url).asJava)
      .execute(handler)
    println(request.build().toString)
    handler.httpResponse.future
  }
}

object HttpClient {
  val oneYear = 360l * 24 * 60 * 60 * 1000
  def cookies(config:CrawlConfig, url:String) = {
    config.cookies.cookies
      .map(cookie => Cookie.newValidCookie(cookie._1,cookie._2,true, host(url),"/", oneYear, 360 , url.startsWith("https"),true))
      .toList
  }
  def host(url:String) = new URL(url).getHost
}
