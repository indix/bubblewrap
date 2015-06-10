package bubblewrap

import java.util.regex.Pattern

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

case class Page(metaRefresh:Option[WebUrl], canonicalUrl:Option[WebUrl], outgoingLinks:List[WebUrl])

class PageParser {
  val MetaUrl = "(?i)[^;]*;\\s*URL\\s*=(.*)".r
  def parse(url:WebUrl, page: String) = {
    val doc = Jsoup.parse(page)
    Page(metaRefresh(url, doc), canonicalUrl(url, doc), outGoingLinks(url, doc))
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

  private def outGoingLinks(baseUrl:WebUrl, doc: Document) = {
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
