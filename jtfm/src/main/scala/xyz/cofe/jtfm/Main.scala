package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, ExtendedTerminal, MouseCaptureMode, Terminal}
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer

import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicReference

enum ParseCmdLineState:
  case Init
  case TelnetPort
  case TelnetUse

/**
 * Опции командной строки
 * @param state Состояние парсера коммандной строки
 * @param telnetStart Запустить telnet сервер
 * @param telnetPort Порт на котором заустить telnet
 * @param unparsed Не распознанные параметры
 * @param termConfigure Конфигурация работы мыши в терминале
 */
case class CmdLineOpt
(
  /** Состояние парсера коммандной строки */
  state: ParseCmdLineState = ParseCmdLineState.Init,

  /** Запустить telnet сервер */
  telnetStart: Boolean = false,

  /** Порт на котором заустить telnet */
  telnetPort: Int = 4044,

  /** Не распознанные параметры */
  unparsed: List[String] = List(),

  /** Конфигурация работы мыши в терминале */
  termConfigure: (Terminal => Terminal) = trm => {
    trm match {
      case t: ExtendedTerminal => t.setMouseCaptureMode(MouseCaptureMode.CLICK)
      case _ => 
    }
    trm
  }
):
  /**
   * Применение настроек к терминалу
   *
   * @param term терминал
   * @return терминал
   */
  def apply(term: Terminal): Terminal =
    termConfigure(term)
    term
    
  def start()=
    if( telnetStart )
      startTelnet(this)
    else
      startDefault(this)

object CmdLineOpt:
  
  import ParseCmdLineState._
  
  def parse(args: Seq[String]): CmdLineOpt =
    args.foldLeft(CmdLineOpt())((opt, arg) => {
      opt.state match {
        case Init => arg match {
          case "-telnet.port" | "-t.port" => opt.copy(state = TelnetPort, telnetStart = true)
          case "-telnet" => opt.copy(state = TelnetUse)
          case _ => opt.copy(unparsed = opt.unparsed ::: List(arg))
        }
        case TelnetPort => opt.copy(telnetPort = arg.toInt, state = Init)
        case TelnetUse => arg match {
          case "yes" | "true" | "on" => opt.copy(state = Init, telnetStart = true)
          case "no" | "false" | "off" => opt.copy(state = Init, telnetStart = false)
          case s if s.matches("\\d+") => opt.copy(state = Init, telnetStart = true, telnetPort = s.toInt)
          case _ => opt.copy(unparsed = opt.unparsed ::: List(arg), state = Init)
        }
      }
    })

/**
 * Входная точка програмы
 * @param args аргументы коммандной строки
 */
@main def hello(args: String*): Unit =
  CmdLineOpt.parse(args).start()

/**
 * Старт в обычном режиме
 * @param opt опции командной строки
 */
def startDefault(opt:CmdLineOpt): Unit =
  Session(opt(DefaultTerminalFactory().createTerminal()))

/**
 * Сессия запущенная в отдельном потоке
 * @param startSesssion получение/запуск сессии
 */
class ThreadSession( private val startSesssion:()=>Session ):
  private val ses:AtomicReference[Session] = new AtomicReference[Session](null)

  val sessionThread = new Thread():
    override def run():Unit =
      ses.set(startSesssion())
  
  sessionThread.setDaemon(true)
  sessionThread.setName("terminal session")
  sessionThread.start()
  
  def kill() =
    val s = ses.get()
    if s!=null then
      s.terminate()

/**
 * Старт telnet сервера
 * @param opt опции
 */
def startTelnet(opt:CmdLineOpt): Unit =
  println(s"starting telnet on ${opt.telnetPort} port")
  val telNet = new TelnetTerminalServer(opt.telnetPort)
  telNet.getServerSocket.setSoTimeout( 1000 * 2 )

  val sessionManager = new SessionManager[ThreadSession]()
  val listener = sessionManager.listen( ()=>{
    try {
      val term = telNet.acceptConnection()
      Some( ThreadSession(()=>Session(term)) )
    } catch {
      case e:SocketTimeoutException => None
    }
  })

  var stop = false
  while 
    !stop
  do
    val inputs = new java.util.Scanner( System.in )
    System.out.println("enter 'exit' for stop >")
    val line = inputs.nextLine.trim
    if line.equalsIgnoreCase("exit") then
      listener.stopNow()
      stop = true
    else if line.equalsIgnoreCase("ses") then
      println(
        s"""
           |sessions ${sessionManager.sessions.size}
           |  alive ${sessionManager.sessions.filter(_.sessionThread.isAlive).size}
           |""".stripMargin.trim )

  implicit val termSes1 = new Terminable[Session] {
    def terminate(s:Session):Unit = s.terminate()
  }
  implicit val termSes2 = new Terminable[ThreadSession] {
    def terminate(s:ThreadSession):Unit = s.kill()
  }
  sessionManager.terminateAll
  