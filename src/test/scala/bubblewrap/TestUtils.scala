package bubblewrap

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}

object TestUtils {
  def get[T](future:Future[T], duration:FiniteDuration = 100 millis): T = Await.result(future, duration)
}
