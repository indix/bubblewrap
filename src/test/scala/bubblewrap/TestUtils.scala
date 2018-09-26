package bubblewrap

import org.apache.commons.io.IOUtils

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}

object TestUtils {
  def get[T](future:Future[T], duration:FiniteDuration = 100 millis): T = Await.result(future, duration)
  def readAsString(resource: String, encoding: String = "UTF-8"): String = IOUtils.toString(getClass.getResourceAsStream(resource),encoding)
  def readAsBytes(resource: String, encoding: String = "UTF-8") = IOUtils.toByteArray(getClass.getResourceAsStream(resource))
}
