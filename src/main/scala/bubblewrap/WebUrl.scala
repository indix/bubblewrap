package bubblewrap


import java.net.{URI, URL}

import scala.annotation.tailrec
import scala.util.parsing.combinator.RegexParsers


case class WebUrl(url:String){
  override def toString = url
  def normalized = WebUrl.from(url)
  def resolve(relative:String) = WebUrl(new URL(new URL(url),relative).toString)
}

object WebUrl {
  val sessionQuery = "(jsessionid|phpsessid|aspsessionid)=.*"
  val hexCodesToReplace = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Set('-', '.', '_', '~')
  val HEX = "[0-9abcdefABCDEF]+".r

  def from(urlString: String) = {
    val url = new URL(urlString)
    val port = removePortIfDefault(url.getProtocol)(url.getPort)
    val scheme = toLowercase(url.getProtocol)
    val host = toLowercase(url.getHost)
    val path = (removeDuplicateSlashes andThen removeDotSegments andThen replaceOctets)(url.getPath)
    val query = Option(url.getQuery).map(removeSessionIdQueries)

    WebUrl(new URL(scheme, host, port, query.map(path + "?" + _).getOrElse(path)).toString)
  }

  def raw(urlString: String) = WebUrl(urlString)

  def removeDuplicateSlashes = (path: String) => path.replaceAll("//+", "/")

  def removeDotSegments = (path: String) => "/" + path.split("/", -1).drop(1)
      .filterNot(_.trim.equals("."))
      .foldLeft(List[String]())((sofar, segment) => if (segment.trim == "..") sofar.drop(1) else segment :: sofar)
      .reverse
      .mkString("/")

  def removePortIfDefault(scheme: String) = (port: Int) => if (scheme == "http" && port == 80) -1 else port

  def toLowercase = (value: String) => value.toLowerCase

  def removeSessionIdQueries = (value: String) => value.split("&").filterNot(_.matches(sessionQuery)).mkString("&")

  def replaceOctets = {
    @tailrec
    def parseInternal(value: String, sofar: StringBuilder): StringBuilder =
      if (value.length < 3) sofar.append(value)
      else if (value.charAt(0) == '%' && isHex(value.slice(1, 3))) {
        val char = Integer.parseInt(value.slice(1, 3), 16).toChar
        if (hexCodesToReplace contains char) parseInternal(value.substring(3), sofar.append(char)) else parseInternal(value.substring(3), sofar.append(value.take(3).toUpperCase))
      } else {
        parseInternal(value.substring(1), sofar.append(value.charAt(0)))
      }

    def parse(value: String): String = parseInternal(value, new StringBuilder).toString()

    parse _
  }


  def isHex(value:String) =value match {
    case HEX() => true
    case _ => false
  }
}
