package xyz.cofe.jtfm.ui.copy

import java.nio.file.Path
import xyz.cofe.term.ui.Dialog
import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.TextField
import xyz.cofe.term.ui.Label
import xyz.cofe.term.ui.Button
import xyz.cofe.jtfm.ui.Promise
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.ui.prop.bind
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.Position
import xyz.cofe.term.geom.Rect
import xyz.cofe.term.ui.conf.DialogConf
import xyz.cofe.term.ui.conf.DialogColorConf
import xyz.cofe.term.common.Color
import xyz.cofe.files.PathFilter
import xyz.cofe.jtfm.ui.copy.ResolveSymLink

object CopyDialog:
  case class CopyOption(
    resolveSymLinks:ResolveSymLink,
    saveAttribs:Boolean,
    sourceFiles:List[Path],
    filter:PathFilter,
    target:Path,
    copyIntoFolder:Boolean
  )

  private val saveAttribSelected   = "[ ] save attrib"
  private val saveAttribUnSelected = "[x] save attrib"

  private val intoFolderSelected   = "[ ] into folder"
  private val intoFolderUnSelected = "[x] into folder"

  private val noResolveSelected   = "(*) no resolve"
  private val noResolveUnSelected = "( ) no resolve"

  private val absResolveSelected   = "(*) absolute resolve"
  private val absResolveUnSelected = "( ) absolute resolve"

  def open(files:List[Path], to:Path)(using conf:DialogConf, colors:DialogColorConf):Promise[CopyOption] =
    val prom = Promise[CopyOption]()
    Dialog
      .title("Copy files")
      .relocateWhenOpen( rootWid => rootWid.center(Size( rootWid.size.width()-4,15)) )
      .content( (pnl,hdl) => {
        var resolvSymLink : ResolveSymLink = ResolveSymLink.None
        var saveAttrib : Boolean = true
        var intoFolder : Boolean = true
        ///////////////
        val inputLabel = Label("Input filter:")
        val inputFileFilter = TextField()
        inputFileFilter.text = "*"

        val outputLabel = Label("Output target:")
        val outputDir = TextField()
        outputDir.text = to.toString()
        outputDir.selectAll()
        outputDir.moveCursorEnd(true)
        hdl.onOpen(outputDir.focus.request)

        val optLabel = Label("Options:")
        val saveAttrButton = Button()
        val intoFolderButton = Button()

        val nonResolveButton = Button()
        val absResolveButton = Button()

        val okButton = Button("Ok")

        val cancelButton = Button("Cancel")
        /////////////////////////////////////

        pnl.children.append(inputLabel)
        inputLabel.location = Position(1,0)
        inputLabel.size = Size(15,1)

        pnl.children.append(inputFileFilter)
        inputFileFilter.bind(pnl)(b=>Rect(0,1,b.width,1))

        pnl.children.append(outputLabel)
        outputLabel.location = Position(1,2)
        outputLabel.size = Size(15,1)

        pnl.children.append(outputDir)
        outputDir.bind(pnl)(b=>Rect(0,3,b.width,1))

        pnl.children.append(optLabel)
        optLabel.location = Position(1,4)

        pnl.children.append(nonResolveButton)
        nonResolveButton.location = Position(1,5)
        nonResolveButton.size = Size(20,1)

        pnl.children.append(absResolveButton)
        absResolveButton.location = Position(1,6)
        absResolveButton.size = Size(20,1)

        pnl.children.append(saveAttrButton)
        saveAttrButton.location = Position(25,5)
        saveAttrButton.size = Size(20,1)

        pnl.children.append(intoFolderButton)
        intoFolderButton.location = Position(25,6)
        intoFolderButton.size = Size(20,1)

        pnl.children.append(okButton)
        okButton.location  = Position(20,8)        

        pnl.children.append(cancelButton)
        cancelButton.location  = Position(1,8)

        List(saveAttrButton,nonResolveButton,absResolveButton,intoFolderButton).foreach { b =>
          b.foregroundColor = Color.Black
          b.backgroundColor = Color.White
        }

        /////////////////////////////////////
        cancelButton.action { hdl.close() }

        def render:Unit =
          absResolveButton.text = absResolveUnSelected
          nonResolveButton.text = noResolveUnSelected
          resolvSymLink match
            case ResolveSymLink.None => 
              nonResolveButton.text = noResolveSelected
            case ResolveSymLink.ResolveAbsolute =>
              absResolveButton.text = absResolveSelected

          intoFolderButton.text = if intoFolder then intoFolderSelected else intoFolderUnSelected
          saveAttrButton.text = if saveAttrib then saveAttribSelected else saveAttribUnSelected

        absResolveButton.action { resolvSymLink = ResolveSymLink.ResolveAbsolute; render }
        nonResolveButton.action { resolvSymLink = ResolveSymLink.None; render }
        intoFolderButton.action { intoFolder = ! intoFolder; render }
        saveAttrButton.action   { saveAttrib = ! saveAttrib; render }

        render

        def emitOk:Unit =
          prom.ok.emit(CopyOption(
            resolveSymLinks = resolvSymLink,
            saveAttribs = saveAttrib,
            sourceFiles = files,
            filter = PathFilter.Wildcard(inputFileFilter.text.get),
            target = Path.of(outputDir.text.get),
            copyIntoFolder = intoFolder
          ))

        okButton.action { emitOk; hdl.close() }
        List(outputDir,inputFileFilter).foreach(_.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Enter,false,false,false), TextField.Action.Custom(tf => {
          emitOk; hdl.close()
        })))

        hdl.onOpen { outputDir.focus.request }
      })
      .onClose {
        prom.closed.emit(())
      }
      .open()
    prom
