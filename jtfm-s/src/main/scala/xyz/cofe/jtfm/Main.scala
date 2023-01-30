package xyz.cofe.jtfm

import xyz.cofe.files.AppHome
import xyz.cofe.term.common.ConsoleBuilder
import xyz.cofe.term.common.Console
import xyz.cofe.term.ui.Session
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.geom._
import xyz.cofe.jtfm.ui.table.DirectoryTable
import xyz.cofe.term.common.Position
import xyz.cofe.term.ui.VSplitPane
import java.nio.file.Path
import xyz.cofe.term.ui.MenuBar
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.ui.Widget
import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.files.log.PathPattern
import xyz.cofe.files.log.PathPattern.Evaluate
import xyz.cofe.files.log.AppendableFile
import xyz.cofe.term.ui
import xyz.cofe.term.ui._
import xyz.cofe.term.ui.ses._
import xyz.cofe.jtfm.log.JsonLogOutput
import xyz.cofe.term.buff.ScreenBufferSyncLog
import xyz.cofe.cli.CmdLine
import xyz.cofe.term.ui.log.CommonLog
import xyz.cofe.cli.CmdLineDefault
import xyz.cofe.term.common.Size

object Main:
  implicit object appHome extends AppHome("jtfm")
  implicit val pathPatternEval : Evaluate = Evaluate.defaultEvaluate
  lazy val logPathCommon = "{appHome}/log/{yyyy}/{MM}/{dd}/{HH}-{mi}-pid{pid}-"

  lazy val simpleLogPath = PathPattern.parse(Path.of(s"${logPathCommon}simple.log"))
  lazy val simpleLog : AppendableFile = AppendableFile(simpleLogPath,Some(1024*1024*16))

  lazy val sesInputLogPath = PathPattern.parse(Path.of(s"${logPathCommon}sesInput.json"))
  lazy val sesInputOut = AppendableFile(sesInputLogPath,Some(1024*1024*16))

  given sesInputLog : SesInputLog = SesInputLog.writeTo(new JsonLogOutput[SesInputLog.SesInputEvent](sesInputOut))
  given sessionLog : SessionLog = SessionLog.simple(simpleLog)
  given screenBufferSyncLog : ScreenBufferSyncLog = ScreenBufferSyncLog.simple(simpleLog)
  
  CommonLog.setOutput(simpleLog)

  case class WinConsole(
    width:Int,
    height:Int,    
    buffSet:Int,
    rectSet:Int,
  )



  object WinConsole:
    given defaults : CmdLineDefault[WinConsole] = 
      CmdLineDefault
        ("buffSet","1")
        ("rectSet","2")

  def resize( winCon:xyz.cofe.term.win.WinConsole, target:Size ):Unit =
    def resizeWidth:Unit =
      val sbuff0 = winCon.getScreenBufferInfo()
      simpleLog.append(s"resizeWidth to ${target.width()} x ${target.height()} from ${sbuff0.getWidthMax()}(max) x ${sbuff0.getHeightMax()}(max)\n")
      if sbuff0.getWidth() < target.width() then
        simpleLog.append(s"bufferSize ${target.width()} ${sbuff0.getHeight()}\n")
        winCon.getOutput().bufferSize( target.width(), sbuff0.getHeight() )

        simpleLog.append(s"windowRect ${target.width()-1} ${sbuff0.getHeight()-1}\n")
        winCon.getOutput().windowRect( 0, 0, target.width()-1, sbuff0.getHeight()-1 )
      else if sbuff0.getWidth() > target.width() then
        simpleLog.append(s"windowRect ${target.width()-1} ${sbuff0.getHeight()-1}\n")
        winCon.getOutput().windowRect( 0, 0, target.width()-1, sbuff0.getHeight()-1 )

        simpleLog.append(s"bufferSize ${target.width()} ${sbuff0.getHeight()}\n")
        winCon.getOutput().bufferSize( target.width(), sbuff0.getHeight() )
    def resizeHeight:Unit =
      val sbuff0 = winCon.getScreenBufferInfo()
      simpleLog.append(s"resizeHeight to ${target.width()} x ${target.height()} from ${sbuff0.getWidthMax()}(max) x ${sbuff0.getHeightMax()}(max)\n")
      if sbuff0.getHeight() < target.height() then
        simpleLog.append(s"bufferSize ${sbuff0.getWidth()} ${target.height()}\n")
        winCon.getOutput().bufferSize( sbuff0.getWidth(), target.height() )

        simpleLog.append(s"windowRect ${sbuff0.getWidth()-1} ${target.height()-1}\n")
        winCon.getOutput().windowRect( 0, 0, sbuff0.getWidth()-1, target.height()-1 )
      else if sbuff0.getHeight() > target.height() then
        simpleLog.append(s"windowRect ${sbuff0.getWidth()-1} ${target.height()-1}\n")
        winCon.getOutput().windowRect( 0, 0, sbuff0.getWidth()-1, target.height()-1 )

        simpleLog.append(s"bufferSize ${sbuff0.getWidth()} ${target.height()}\n")
        winCon.getOutput().bufferSize( sbuff0.getWidth(), target.height() )
    resizeWidth
    resizeHeight

  def main(args0:Array[String]):Unit =
    simpleLog.append(s"starting\n")
    simpleLog.append(s"args:\n")
    args0.foreach { arg => simpleLog.append(s"  arg $arg\n") }
    try      
      simpleLog.append(s"create win console\n")
      val winConsole = 
        new xyz.cofe.term.win.WinConsole(
          new xyz.cofe.term.win.ConnectToConsole.TryAttachParent()
        )

      val savedScreenBufMode = winConsole.getScreenBufferMode()
      val savedInputMode = winConsole.getInputMode()

      winConsole.setScreenBufferMode(
        winConsole.getScreenBufferMode().newLineAutoReturn(false).processing(false).wrapAtEol(false)
      )

      winConsole.setInputMode(
        winConsole.getInputMode()
          .echo(false)
          .insert(false)
          .line(false)
          .mouse(true)
          //.processing(false)
          //.virtualTerminal(false)
          .quickEdit(false)
          .window(true)
      )

      // resize order
      // w=150 h=20 >>w=150 h=40  rectSet, buffSet - fail
      // w=150 h=20 >>w=150 h=40  buffSet, rectSet - ok
      // w=150 h=40 >>w=150 h=20  buffSet, rectSet - fail
      // w=150 h=40 >>w=150 h=20  
      
      val sbuff = winConsole.getScreenBufferInfo()
      simpleLog.append(s"width=${sbuff.getWidth()} width.max=${sbuff.getWidthMax()} height=${sbuff.getHeight()} height.max=${sbuff.getHeightMax()}\n")

      val args = args0.toList
      if args.nonEmpty then
        args.headOption match
          case Some("win.size") =>
            CmdLine.parse[WinConsole](args.tail) match
              case Left(err) => simpleLog.append(s"fail parse win.size $err\n")
              case Right((argz,rest)) => 
                simpleLog.append(s"resize terminal\n")
                resize(winConsole, Size(argz.width, argz.height))
          case _ => ()

      val sbuff1 = winConsole.getScreenBufferInfo()
      simpleLog.append(s"win.console size ${sbuff1.getWidth()} x ${sbuff1.getHeight()}\n")

      val console = new xyz.cofe.term.common.win.WinConsole(winConsole)
      val size2 = console.getSize()
      simpleLog.append(s"console size ${size2.width()} x ${size2.height()}\n")

      simpleLog.append(s"start session\n")
      startSession(console)

      simpleLog.append(s"restore console\n")
      winConsole.getOutput().setScreenBufferMode(savedScreenBufMode)
      winConsole.getInput().setInputMode(savedInputMode)

      simpleLog.append(s"closing console\n")

      console.close()
    catch
      case err:Throwable =>
        simpleLog.append(s"catched err $err\n")
    finally
      simpleLog.append(s"finished\n")

  private var mbarOpt : Option[WidgetInput] = None
  def startSession( console: Console ):Unit =
    simpleLog.append(s"startSession\n")
    Session.start(console) { implicit ses =>
      simpleLog.append(s"build session\n")
      import xyz.cofe.term.ui.menuBuilder._

      val leftPanel  = new DirectoryTable
      val rightPanel = new DirectoryTable

      leftPanel.directory.set(Some(Path.of(".")))
      rightPanel.directory.set(Some(Path.of(".")))

      val vsplitPanel = VSplitPane()
      ses.rootWidget.children.append(vsplitPanel)
      vsplitPanel.bind( ses.rootWidget ){ case (root) => 
        val rect = (root.leftTop + (0,1), root.rightBottom).rect 
        simpleLog.append(s"recompute vsplit root: ${root.size}, vsplit $rect\n")
        rect
      }
      
      vsplitPanel.leftWidget.set(Some(leftPanel))
      vsplitPanel.rightWidget.set(Some(rightPanel))

      ses.requestFocus(leftPanel)
      leftPanel.selection.focusedIndex.set(Some(1))

      menuBar {
        menu("File") {
          action("Exit").exec( executorOf(Action.Exit) )
        }
        mbarOpt = Some(menu("View") {
          action("Show menu").keyStroke(KeyStroke.KeyEvent(KeyName.F5,false,false,false)) {
            mbarOpt.foreach { mbar => mbar.focus.request }
          }
        })
      }
    }

  def executorOf(action:Action)(using ses:Session): ()=>Unit =
    action match
      case Action.Exit => ()=>{ ses.stop = true }
      case Action.ActivateMainMenu => ()=>{

      }

  enum Action:
    case Exit
    case ActivateMainMenu
