package xyz.cofe.metric

import java.util.concurrent.atomic.AtomicLong

trait Metrics[M]:
  def all:Map[String,M]
  def metric(name:String):M

trait Mono[M]:
  def add(m:M, v:Long):Unit

class MetricLong:
  private val value_ = AtomicLong(0)
  def value:Long = value_.get()

  def add(v:Long):Unit =
    value_.addAndGet(v)

object MetricLong:
  given Mono[MetricLong] with
    override def add(m: MetricLong, v: Long): Unit = 
      m.add(v)

class MetricsData[M](builder:String=>M) extends Metrics[M]:
  @volatile var all:Map[String,M] = Map.empty
  def metric(name: String): M = 
    this.synchronized {
      val mOpt = all.get(name)
      mOpt.getOrElse {
        val r = builder(name)
        all = all + (name -> r)
        r
      }
    }

case class Tracker[M:Mono]( duration:M, count:M )

extension [M:Mono]( m:Tracker[M] )
  def apply[R]( code: =>R ):R =
    val start = System.nanoTime()
    val res = code
    val stop = System.nanoTime()
    summon[Mono[M]].add(m.duration, stop-start)
    summon[Mono[M]].add(m.count, 1)
    res

object Metrics:
  lazy val mono = MetricsData(
    builder = name => new MetricLong
  )

  lazy val trackers = MetricsData(
    builder = name => Tracker(
      duration = mono.metric(name+".duration"),
      count = mono.metric(name+".count"),
    )
  )

  def tracker(name:String) = trackers.metric(name)