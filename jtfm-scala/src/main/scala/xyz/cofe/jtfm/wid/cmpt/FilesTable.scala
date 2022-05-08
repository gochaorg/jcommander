package xyz.cofe.jtfm.wid.cmpt

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class FilesTable extends Table[Path] {
  
}

object FilesTable {  
  def humanSize(size:Long):String = {
    if size<0 then
      size.toString
    else
      if size < 1024 then
        size.toString
      else if size < 1024*1024 then
        // K
        (size / 1024)+"k"
      else if size < 1024*1024*1024 then
        // M
        (size / 1024 / 1024)+"m"
      else
        // G
        (size / 1024 / 1024 / 1024 )+"g"
  }

  val yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val HHmmss   = DateTimeFormatter.ofPattern("hh:mm:ss")

  def dateTimeShort(ft:FileTime):String = {
    val dt = ft.toInstant.atZone(ZoneId.systemDefault)
    val now = LocalDateTime.now
    if dt.getYear == now.getYear then
      HHmmss.format(dt)
    else
      yyyyMMdd.format(dt)
  }
  object columns {
    val fileName = Column[Path,String](
      "name", 
      path=>{ 
        val pref = 
          if Files.isSymbolicLink(path) then "~"
          else if Files.isDirectory(path) then "/"
          else " "
        pref + path.getFileName.toString
      },
      _.toString
    )    
    val size = {
      val c = Column[Path,Long](
        "size", 
        path => {
          if( Files.exists(path) && Files.isRegularFile(path) ){
            try {
              Files.size(path)
            } catch {
              case err:Throwable => -2L
            }
          }else{
            -1L
          }
        },
        size => humanSize(size)
      )
      c.width.prefect = Some(6)
      c
    }
    val latModifiedTime = {
      val c = Column[Path,FileTime](
        "modified",
        path => {
          try { Files.getLastModifiedTime(path) } catch {
            case _ : Throwable => null
          }
        },
        nullableFileTime => if nullableFileTime==null then "?" else dateTimeShort(nullableFileTime)
      )
      c.width.prefect = Some(10)
      c
    }
  }
}
