package xyz.cofe.jtfm.metric

import io.undertow.Undertow
import io.undertow.util.Headers

import xyz.cofe.metric._

class MetricExport(using conf:MetricConf):
  val server = Undertow.builder()
    .addHttpListener(conf.port,conf.host)
    .setHandler( exchange => {
      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html")

      //exchange.getPath

      val s0 = Metrics.trackers.all.map { case (name,t) =>         
        val durNano = t.duration.value
        val durMs = durNano / 1000000.0
        s"<tr><td>${name}</td><td>${t.count.value}</td><td>${durMs} ms</td></tr>"
      }.mkString("\n")
      val s1 = s"<table>$s0</table>"

      val head = s"<head><meta http-equiv=\"refresh\" content=\"3\"></head>"
      val body = s"<body>$s1</body>"
      val html = s"<html>$head $body</html>"
      
      exchange.getResponseSender().send(s1)
    })
    .build()

  def start():Unit =
    server.start()

  def stop():Unit =
    server.stop()
