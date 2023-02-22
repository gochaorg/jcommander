package xyz.cofe.jtfm

import xyz.cofe.files._
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZoneOffset
import xyz.cofe.files.extension
import java.time.Instant
import java.time.Duration
import xyz.cofe.files.deleteIfExists
import xyz.cofe.files.move

object LogPrepare:
  def prepare(using appHome:AppHome):Unit =
    val logDir = appHome.directory.resolve("log")
    System.setProperty("LOG_DIR",logDir.toString())
    archivate
    cleanupArchive

  def archivate(using appHome:AppHome):Unit =
    val logDir = appHome.directory.resolve("log")
    val archDir = logDir.resolve("arch")

    if ! archDir.exists.getOrElse(true) 
    then 
      archDir.createDirectories()

    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")

    logDir.readDir.foreach { files =>      
      files.filter(_.isRegularFile.getOrElse(true)).foreach { file =>
        file.fileTime.map(_.lastModified).foreach { lastMod => 
          val timeStr = lastMod.atZone(ZoneOffset.UTC).format(df)
          val ext = file.`extension`.getOrElse("")
          val target = archDir.resolve(s"arc-${timeStr}.${ext}")
          file.move(target)
        }
      }
    }

  def cleanupArchive(using appHome:AppHome):Unit =
    val logDir = appHome.directory.resolve("log")
    val archDir = logDir.resolve("arch")
    val keepDuration = Duration.ofDays(7)
    val keepAfter = Instant.now().plusSeconds( 0 - keepDuration.toSeconds() )
    archDir.readDir.foreach { files =>
      files
        .sortBy(_.fileTime.map(_.lastModified).getOrElse(Instant.now()))
        .filter(_.fileTime.map(_.lastModified).map(t => t.compareTo(keepAfter)<0 ).getOrElse(false))
        .foreach(_.deleteIfExists())
    }
