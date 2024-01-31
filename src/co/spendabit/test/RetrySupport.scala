package co.spendabit.test

import scala.annotation.tailrec

import org.scalatest._

/** This trait is useful for test-cases that are not 100% deterministic, e.g., because they depend
  * on small variances, perhaps random, in values that determine test outcome. It is also useful
  * for performance-related test-cases, whereby timing can vary greatly dependent upon the
  * load (e.g., CPU usage, RAM availability) of the computer running the test(s).
  *
  * Using this trait, one can flag such test-cases to be "retried" when they "flicker" (when they
  * fail, presumably for reasons unrelated to the code under test), helping to avoid a spurious
  * failed test outcome.
  *
  * In addition to including this trait in your `TestSuite`, flag any test-cases which qualify to
  * be re-run with the `Retryable` tag.
  */
trait RetrySupport extends TestSuite with Retries {

  protected val maxRetries = 5

  protected var retriesSoFar = 0

  override protected def withFixture(test: NoArgTest): Outcome = {
    if (isRetryable(test)) {
      retriesSoFar = 0
      withRetries({ super.withFixture(test) }, numRetriesLeft = maxRetries)
    } else {
      super.withFixture(test)
    }
  }

  @tailrec
  private def withRetries(testCase: => Outcome, numRetriesLeft: Int,
                          hasFailed: Boolean = false): Outcome = {
    val firstOutcome = testCase
    firstOutcome match {
      case Failed(_) =>
        if (numRetriesLeft == 0)
          firstOutcome
        else {
          note(s"Test failed, but is being retried (up to $numRetriesLeft times more)")
          retriesSoFar += 1
          withRetries(testCase, numRetriesLeft - 1, hasFailed = true)
        }
      case Succeeded =>
        if (hasFailed)
          alert("Test \"flickered\" " + retriesSoFar + " times, " +
            s"but passed after try #${retriesSoFar + 1}")
        retriesSoFar = 0
        firstOutcome
      case other => other
    }
  }

  protected def note: Notifier

  protected def alert: Alerter
}
