package bubblewrap

import java.util.regex.Pattern

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

case class Content(url: WebUrl, content: String, contentType: Option[String] = None, contentCharset: Option[String] = None,  contentEncoding: Option[String] = None) {
  def asString = content
  def asBytes = content.getBytes(contentCharset.getOrElse("UTF-8"))
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
  def apply(url: WebUrl, body: String, responseHeaders: ResponseHeaders): Content = {
    Content(url, body,responseHeaders.contentEncoding, responseHeaders.contentCharset, responseHeaders.contentType)
  }
}


class PageParser {
  val MetaUrl = "(?i)[^;]*;\\s*URL\\s*=(.*)".r
  def parse(content: Content) = {
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

  private def outgoingLinks(baseUrl:WebUrl, doc: Document) = {
    (doc.getElementsByTag("a") ++ doc.getElementsByTag("link"))
      .flatMap{ element => extractLink(baseUrl, element) }
      .toList
  }

  private def extractLink(baseUrl:WebUrl, elem: Element) = {
    val href = elem.attr("href")
    val shouldFollow = elem.attr("rel") != "nofollow"
    val isCanonical = elem.attr("rel") == "canonical"
    val isWebPageLink = !(href.contains("javascript:") || href.contains("mailto:") || href.contains("@"))
    if(isWebPageLink && shouldFollow && !isCanonical) Some(baseUrl.resolve(href)) else None
  }
}

object PageParser {
  def apply() = new PageParser()
}
