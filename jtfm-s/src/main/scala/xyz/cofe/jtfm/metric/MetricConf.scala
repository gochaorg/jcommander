package xyz.cofe.jtfm.metric

import xyz.cofe.files.AppHome
import xyz.cofe.jtfm.conf.ConfFile
import xyz.cofe.jtfm.conf.ConfError

import xyz.cofe.jtfm.metric.MetricHttpExport
case class MetricConf(
  enabled:Boolean,
  host:String,
  port:Int
):
  def run[R]( code: =>R ):R =
    implicit val conf:MetricConf = this
    val metricExport = new MetricHttpExport
    if enabled 
    then
      try
        metricExport.start()
        code
      finally
        metricExport.stop()
    else
      code

object MetricConf:
  def confFile(appHome:AppHome):ConfFile[MetricConf] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("metrics.jsonc")),
      ConfFile.Resource("/default-config/metrics.jsonc")
    )

  def read(using appHome:AppHome):Either[ConfError,MetricConf] =
    confFile(appHome).read  

  val defaultConf = MetricConf(
    enabled = false,
    host = "localhost",
    port = 8080
  )
    