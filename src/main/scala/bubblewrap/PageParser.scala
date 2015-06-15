package bubblewrap

import java.util.regex.Pattern

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

case class Page(url: WebUrl, content: String, contentType: Option[String] = None, contentEncoding: Option[String] = None,
                contentCharset: Option[String] = None, metaRefresh:Option[WebUrl] = None,
                canonicalUrl:Option[WebUrl] = None, outgoingLinks:List[WebUrl] = List.empty)

object Page{
  def apply(url: WebUrl, body: String, responseHeaders: ResponseHeaders): Page = {
    Page(url, body, responseHeaders.contentType, responseHeaders.contentEncoding, responseHeaders.contentCharset)
  }
}


class PageParser {
  val MetaUrl = "(?i)[^;]*;\\s*URL\\s*=(.*)".r
  def parse(page: Page) = {
    val doc = Jsoup.parse(page.content)
    page.copy(metaRefresh = metaRefresh(page.url, doc), canonicalUrl = canonicalUrl(page.url, doc), outgoingLinks = outgoingLinks(page.url, doc))
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
