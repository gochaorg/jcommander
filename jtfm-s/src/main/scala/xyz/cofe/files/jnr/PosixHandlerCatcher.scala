package xyz.cofe.files.jnr

import java.io.StringWriter
import java.io.InputStream
import java.io.PrintStream
import java.io.File
import jnr.posix.POSIXHandler
import jnr.constants.platform.Errno
import jnr.posix.POSIXHandler.WARNING_ID
import xyz.cofe.files.util.AppendPrintStream

import xyz.cofe.files.util.AppendPrintStream

class PosixHandlerCatcher(verbose:Boolean) extends PosixHandler(verbose):
  lazy val outputBuffer = new StringWriter()
  private lazy val outPrint = new AppendPrintStream(outputBuffer)

  lazy val errputBuffer = new StringWriter()
  private lazy val errPrint = new AppendPrintStream(errputBuffer)

  var errors : List[PosixError] = List.empty
  
  override def getInputStream(): InputStream = System.in
  override def getOutputStream(): PrintStream = outPrint
  override def getErrorStream(): PrintStream = errPrint

  override def error(error: Errno, extraData: String): Unit =
    errors = errors :+ PosixError.Common(error, Option(extraData))

  override def error(error: Errno, methodName: String, extraData: String): Unit =
    errors = errors :+ PosixError.Method(error, Option(methodName),  Option(extraData))

  override def unimplementedError(methodName: String): Unit = 
    errors = errors :+ PosixError.Unimplemented(Option(methodName))

  override def warn(id: WARNING_ID, message: String, data: Object*): Unit =
    errors = errors :+ PosixError.Warning(Option(message), if data eq null then new Array[Object](0) else data.toArray)
