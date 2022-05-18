package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, ExtendedTerminal, MouseCaptureMode, Terminal}
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer

import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicReference
import java.io.InputStream
import xyz.cofe.jtfm.wid.wc.InputStreamHist

import java.nio.charset.{Charset, StandardCharsets}

enum ParseCmdLineState:
  case Init
  case TelnetPort
  case TelnetUse
  case TelnetCharset

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

  /** Кодировка telnet */
  telnetCharset: Option[Charset] = Some(StandardCharsets.ISO_8859_1),

  /** Не распознанные параметры */
  unparsed: List[String] = List(),

  /** Конфигурация работы мыши в терминале */
  termConfigure: (Terminal => Terminal) = trm => {
    trm match {
      case t: ExtendedTerminal =>
        t.setMouseCaptureMode(MouseCaptureMode.CLICK)
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
          case "-telnet.charset" => opt.copy(state = TelnetCharset)
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
        case TelnetCharset => opt.copy(state = Init, telnetCharset = Some(Charset.forName(arg)))
      }
    })

/**
 * Входная точка програмы
 * @param args аргументы коммандной строки
 */
@main def hello(args: String*): Unit =
  //System.setIn( InputStreamHist(System.in,100) )
  CmdLineOpt.parse(args).start()

/**
 * Старт в обычном режиме
 * @param opt опции командной строки
 */
def startDefault(opt:CmdLineOpt): Unit =
  Session(opt(DefaultTerminalFactory().createTerminal())).run()

/**
 * Сессия запущенная в отдельном потоке
 * @param openSesssion получение/запуск сессии
 */
class ThreadSession( private val openSesssion:()=>Session ):
  private val ses:AtomicReference[Session] = new AtomicReference[Session](null)

  val sessionThread = new Thread():
    override def run():Unit =
      val ses1=openSesssion()
      ses.set(ses1)
      ses1.run()
      println(s"stop session at thread ${Thread.currentThread().getId} : ${Thread.currentThread().getName}")
  
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
  val telNet = opt.telnetCharset match {
    case None => new TelnetTerminalServer(opt.telnetPort)
    case Some(cs) => new TelnetTerminalServer(opt.telnetPort, cs)
  }
  telNet.getServerSocket.setSoTimeout( 1000 * 2 )

  val sessionManager = new SessionManager[ThreadSession]()
  val listener = sessionManager.listen( ()=>{
    try {
      val term = telNet.acceptConnection()
      Some( ThreadSession(()=>{
        Thread.currentThread().setName(s"terminal session ${term.getRemoteSocketAddress}")
        println(s"accept connection from ${term.getRemoteSocketAddress} and start thread ${Thread.currentThread().getId} : ${Thread.currentThread().getName}")
        Session(opt(term))
      }))
    } catch {
      case e:SocketTimeoutException => None
    }
  })
  
  implicit val termSes1 = new Terminable[Session] { def terminate(s:Session):Unit = s.terminate() }
  implicit val termSes2 = new Terminable[ThreadSession] { def terminate(s:ThreadSession):Unit = s.kill() }

  println(
    s"""|commands:
        |  ls - list sessions
        |  exit - finish all work and exit
        |  kill <th_id> - kill session
        |""".stripMargin.trim)

  var stop = false
  while 
    !stop
  do
    val inputs = new java.util.Scanner( System.in )
    println("enter 'exit' for stop >")
    val line = inputs.nextLine.trim
    if line.equalsIgnoreCase("exit") then
      listener.stopNow()
      stop = true
    else if line.equalsIgnoreCase("ls") then
      sessionManager.remove( ses => !ses.sessionThread.isAlive )
      
      println(
        s"""
           |sessions count ${sessionManager.sessions.size}
           |""".stripMargin.trim )
      sessionManager.sessions.foreach { ses =>
        println(
          s"""
              |  th_id=${ses.sessionThread.getId} alive=${ses.sessionThread.isAlive} name=${ses.sessionThread.getName}
              |""".stripMargin.trim )
      }
    else
      "kill (?<th>[0-9]+)".r.findFirstMatchIn(line) match {
        case Some(m) =>
          
          sessionManager.sessions
            .filter( _.sessionThread.getId == m.group("th").toLong )
            .filter( _.sessionThread.isAlive )
            .foreach { ses => sessionManager.terminate(ses) }
        case _ =>
      }
  sessionManager.terminateAll
  