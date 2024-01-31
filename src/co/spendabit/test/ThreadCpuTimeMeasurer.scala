package co.spendabit.test

import scala.collection.{Seq, mutable}

import org.scalameter.{Context, Quantity}
import org.scalameter.Measurer.{IterationBasedValue, Timer}

/** A `Measurer` for scalameter that uses the "CPU time" for the current thread (via
  * method `getThreadCpuTime`) to measure operation time.
  */
class ThreadCpuTimeMeasurer extends Timer with IterationBasedValue with PerformanceTesting {

  @volatile private var snippetResult: Any = _

  def name = "ThreadCpuTimeMeasurer"

  def measure[T](context: Context, measurements: Int, setup: T => Any,
                 tear: T => Any, regen: () => T, snippet: T => Any): Seq[Quantity[Double]] = {
    var iteration = 0
    val times = mutable.ListBuffer.empty[Quantity[Double]]
    var value = regen()

    while (iteration < measurements) {
      value = valueAt(context, iteration, regen, value)
      setup(value)

      val threadStart = cpuTimeInNanoSeconds(Thread.currentThread)
      snippetResult = snippet(value)
      val threadEnd = cpuTimeInNanoSeconds(Thread.currentThread)
      val threadTime = Quantity((threadEnd - threadStart) / 1000000.0, "ms")

      tear(value)

      times += threadTime
      iteration += 1
    }
    snippetResult = null

    times.result()
  }
}
