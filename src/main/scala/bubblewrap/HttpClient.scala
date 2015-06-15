package bubblewrap

import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.{SSLContext, X509TrustManager}

import com.ning.http.client._
import com.ning.http.client.cookie.Cookie
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._

import scala.collection.JavaConverters._

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

  def get(url: WebUrl, config:CrawlConfig) = {
    val handler = new HttpHandler(config, url)
    val request = client.prepareGet(url.toString)
    config.proxy.foreach{
      case PlainProxy(host, port) => request.setProxyServer(new ProxyServer(host,port))
      case ProxyWithAuth(host, port, user, pass) => request.setProxyServer(new ProxyServer(host, port, user, pass))
    }
    request
      .addHeader(ACCEPT, "*/*")
      .addHeader(ACCEPT_ENCODING, GZIP)
      .addHeader(USER_AGENT, config.userAgent)
      .setCookies(HttpClient.cookies(config,url.toString).asJava)

    config.customHeaders.headers.foreach(header => request.addHeader(header._1, header._2))
    request.execute(handler)
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
