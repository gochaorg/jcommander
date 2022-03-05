package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.NavigateFilter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.TerminalSize
import xyz.cofe.jtfm.gr.Rect

/** Цикл управления событиями ввода и рендера.
  *
  * <p> 1 поток = 1 цикл обработки
  */
class WidgetCycle(
    private val terminal: Terminal
) {
  import Widget._

  /** Виджеты */
  implicit val root: VirtualWidgetRoot = VirtualWidgetRoot()

  private implicit val visibleFilter: NavigateFilter[? <: Widget[?]] = 
      NavigateFilter.create( { _.visible.value } )

  private implicit val visibleNavigator: Navigate[Widget[?]] = Navigate.deepOrder

  /** Состояние цикла */
  sealed trait State
  object State {
      /** Начальное состояние */
      case class Init() extends State

      /** Рабочее состояние */
      case class Work( 
        screen: Screen,
        shutdown: List[Work=>Unit],
      ) extends State

      /** Завершенное состояние */
      case class End() extends State
  }

  private var state : State = State.Init()

  implicit class InitState( val s:State.Init ) {
      def start():State.Work = {
          val r_ls = new TerminalResizeListener {
              override def onResized( t:Terminal, sz:TerminalSize ):Unit = {
                  root.rect.value( Rect(0,0, sz.getColumns, sz.getRows) )
              }
          };
          terminal.addResizeListener( r_ls )

          val screen = new TerminalScreen(terminal)          
          val sz = terminal.getTerminalSize()
          root.rect.value( Rect(0,0, sz.getColumns, sz.getRows) )

          screen.startScreen()
          screen.setCursorPosition(null)

          var shutdown = List[State.Work=>Unit]()
          shutdown = shutdown :+ { w => terminal.removeResizeListener(r_ls) }
          shutdown = shutdown :+ { w => screen.stopScreen() }

          State.Work(
              screen,
              shutdown
          )
      }
  }

  private def work(state:State.Work):Unit = {
      val renderTree = WidgetTreeRender(root, state.screen)(visibleNavigator)
      renderTree.apply()
  }

  /**
   * Запуск цикла обработки событий
   */
  def run(): Unit = {
      try {
      } finally {
        //state.close()
      }
  }
}

object WidgetCycle {
  private val currentCycle: InheritableThreadLocal[WidgetCycle] =
    new InheritableThreadLocal()

  def apply(term: Terminal): Either[WidgetCycle, Error] = {
    val wc0 = currentCycle.get
    if (wc0 == null) {
      val wc1 = new WidgetCycle(term)
      currentCycle.set(wc1)
      Left(wc1)
    } else {
      if (wc0.terminal == term) {
        Left(wc0)
      } else {
        Right(
          new Error("has already created WidgetCycle for different Terminal")
        )
      }
    }
  }

  /** Получение цикла обработки UI для текущего потока
    * @return
    *   цикл
    */
  def tryGet: Option[WidgetCycle] = {
    val x = currentCycle.get
    if (x != null) Some(x) else None
  }
}
