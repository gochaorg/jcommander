package xyz.cofe.jtfm.metric

import io.undertow.Undertow
import io.undertow.util.Headers

import io.undertow.Handlers
import io.undertow.server.HttpHandler

import xyz.cofe.metric._
import java.lang.management._

class MetricHttpExport(using conf:MetricConf):
  val threadInfo = ManagementFactory.getThreadMXBean()

  val metricHtmlHandler : HttpHandler = exchange => {
    val tracks = Metrics.trackers.all.map { case (name,t) =>         
      val durNano = t.duration.value
      val durMs = durNano / 1000000.0
      s"metric ${name} ${t.count.value} ${durMs} ms"
    }.mkString("<br/>")

    val thInfo = threadInfo.getAllThreadIds().sorted.map { thId => 
      ( threadInfo.getThreadInfo(thId)
      , threadInfo.getThreadCpuTime(thId)
      , threadInfo.getThreadUserTime(thId)
      )
    }.map { case (ti,cpu,usr) => 
      s"thread <a href='/thread/${ti.getThreadId()}'>id=${ti.getThreadId()}</a> name=${ti.getThreadName()} state=${ti.getThreadState()} cpu=${cpu} usr=${usr}"
    }.mkString("<br/>")

    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html")
    exchange.getResponseSender().send(List(tracks,thInfo).mkString("<p/>"))
  }

  val threadHandler : HttpHandler = exchange => {
    // println(exchange.getRequestPath())
    // println(exchange.getPathParameters())
    // println(exchange.getQueryParameters())

    val ti = threadInfo.getThreadInfo(
      exchange.getQueryParameters().get("id").getFirst().toLong
    )

    val thId =
      s"""
        name: ${ti.getThreadName()} <br/>
        id: ${ti.getThreadId()} <br/>
        state: ${ti.getThreadState()}
      """

    val stackTrace = new StringBuilder

    val thAll = Thread.getAllStackTraces()
    thAll.forEach { case (th,st) => 
      if( th.getId()==ti.getThreadId() ){
        stackTrace.append( st.mkString("<br/>") )
      }
    }

    val body = s"$thId <br> stack: <br> $stackTrace"

    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html")
    exchange.getResponseSender().send(body)
  }

  val defaultHandler : HttpHandler = exchange => {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html")
    exchange.getResponseSender().send("<a href='/metric/html'>metric html</a><br>")
  }

  val routing = Handlers.routing()
    .get("/metric/html", metricHtmlHandler)
    .get("/thread/{id}", threadHandler)
    .get("/",defaultHandler)

  val server = Undertow.builder()
    .addHttpListener(conf.port,conf.host)
    .setHandler( routing )
    .build()

  def start():Unit =
    server.start()

  def stop():Unit =
    server.stop()
