package bubblewrap

import java.net.URL
import java.security.cert.X509Certificate
import java.util.concurrent.Executor
import javax.net.ssl.{TrustManager, X509TrustManager, SSLContext}

import com.ning.http.client._
import com.ning.http.client.cookie.Cookie
import org.jboss.netty.handler.codec.http.HttpHeaders
import scala.collection.JavaConverters._
import HttpHeaders.Names._
import HttpHeaders.Values._

class HttpClient(clientSettings:ClientSettings = new ClientSettings()) {
  val lenientSSLContext: SSLContext = {
    val context = SSLContext.getInstance("SSL")
    context.init(null, Array(new X509TrustManager {
      override def checkClientTrusted(chain:Array[X509Certificate], authType:String) {}
      override def checkServerTrusted(chain:Array[X509Certificate], authType:String) {}
      override def getAcceptedIssuers = null
    }
    ), null)
    context
  }
  
  val client = new AsyncHttpClient(new AsyncHttpClientConfigBean()
                                      .setMaxConnectionPerHost(clientSettings.maxConnectionPerHost)
                                      .setConnectionTimeOut(clientSettings.socketTimeout)
                                      .setMaxTotalConnections(clientSettings.maxTotalConnections)
                                      .setSslContext(lenientSSLContext)
  )

  def get(url: String, config:CrawlConfig) = {
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
