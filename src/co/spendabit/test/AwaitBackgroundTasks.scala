package co.spendabit.test

import scala.annotation.tailrec

/** Simple functionality useful for waiting on background tasks to complete an operation.
  */
trait AwaitBackgroundTasks {

  /** Wait for the given `predicate` to return `true`, sleeping 1/10th of a second between
    * each check, and waiting for a maximum of `maxSecondsToWait` seconds.
    */
  @tailrec
  final protected def waitUntil(predicate: => Boolean,
                                maxSecondsToWait: Double = 60.0): Unit = {
    if (!predicate) {
      if (maxSecondsToWait <= 0) {
        throw new TimeoutException("Waited maximum amount of time and predicate is still false")
      } else {
        Thread.sleep(100)
        waitUntil(predicate, maxSecondsToWait - 0.1)
      }
    }
  }

  protected def waitUntil(predicate: => Boolean): Unit =
    waitUntil(predicate, maxSecondsToWait = 60.0)

  class TimeoutException(m: String) extends Exception(m)
}
