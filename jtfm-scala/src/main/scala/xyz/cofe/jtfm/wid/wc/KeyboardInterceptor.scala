package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.KeyStroke
import xyz.cofe.jtfm.wid.Shortcut
import xyz.cofe.jtfm.wid.Shortcut.SeqShortcut
import xyz.cofe.jtfm.wid.wc.KeyboardInterceptor.Behavior
import xyz.cofe.jtfm.wid.WidgetCycle
import org.slf4j.LoggerFactory

/** Перехват нажатия клавиатуры */
class KeyboardInterceptor {
  private val log = LoggerFactory.getLogger(classOf[KeyboardInterceptor])

  private var bindings:Map[Shortcut,()=>Behavior] = Map()
  def accept( state:State.Work, hist:List[KeyStroke] ):Option[State] = {
    hist.size match {
      case 0 => 
        log.debug("KeyboardInterceptor no")
        None
      case _ =>
        log.debug("KeyboardInterceptor accept head={} as shortcut={}",
          hist.head,
          hist.headOption.flatMap( ks => Shortcut.parse(ks) )
        )
        bindings.map { (shrt,action) => (shrt.test(hist),action) }.find { _._1 } match {
          case None => 
            log.debug("KeyboardInterceptor not matched")
            None
          case Some((shrt,action)) =>
            log.info("KeyboardInterceptor has match shortcut={} action={}",shrt,action)
            action() match {
              case Behavior.Continue => 
                None
              case Behavior.Eat => 
                Some(state)
              case Behavior.Exit => 
                Some(state.finish())
            }
        }
    }
  }
}

object KeyboardInterceptor {
  /** 
   * Дальнейшее поведение при выполнии action
   * 
   * - Continue - Продолжить как есть
   * - Eat - завершить обработку Shortcut, дальнейшие Shortcut не обрабатывать, продолжить работу приложения
   * - Exit - завершить работу приложения
   */
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

  /**
   * Как продолжить работу после выполнения action
   */  
  case class Bind( shortcut:Shortcut ):
    private var name:Option[String] = None
    private def createAction( action: =>Unit, behavior:Behavior ):Function0[Behavior] = 
      name match {
        case None => ()=>{
          action
          behavior
        }
        case Some(actionName) => new Function0[Behavior]() {
          override def apply():Behavior = {
            action
            behavior
          }

          override def toString():String = s"Action $actionName by shortcut $shortcut"
        }
      }
    def eat(action: =>Unit ):Unit = {
      bind(shortcut, createAction(action,Behavior.Eat))
    }
    def continue(action: =>Unit ):Unit = {
      bind(shortcut, createAction(action,Behavior.Continue))
    }
    def exit(action: =>Unit ):Unit = {
      bind(shortcut, createAction(action,Behavior.Exit))
    }

  def bind(shortcut:Shortcut):Bind = Bind(shortcut)
}