package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.KeyStroke
import xyz.cofe.jtfm.wid.Shortcut
import xyz.cofe.jtfm.wid.Shortcut.SeqShortcut
import xyz.cofe.jtfm.wid.wc.KeyboardInterceptor.Behavior
import xyz.cofe.jtfm.wid.WidgetCycle

/** Перехват нажатия клавиатуры */
class KeyboardInterceptor {
  private def log(msg:String):Unit = {
    //println(msg)
  }

  private var bindings:Map[Shortcut,()=>Behavior] = Map()
  def accept( state:State.Work, hist:List[KeyStroke] ):Option[State] = {
    log("KeyboardInterceptor accept")
    hist.size match {
      case 0 => 
        log("KeyboardInterceptor no")
        None
      case _ =>
        log(s"KeyboardInterceptor accept head=${hist.head}")
        bindings.map { (shrt,action) => (shrt.test(hist),action) }.find { _._1 }.map { _._2 } match {
          case None => 
            log("KeyboardInterceptor not matched")
            None
          case Some(action) =>
            log("KeyboardInterceptor has match")
            action() match {
              case Behavior.Continue => None
              case Behavior.Eat => Some(state)
              case Behavior.Exit => Some(state.finish())
            }
        }
    }
  }
}

object KeyboardInterceptor {
  enum Behavior:
    case Continue
    case Eat
    case Exit

  def bind( shortcut:Shortcut, action:()=>Behavior ):Unit = {
    WidgetCycle.tryGet match {
      case None => throw new IllegalStateException("WidgetCycle not prepared/started (tryGet)")
      case Some(wc) => wc.jobs match {
        case None => throw new IllegalStateException("can't get jobs of WidgetCycle")
        case Some(jobs) => jobs.add( ()=>{
            wc.workState match {
              case None => {
              }
              case Some(ws) =>
                ws.keyInterceptor.bindings = ws.keyInterceptor.bindings + ( shortcut -> action )
            }
          }
        )
      }
    }
  }

  case class Bind( shortcut:Shortcut ):
    def eat(action: =>Unit ):Unit = {
      bind(shortcut, ()=>{
        action
        Behavior.Eat
      })
    }
    def continue(action: =>Unit ):Unit = {
      bind(shortcut, ()=>{
        action
        Behavior.Continue
      })
    }
    def exit(action: =>Unit ):Unit = {
      bind(shortcut, ()=>{
        action
        Behavior.Exit
      })
    }

  def bind(shortcut:Shortcut):Bind = Bind(shortcut)
}