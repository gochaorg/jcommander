package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.DefaultTerminalFactory

enum ParseState:
  case Init
  case TelnetPort
  case TelnetUse

case class CmdLineOpt(
                       state: ParseState = ParseState.Init,
                       telnetPort: Int = 4044,
                       telnetStart: Boolean = false,
                       unparsed: List[String] = List()
                     )

object CmdLineOpt:
  
  import ParseState._
  
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

@main def hello(args: String*): Unit =
  CmdLineOpt.parse(args) match {
    case CmdLineOpt(_, telnetPort, true, _) =>
      println(s"start telnet here on port $telnetPort")
    case CmdLineOpt(_, _, false, _) =>
      println("start as is")
    case _ =>
      throw new Error("undefined beheavior")
  }

//startDefault()

def startDefault(): Unit =
  Session(DefaultTerminalFactory().createTerminal())


