package xyz.cofe.jtfm

import org.slf4j.LoggerFactory
import xyz.cofe.term.common.{Console => Term}
import xyz.cofe.term.common.ConsoleBuilder
import xyz.cofe.jtfm.json._
import xyz.cofe.files.AppHome
import xyz.cofe.jtfm.Main.appHome
import _root_.xyz.cofe.term.win.{ WinConsole => WCon }
import _root_.xyz.cofe.term.common.win.{ WinConsole => WtCon }
import xyz.cofe.jtfm.conf.ConfFile
import xyz.cofe.files.FilesLogger
import xyz.cofe.term.common.Size
import scala.util.Try
import scala.util.Failure
import scala.util.Success

object PrepareConsole:
  val logger = LoggerFactory.getLogger("xyz.cofe.jtfm.PrepareConsole")
  implicit val fsLogger:FilesLogger = FilesLogger.slf(logger, FilesLogger.Level.Info, FilesLogger.Level.Warn)
  def log(message:String):Unit = logger.info(message)

  private def telnetConsole(port:Int, async:Boolean):Term =
    log(s"build telnet console port=$port, async=$async")
    System.setProperty("xyz.cofe.term.telnet.port", port.toString())
    System.setProperty("xyz.cofe.term.telnet.async", async.toString())
    ConsoleBuilder.telnetConsole()

  private def telnetConsole(con:Telnet):Term = telnetConsole(con.port, con.async)

  private def unixConsole(async:Boolean):Term =
    log(s"build unix async=$async")
    System.setProperty("xyz.cofe.term.nix.async", async.toString())
    ConsoleBuilder.nixConsole()

  private def unixConsole(con:Unix):Term = unixConsole(con.async)

  @volatile private var winConsoleHolder:Option[WCon] = None

  def winConsole( f:WCon => Any ):Unit =
    winConsoleHolder.foreach(f)

  def resize( con:WCon, target:Size ):Unit =
    def resizeWidth:Unit =
      val sbuff0 = con.getScreenBufferInfo()
      log(s"resizeWidth to ${target.width()} x ${target.height()} from ${sbuff0.getWidthMax()}(max) x ${sbuff0.getHeightMax()}(max)\n")
      if sbuff0.getWidth() < target.width() then
        log(s"bufferSize ${target.width()} ${sbuff0.getHeight()}\n")
        con.getOutput().bufferSize( target.width(), sbuff0.getHeight() )

        log(s"windowRect ${target.width()-1} ${sbuff0.getHeight()-1}\n")
        con.getOutput().windowRect( 0, 0, target.width()-1, sbuff0.getHeight()-1 )
      else if sbuff0.getWidth() > target.width() then
        log(s"windowRect ${target.width()-1} ${sbuff0.getHeight()-1}\n")
        con.getOutput().windowRect( 0, 0, target.width()-1, sbuff0.getHeight()-1 )

        log(s"bufferSize ${target.width()} ${sbuff0.getHeight()}\n")
        con.getOutput().bufferSize( target.width(), sbuff0.getHeight() )
    def resizeHeight:Unit =
      val sbuff0 = con.getScreenBufferInfo()
      log(s"resizeHeight to ${target.width()} x ${target.height()} from ${sbuff0.getWidthMax()}(max) x ${sbuff0.getHeightMax()}(max)\n")
      if sbuff0.getHeight() < target.height() then
        log(s"bufferSize ${sbuff0.getWidth()} ${target.height()}\n")
        con.getOutput().bufferSize( sbuff0.getWidth(), target.height() )

        log(s"windowRect ${sbuff0.getWidth()-1} ${target.height()-1}\n")
        con.getOutput().windowRect( 0, 0, sbuff0.getWidth()-1, target.height()-1 )
      else if sbuff0.getHeight() > target.height() then
        log(s"windowRect ${sbuff0.getWidth()-1} ${target.height()-1}\n")
        con.getOutput().windowRect( 0, 0, sbuff0.getWidth()-1, target.height()-1 )

        log(s"bufferSize ${sbuff0.getWidth()} ${target.height()}\n")
        con.getOutput().bufferSize( sbuff0.getWidth(), target.height() )
    Try {
      resizeWidth
      resizeHeight
    } match
      case Failure(exception) => logger.error("can't resize",exception)
      case Success(value) =>
    

  private def winConsole_(connect:WinConnect)(using appHome:AppHome):Term =
    log(s"build win console connect=$connect")
    val conn = connect match
      case WinConnect.Alloc =>  new xyz.cofe.term.win.ConnectToConsole.AllocConsole()
      case WinConnect.Attach =>  new xyz.cofe.term.win.ConnectToConsole.AttachParent()
      case WinConnect.TryAttach =>  new xyz.cofe.term.win.ConnectToConsole.TryAttachParent()
    
    val winTerm = prepare(new WCon(conn))
    winConsoleHolder = Some(winTerm)

    val con = new WtCon(winTerm)
    con

  private def prepare(winCon: xyz.cofe.term.win.WinConsole)(using appHome:AppHome):xyz.cofe.term.win.WinConsole =
    log("prepare WinConsole")
    ConsoleSize.confFile.read.foreach { conSize => 
      resize(winCon, conSize.size)
    }
    winCon

  private def winConsole(con:Windows)(using appHome:AppHome):Term = winConsole_(con.connect)

  def createConsole(using appHome:AppHome):Term =
    log("create console")
    val ccon = CreateConsole.confFile.read

    val telnet = ccon.flatMap(c => Right(c.telnet)).map(_.getOrElse(defaultTelnet)).getOrElse(defaultTelnet)
    val nix = ccon.flatMap(c => Right(c.unix)).map(_.getOrElse(defaultUnix)).getOrElse(defaultUnix)
    val win = ccon.flatMap(c => Right(c.windows)).map(_.getOrElse(defaultWindows)).getOrElse(defaultWindows)

    Option(System.getProperty("jtfm.console")) match
      case Some("telnet") => 
        log("create telnet console by sys properties")
        telnetConsole(telnet)
      case Some("win") => 
        log("create windows console by sys properties")
        winConsole(win)
      case Some("nix") => 
        log("create unix console by sys properties")
        unixConsole(nix)
      case _ => 
        log("create telnet console by default")
        telnetConsole(defaultTelnet)

  case class Telnet(port:Int, async:Boolean)
  val defaultTelnet = Telnet(12348,true)

  case class Windows(connect:WinConnect)
  val defaultWindows = Windows(WinConnect.TryAttach)

  case class Unix(async:Boolean)
  val defaultUnix = Unix(true)

  enum WinConnect(val name:String):
    case Alloc     extends WinConnect("alloc")
    case Attach    extends WinConnect("attach")
    case TryAttach extends WinConnect("tryAttach")

  case class CreateConsole(
    telnet:  Option[Telnet],
    windows: Option[Windows],
    unix:    Option[Unix],    
  )

  object CreateConsole:
    def confFile(using appHome:AppHome):ConfFile[CreateConsole] =
      ConfFile.Fallback(
        ConfFile.File(appHome.directory.resolve("create-console.jsonc")),
        ConfFile.Resource("/default-config/create-console.jsonc")
      )

  case class ConsoleSize( 
    width: Int,
    height: Int
  ):
    lazy val size : Size = Size(width,height)

  object ConsoleSize:
    def confFile(using appHome:AppHome):ConfFile[ConsoleSize] =
      ConfFile.Fallback(
        ConfFile.File(appHome.directory.resolve("console-size.jsonc")),
        ConfFile.Resource("/default-config/console-size.jsonc")
      )
