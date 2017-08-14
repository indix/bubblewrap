package bubblewrap


import java.net.{URI, URL}

import scala.annotation.tailrec
import scala.util.parsing.combinator.RegexParsers


case class WebUrl(url: String) {
  override def toString = url

  def normalized(keepFragments: Boolean = false) = WebUrl.from(url, keepFragments = keepFragments)

  def resolve(relative: String) = WebUrl(new URL(new URL(url), relative).toString)
}

object WebUrl {
  val sessionQuery = "(jsessionid|phpsessid|aspsessionid)=.*"
  val hexCodesToReplace = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Set('-', '.', '_', '~')
  val HEX = "[0-9abcdefABCDEF]+".r

  def from(urlString: String, keepFragments: Boolean = false) = {
    val url = new URL(urlString)
    val port = removePortIfDefault(url.getProtocol)(url.getPort)
    val scheme = toLowercase(url.getProtocol)
    val host = toLowercase(url.getHost)
    val path = (removeDuplicateSlashes andThen removeDotSegments andThen replaceOctets) (url.getPath)
    val query = Option(url.getQuery).map(removeSessionIdQueries)
    val fragment = if (keepFragments) extractFragments(urlString) else ""

    WebUrl(new URL(scheme, host, port, query.map(path + "?" + _).getOrElse(path) + fragment).toString)
  }

  def extractFragments(urlString: String): String = {
    // Had to resort to this since URL doesn't return Fragments, and URI which supports Fragments,
    // doesn't like octets or invalid characters in URL.
    val fragmentParts = urlString.split("#")
    if (fragmentParts.length > 1) "#" + fragmentParts.drop(1).mkString else ""
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


  def isHex(value: String) = value match {
    case HEX() => true
    case _ => false
  }
}
