package bubblewrap

import java.net.{URI, URL}


case class WebUrl(url:String){
  override def toString = url
  def normalized = WebUrl.from(url)
  def resolve(relative:String) = WebUrl(new URI(url).resolve(relative).toString)
}

object WebUrl {
  val sessionQuery = "(jsessionid|phpsessid|aspsessionid)=.*"
  def from(urlString: String) = {
    val url = new URI(urlString)
    val port = removePortIfDefault(url.getScheme)(url.getPort)
    val scheme = toLowercase(url.getScheme)
    val host = toLowercase(url.getHost)
    val path = (removeDotSegments andThen removeDuplicateSlashes)(url.getPath)
    val query = Option(url.getQuery).map(removeSessionIdQueries).orNull
    
    WebUrl(new URI(scheme, url.getUserInfo, host, port, path, query, url.getFragment).toString)
  }

  def raw(urlString: String) = WebUrl(urlString)

  def removeDuplicateSlashes = (path:String) => path.replaceAll("//+","/")
  def removeDotSegments = (path:String) => path.replaceAll("/\\.\\./", "/").replaceAll("/\\./","/")
  def removePortIfDefault(scheme:String)= (port:Int) => if(scheme == "http" && port == 80) -1 else port
  def toLowercase = (value: String) => value.toLowerCase
  def removeSessionIdQueries = (value:String) => value.split("&").filterNot(_.matches(sessionQuery)).mkString("&")
  
}
