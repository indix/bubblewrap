package bubblewrap

import java.util.regex.Pattern

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

case class Content(url: WebUrl, content: Array[Byte], contentType: Option[String] = None, contentCharset: Option[String] = None,  contentEncoding: Option[String] = None) {
  def asString = new String(content,contentCharset.getOrElse("UTF-8"))
  def asBytes = content
  def length = content.length
}
case class Page(content:Content,
                metaRefresh:Option[WebUrl] = None,
                canonicalUrl:Option[WebUrl] = None,
                outgoingLinks:List[WebUrl] = List.empty) {
  def url = content.url
  def contentType = content.contentType
  def contentCharset = content.contentCharset
  def contentEncoding = content.contentEncoding
}

object Content{
  def apply(url: WebUrl, body: Array[Byte], responseHeaders: ResponseHeaders): Content = {
    Content(url, body,responseHeaders.contentEncoding, responseHeaders.contentCharset, responseHeaders.contentType)
  }
}


class PageParser {
  val MetaUrl = "(?i)[^;]*;\\s*URL\\s*=(.*)".r
  val NOFOLLOW = "(?i).*nofollow.*"
  val SCHEME = "([^:]+):.*".r
  val schemes = Set("http","https")
  def parse(content: Content): Page = {
    val doc = Jsoup.parse(content.asString)
    Page(content, metaRefresh(content.url, doc), canonicalUrl(content.url, doc), outgoingLinks = outgoingLinks(content.url, doc))
  }

  private def metaRefresh(baseUrl:WebUrl,doc: Document) = {
    doc.getElementsByAttributeValueMatching("http-equiv", Pattern.compile("refresh", Pattern.CASE_INSENSITIVE))
      .headOption
      .map(element => element.attr("content"))
      .collect {
        case MetaUrl(location) => baseUrl.resolve(location)
      }
  }

  private def canonicalUrl(baseUrl:WebUrl, doc: Document) = {
    doc.getElementsByAttributeValue("rel", "canonical")
      .headOption
      .map(element => baseUrl.resolve(element.attr("href")))
  }

  private def outgoingLinks(baseUrl:WebUrl, doc: Document): List[WebUrl] = {
    if(noFollow(doc)) return List.empty
    (doc.getElementsByTag("a") ++ doc.getElementsByTag("link"))
      .flatMap{ element => extractLink(baseUrl, element) }
      .toList
  }

  private def noFollow(doc: Document) = {
    doc.getElementsByAttributeValue("name", "robots")
      .headOption
      .exists(element => element.attr("content").matches(NOFOLLOW))
  }

  private def extractLink(baseUrl:WebUrl, elem: Element) = {
    val href = elem.attr("href").trim
    val shouldFollow = elem.attr("rel") != "nofollow"
    val isCanonical = elem.attr("rel") == "canonical"
    val isWebPageLink = allowedSchemes(href) && notHasIllegalCharacters(href)
    if(isWebPageLink && shouldFollow && !isCanonical) Some(baseUrl.resolve(href)) else None
  }

  private def notHasIllegalCharacters(href:String) = !href.contains("@")

  private def allowedSchemes(href:String) = href match {
    case SCHEME(scheme) => schemes contains scheme.trim.toLowerCase
    case _ => true
  }
}

object PageParser {
  def apply() = new PageParser()
}
