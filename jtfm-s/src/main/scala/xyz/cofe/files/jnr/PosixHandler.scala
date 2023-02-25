package xyz.cofe.files.jnr

import jnr.posix.POSIXHandler
import xyz.cofe.files.util.AppendPrintStream
import java.io.InputStream
import java.io.PrintStream
import jnr.constants.platform.Errno
import java.io.File
import jnr.posix.POSIXHandler.WARNING_ID

class PosixHandler( out:Appendable, err:Appendable, verbose:Boolean ) extends POSIXHandler:
  private lazy val outPrint = new AppendPrintStream(out)
  private lazy val errPrint = new AppendPrintStream(err)
  
  override def getInputStream(): InputStream = System.in
  override def getOutputStream(): PrintStream = outPrint
  override def getErrorStream(): PrintStream = errPrint

  private lazy val pid = { ProcessHandle.current().pid() }
  override def getPID(): Int = pid.toInt

  private lazy val env = {
    var envList = List.empty[String]
    System.getenv().forEach( (a,b) => envList = envList :+ (a+"="+b) )
    envList.toArray
  }
  override def getEnv(): Array[String] = env

  private lazy val curWorkDir = new File(".").getAbsoluteFile()
  override def getCurrentWorkingDirectory(): File = curWorkDir

  override def isVerbose(): Boolean = verbose

  override def error(error: Errno, extraData: String): Unit =
    errPrint.println(s"error $error extraData $extraData")

  override def error(error: Errno, methodName: String, extraData: String): Unit =
    errPrint.println(s"error $error method $methodName extraData $extraData")

  override def unimplementedError(methodName: String): Unit = 
    errPrint.println(s"unimplementedError $methodName")

  override def warn(id: WARNING_ID, message: String, data: Object*): Unit =
    errPrint.println(s"warn id=$id message=$message data=$data")
