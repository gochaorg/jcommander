package xyz.cofe.jtfm.bg.exch

import java.util.concurrent.atomic.AtomicLong

trait IdGenerator[A]:
  def generate:A

object IdGenerator:
  given linear:IdGenerator[Long] =
    val idSeq = new AtomicLong(0)
    new IdGenerator[Long] {
      override def generate: Long = 
        idSeq.incrementAndGet()
    }