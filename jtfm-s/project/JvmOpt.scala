package dist

import JvmOpt.SizeSuff.KBytes
import JvmOpt.SizeSuff.MBytes
import JvmOpt.SizeSuff.GBytes
import JvmOpt.GCLogRotation.UseGCLogFileRotation
import JvmOpt.GCLogRotation.Default

sealed trait JvmOpt {
  def cmdLine:String
}

object JvmOpt {
  sealed trait SizeSuff
  object SizeSuff {
    object KBytes extends SizeSuff
    object MBytes extends SizeSuff
    object GBytes extends SizeSuff
  }

  case class Custom(cmdLine:String) extends JvmOpt
  case class MemInitalSize(size:Int, suff:SizeSuff) extends JvmOpt {
    val cmdLine:String = suff match {
      case KBytes => s"-Xms${size}k"
      case MBytes => s"-Xms${size}m"
      case GBytes => s"-Xms${size}g"
    }
  }

  case class MemMaxSize(size:Int, suff:SizeSuff) extends JvmOpt {
    val cmdLine:String = suff match {
      case KBytes => s"-Xmx${size}k"
      case MBytes => s"-Xmx${size}k"
      case GBytes => s"-Xmx${size}k"
    }
  }

  case class MaxMetaspaceSize(size:Int, suff:SizeSuff) extends JvmOpt {
    val cmdLine:String = suff match {
      case KBytes => s"-XX:MaxMetaspaceSize=${size}k"
      case MBytes => s"-XX:MaxMetaspaceSize=${size}k"
      case GBytes => s"-XX:MaxMetaspaceSize=${size}k"
    }
  }

  sealed trait GCType
  object GCType {
    object Serial extends GCType with JvmOpt { val cmdLine = "-XX:+UseSerialGC" }
    object Parallel extends GCType with JvmOpt { val cmdLine = "-XX:+UseParallelGC" }
    object CMS extends GCType with JvmOpt { val cmdLine = "-XX:+USeParNewGC" }
    object G1 extends GCType with JvmOpt { val cmdLine = "-XX:+UseG1GC" }
  }

  case class MByte(count:Int) extends AnyVal
  case class NumberOfGCLogFiles(num:Int) extends AnyVal
  
  sealed trait GCLogRotation
  object GCLogRotation {
    object UseGCLogFileRotation extends GCLogRotation
    object Default extends GCLogRotation
  }

  case class GCLogOutput(path:String)

  case class GCLog( output:GCLogOutput, numOfGCLogFiles:NumberOfGCLogFiles, logFileSize:MByte, rotation:GCLogRotation ) extends JvmOpt {
    def cmdLine: String = 
      {List[String](
        s"-Xloggc:${output.path}",
        s"-XX:GCLogFileSize=${logFileSize.count}M",
        s"-XX:NumberOfGCLogFiles=${numOfGCLogFiles.num}"
      ) ++ {
        rotation match {
          case UseGCLogFileRotation => List("-XX:+UseGCLogFileRotation")
          case Default => List.empty[String]
        }
      }}.mkString(" ")
  }
}
