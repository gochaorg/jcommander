package xyz.cofe.jtfm.bg.exch

import java.time.Duration

case class SyncSenderConf(
  timeout:  Duration,
  throttle: Duration,
)

object SyncSenderConf:
  given defaultConf:SyncSenderConf = SyncSenderConf(
    timeout = Duration.ofSeconds(5),
    throttle = Duration.ofMinutes(25)
  )

