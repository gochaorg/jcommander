package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.NavigateFilter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyType
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.VirtualWidgetRoot
import xyz.cofe.jtfm.wid.Widget

import java.net.SocketTimeoutException

sealed trait State {
  /**
   * Переход в состояние Завершено
   */
  def finish():State.End = {
    this match {
      case w:State.Init => State.End()
      case w:State.Work => 
        w.shutdown.foreach( _(w) )
        State.End()
      case w:State.End => w
    }
  }
}

object State {
  /** Начальное состояние */
  case class Init(
    root: VirtualWidgetRoot,
    terminal: Terminal,
    jobs: Jobs = Jobs()
  ) extends State

  /** Рабочее состояние */
  case class Work(
                   terminal: Terminal,
                   screen: Screen,
                   shutdown: List[Work=>Unit],
                   visibleNavigator: Navigate[Widget[?]],
                   inputProcess: InputProcess = InputProcess.dummy( ks => List(KeyType.Escape, KeyType.Enter).contains(ks.getKeyType) ),
                   renderTree: WidgetTreeRender[Widget[_]],
                   throttling: Throttling = Throttling.Sleep(100),
                   ubHandler: UndefinedBehavior = UndefinedBehavior.TimeRateLimit(10000L, 8L),
                   focused: Option[Widget[_]] = None,
                   jobs: Jobs
  ) extends State

  /** Завершенное состояние */
  case class End() extends State

  implicit class InitState( val state:State.Init ) {
    def run():State.Work = 
    {
      val r_ls = new TerminalResizeListener {
        override def onResized( t:Terminal, sz:TerminalSize ):Unit = {
          state.root.rect.value( Rect(0,0, sz.getColumns, sz.getRows) )
        }
      };
      state.terminal.addResizeListener( r_ls )

      val screen = new TerminalScreen(state.terminal)          
      val sz = state.terminal.getTerminalSize()
      state.root.rect.value( Rect(0,0, sz.getColumns, sz.getRows) )

      screen.startScreen()
      screen.setCursorPosition(null)

      var shutdown = List[State.Work=>Unit]()
      shutdown = shutdown :+ { w => state.terminal.removeResizeListener(r_ls) }
      shutdown = shutdown :+ { w => screen.stopScreen() }
      shutdown = shutdown :+ { w => state.terminal.close() }

      val visibleFilter: NavigateFilter[? <: Widget[?]] = NavigateFilter.create( { _.visible.value } )
      val visibleNavigator: Navigate[Widget[?]] = Navigate.deepOrder

      State.Work(
        state.terminal,
        screen,
        shutdown,
        jobs = state.jobs,
        visibleNavigator = visibleNavigator,
        renderTree = WidgetTreeRender(state.root, screen)(visibleNavigator).repaitRequest()
      )
    }
  }
  
  type DoWork = State.Work => State
  implicit class CombineWork(val from:DoWork) {
    def next( to:DoWork ):DoWork = w => {
      from(w) match {
        case w_next:State.Work => to(w_next)
        case oth:_ => oth
      }
    }
  }

  implicit class WorkState( val state:State.Work ) {
    val screenResize:DoWork = w => {
      w.screen.doResizeIfNecessary()
      w
    }
    val render:DoWork = w => {
      try {
        w.renderTree.apply()
        w
      } catch {
        case e:Throwable => w.ubHandler(w,e)
      }
    }
    val throttling:DoWork = w => {
      w.throttling(w)
    }
    val inputs:DoWork = w => {
      try {
        val input = state.terminal.pollInput()
        if input != null then
          w.inputProcess.input(w, input)
        else
          w
      } catch {
        case e:SocketTimeoutException => w
        case e:Throwable => w.ubHandler(w, e)
      }
    }
    val screenRefresh:DoWork = w => { w.screen.refresh(); w }
    val jobRunner:DoWork = w => {
      w.jobs.run(w)
    }
    
    val allWorks:DoWork =
      inputs next
        jobRunner next
        screenResize next
        render next
        throttling next
        screenRefresh
    
    def run():State = {
      allWorks(state)
    }
  }
}
