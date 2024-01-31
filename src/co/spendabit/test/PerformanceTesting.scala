package co.spendabit.test

import java.lang.management.ManagementFactory
import java.time.Duration

trait PerformanceTesting extends AwaitBackgroundTasks {

  protected def measureCpuTime(warnAfterSeconds: Long)(f: => Unit): java.time.Duration =
    measureCpuTime(Duration.ofSeconds(warnAfterSeconds))(f)

  protected def measureCpuTime(warnAfter: Duration)(f: => Unit): java.time.Duration = {

    var finalCpuTime: Option[Long] = None
    val thread = inSeparateThread {
      f
      finalCpuTime = Some(cpuTimeInNanoSeconds(Thread.currentThread))
    }

    while (finalCpuTime.isEmpty && thread.isAlive) {
      val nanoSecs = cpuTimeInNanoSeconds(Thread.currentThread)
      // XXX: I don't think this check is working properly...
      if (nanoSecs > warnAfter.toNanos && util.Random.nextInt(10) == 0) {
        val secs = cpuTimeInSeconds(Thread.currentThread)
        println("WARNING: Thread started by `measureCpuTimeInSeconds` has been running for " +
          s"$secs seconds; you may have to forcefully stop the test-run (Ctrl+C)")
      }
      Thread.sleep(100)
    }

    Duration.ofNanos(finalCpuTime.getOrElse {
      throw new TimeoutException("Operation never returned, and the thread is dead (perhaps an " +
        "unhandled exception occurred)")
    })
  }

  protected def measureCpuTimeInSeconds(warnAfterSeconds: Long)(f: => Unit): Double =
    measureCpuTime(warnAfterSeconds)(f).getSeconds

  protected def assertOperationTakesLessThan(seconds: Double)(f: => Unit): Unit = {
    val cpuTimeInSecs = measureCpuTimeInSeconds(warnAfterSeconds = (seconds * 5).toLong)(f)
    assert(cpuTimeInSecs < seconds,
      s"Operation took $cpuTimeInSecs seconds of CPU-time (and should have taken " +
        s"less than $seconds seconds)")
  }

  protected def inSeparateThread(f: => Unit): Thread = {
    val thread = new Thread {
      override def run(): Unit = { f }
    }
    thread.start()
    thread
  }

  protected def cpuTimeInSeconds(thread: Thread): Double =
    cpuTimeInNanoSeconds(thread) / 1000000000.0

  protected def cpuTimeInNanoSeconds(thread: Thread): Long = {
    if (!ManagementFactory.getThreadMXBean.isThreadCpuTimeSupported)
      throw new Exception("JVM does not support measuring thread CPU-time")
    ManagementFactory.getThreadMXBean.getThreadCpuTime(thread.getId)
  }

  protected def assertCPUTimeIsLimited(limitTo: Duration)(operation: => Unit): Unit = {
    val timeTaken = measureCpuTime(warnAfter = limitTo.multipliedBy(10))(operation)
    assert(timeTaken.toMillis > 0, "Took less than 1ms? Something must be amiss...")
    assert(timeTaken.toNanos < limitTo.toNanos,
      s"operation took at least ${timeTaken.toMillis} milliseconds, longer than limit " +
        s"of ${limitTo.toMillis} milliseconds")
  }
}
