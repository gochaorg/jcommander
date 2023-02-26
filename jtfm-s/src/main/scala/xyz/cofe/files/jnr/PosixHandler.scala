package xyz.cofe.files.jnr

import jnr.posix.POSIXHandler
import xyz.cofe.files.util.AppendPrintStream
import java.io.InputStream
import java.io.PrintStream
import jnr.constants.platform.Errno
import java.io.File
import jnr.posix.POSIXHandler.WARNING_ID
import jnr.posix.POSIXFactory

object PosixHandler:
  def create( out:Appendable, err:Appendable, verbose:Boolean ):PosixHandlerBase = new PosixHandlerBase(out,err,verbose)

  def use[R]( code:PosixHandler => R ):PosixResult[R] =     
    val handler = new PosixHandlerCatcher(true)
    val result = code(handler)
    val outStr = handler.outputBuffer.toString()
    val errStr = handler.errputBuffer.toString()
    PosixResult( 
      result, 
      handler.errors, 
      Option.when(outStr.nonEmpty)(outStr),
      Option.when(errStr.nonEmpty)(errStr)
    )


trait PosixHandler( verbose:Boolean ) extends POSIXHandler:
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

  lazy val posix = POSIXFactory.getPOSIX(this,true)
