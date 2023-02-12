package xyz.cofe.jtfm.ui.mkdir

import java.nio.file.Path
import xyz.cofe.term.ui.Dialog
import xyz.cofe.term.ui.Label
import xyz.cofe.term.common.Position
import xyz.cofe.term.ui.TextField
import xyz.cofe.term.ui.prop.bind
import xyz.cofe.term.geom.Rect
import xyz.cofe.files._
import xyz.cofe.jtfm.ui.warn.WarnDialog
import xyz.cofe.term.ui.Button
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.ui.conf.DialogConf
import xyz.cofe.term.ui.conf.DialogColorConf
import xyz.cofe.term.ui._
import xyz.cofe.term.common.Size
import xyz.cofe.jtfm.ui.Promise

object MkDirDialog:
  def open( parentDirectory:Path )(using conf:DialogConf, colors:DialogColorConf):Promise[Unit] =
    val prom = Promise[Unit]()
    Dialog
      .title("mk dir")
      .relocateWhenOpen { rootw => rootw.center(Size(rootw.size.width-6,10)) }
      .content { (panel,hdl) =>
        val label = Label("name:")
        panel.children.append(label)
        label.location = Position(1,0)
        
        val input = TextField()

        panel.children.append(input)
        input.bind(panel) { b => Rect(1,1,b.width-2,1) }

        var validName = true
        def tryCreate =
          if validName then
            val targetDir = parentDirectory.resolve(input.text.get)
            targetDir.createDirectory() match
              case Left(err) =>
                WarnDialog.message(err.getMessage()).title(err.getClass().getSimpleName()).show
              case Right(value) => 
                hdl.close()
                prom.ok.emit()

        val butOk = Button("Ok")
        panel.children.append(butOk)
        butOk.bind(panel) { b => Rect(b.width-3, b.height-1, 2, 1) }

        input.text.onChange( (_,txt) => {
          validName = txt.nonEmpty
          butOk.foregroundColor = if validName then Color.White else Color.RedBright
        })

        val butCancel = Button("Cancel")
        panel.children.append(butCancel)
        butCancel.bind(panel){b => Rect(1,b.height-1,6,1)}
        butCancel.action { hdl.close() }

        butOk.action { tryCreate }
        input.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Enter,false,false,false), TextField.Action.Custom(tf => {
          tryCreate
        }))

        hdl.onOpen { input.focus.request }
      }
      .onClose { prom.closed.emit() }
      .open()
    prom