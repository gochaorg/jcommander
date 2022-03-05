package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.NavigateFilter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.TerminalSize
import xyz.cofe.jtfm.gr.Rect

import xyz.cofe.jtfm.wid.VirtualWidgetRoot
import xyz.cofe.jtfm.wid.WidgetTreeRender
import xyz.cofe.jtfm.wid.Widget

sealed trait State
object State {
  /** Начальное состояние */
  case class Init(
    root: VirtualWidgetRoot,
    terminal: Terminal
  ) extends State

  /** Рабочее состояние */
  case class Work( 
    screen: Screen,
    shutdown: List[Work=>Unit],
    renderTree: WidgetTreeRender[Widget[_]],
    throttling: Throttling = Throttling.Sleep(10)
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

      val visibleFilter: NavigateFilter[? <: Widget[?]] = NavigateFilter.create( { _.visible.value } )
      val visibleNavigator: Navigate[Widget[?]] = Navigate.deepOrder

      State.Work(
        screen,
        shutdown,
        renderTree = WidgetTreeRender(state.root, screen)(visibleNavigator)
      )
    }
  }

  implicit class WorkState( val state:State.Work ) {
    def run():State = {
      try {
        state.renderTree.apply()
        state.throttling(state)
      } catch {
        case e:Throwable =>
          state
      }
    }

    def stop():State.End = {
      state.shutdown.foreach( _(state) )
      State.End()
    }
  }
}
