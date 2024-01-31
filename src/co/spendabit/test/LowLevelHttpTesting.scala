package co.spendabit.test

import java.net.URL
import javax.servlet.http.HttpServlet

import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}

trait LowLevelHttpTesting {

  /** Given an `HttpServlet`, run it as an independent HTTP server on a local port and run the
    * function `f`, passing it a URL at which the running server can be reached.
    */
  protected def withServletRunningOnLocalPort[T](servlet: HttpServlet)(f: URL => T): T = {

    val handler = new ServletContextHandler(ServletContextHandler.SESSIONS)
    handler.setContextPath("/")

    /* A port of zero means, "randomly allocated port". */
    val server = new Server(0)
    server.setHandler(handler)
    server.start()

    val holder = new ServletHolder(servlet.getClass.getName, servlet)
    handler.addServlet(holder, "/*")

    val port = server.getConnectors.head.asInstanceOf[ServerConnector].getLocalPort
    val url = new URL(s"http://localhost:$port/")
    val result = f(url)
    server.stop()

    result
  }
}
