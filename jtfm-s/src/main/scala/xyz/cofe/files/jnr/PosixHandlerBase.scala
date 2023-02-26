package xyz.cofe.files.jnr

import java.io.InputStream
import java.io.PrintStream
import java.io.File
import jnr.posix.POSIXHandler
import jnr.constants.platform.Errno
import jnr.posix.POSIXHandler.WARNING_ID
import xyz.cofe.files.util.AppendPrintStream

class PosixHandlerBase( out:Appendable, err:Appendable, verbose:Boolean ) extends PosixHandler(verbose):
  private lazy val outPrint = new AppendPrintStream(out)
  private lazy val errPrint = new AppendPrintStream(err)
  
  override def getInputStream(): InputStream = System.in
  override def getOutputStream(): PrintStream = outPrint
  override def getErrorStream(): PrintStream = errPrint

  override def error(error: Errno, extraData: String): Unit =
    errPrint.println(s"error $error extraData $extraData")

  override def error(error: Errno, methodName: String, extraData: String): Unit =
    errPrint.println(s"error $error method $methodName extraData $extraData")

  override def unimplementedError(methodName: String): Unit = 
    errPrint.println(s"unimplementedError $methodName")

  override def warn(id: WARNING_ID, message: String, data: Object*): Unit =
    errPrint.println(s"warn id=$id message=$message data=$data")


