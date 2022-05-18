package xyz.cofe.jtfm.wid.cmpt

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import org.slf4j.LoggerFactory

class FilesTable extends Table[Path] {
  import FilesTable.log
}

object FilesTable {  
  val log = LoggerFactory.getLogger(classOf[FilesTable])

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
    val fileName = Column.create[Path,String](
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
      val c = Column.create[Path,Long](
        "size", 
        path => {
          if( Files.exists(path) && Files.isRegularFile(path) ){
            try {
              Files.size(path)
            } catch {
              case err:Throwable => 
                log.warn(s"can't read size of $path",err)
                -2L
            }
          }else{
            log.debug(s"not file or exists $path")
            -1L
          }
        },
        size => humanSize(size)
      )
      c.width.prefect = Some(6)
      c
    }
    val latModifiedTime = {
      val c = Column.create[Path,FileTime](
        "modified",
        path => {
          try { Files.getLastModifiedTime(path) } catch {
            case err : Throwable => 
              log.warn(s"can't read getLastModifiedTime of $path",err)
              null
          }
        },
        nullableFileTime => if nullableFileTime==null then "?" else dateTimeShort(nullableFileTime)
      )
      c.width.prefect = Some(10)
      c
    }
  }

  object sort {
    lazy val defaultSort = combine(List(
      dirFirst, byName
    ))

    val dirFirst:(Path,Path)=>Int = (a,b) => {
      val aDir = try { Files.isDirectory(a) } catch { 
        case e:Throwable => log.warn(s"can't read attr(isDirectory) of $a",e); false
      }
      val bDir = try { Files.isDirectory(b) } catch { 
        case e:Throwable => log.warn(s"can't read attr(isDirectory) of $b",e); false
      }
      aDir == bDir match {
        case true => 0
        case _ => if aDir then -1 else 1
      }
    }

    val byName:(Path,Path)=>Int = (a,b) => {
      val aName = a.getFileName.toString
      val bName = b.getFileName.toString
      if aName==null && bName==null then
        0
      else if aName!=null && bName==null then
        -1
      else if aName==null && bName!=null then
        1
      else
        aName.compareTo(bName)
    }

    def combine( sorts:Seq[(Path,Path)=>Int] ):(Path,Path)=>Int = (fa,fb)=> {
      if sorts.isEmpty then
        0
      else
        var cmpRes = 0
        sorts.foldLeft( 0 )( (a,b) => {
          if a==0 then
            b(fa,fb)
          else 
            a
        })
    }

    def inverse( cmpFn:(Path,Path)=>Int ):(Path,Path)=>Int = (a,b) => cmpFn(a,b) * -1
  }
}
