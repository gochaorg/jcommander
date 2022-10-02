package xyz.cofe.jtfm

import ui.conf.MainWindowConfig
import xyz.cofe.jtfm.store.AppHome
import xyz.cofe.jtfm.files._
import xyz.cofe.jtfm.store.json._
import java.io.IOError
import java.io.IOException

case class AppConfig(
  mainWindow: MainWindowConfig = MainWindowConfig(),
  saveOnExit: Boolean = true
)

object AppConfig:
  val configFilename = "jtfm.json"
  lazy val configFile = AppHome.directory.resolve(configFilename)

  private var activeConfigInst:Option[AppConfig] = None  

  def activeConfig:AppConfig =
    activeConfigInst match
      case Some(conf) => conf
      case None =>
        val initConfig = configFile.exists match
          case Right(true)  =>
            configFile.readJson[AppConfig] match
              case Right(conf) =>
                activeConfigInst = Some(conf)
                activeConfigInst.get
              case Left(err) =>
                println(s"can't read $configFile: $err")
                activeConfigInst = Some(AppConfig())
                activeConfigInst.get
          case _ =>
            activeConfigInst = Some(AppConfig())
            activeConfigInst.get
        if initConfig.saveOnExit then
          saveOnExit()
        initConfig

  def activeConfig_=(conf:AppConfig):Option[AppConfig] =
    val last = activeConfigInst
    activeConfigInst = Some(conf)
    last

  def save:Unit =
    this.synchronized {
      activeConfigInst match
        case Some(conf) => conf.saveOnExit match
          case true => 
            this.synchronized {
              if saveOnExitCalls<1 then saveOnExit()
            }
          case false =>
        case _ =>
      for
        prnt <- configFile.parent match
          case Some(p) => Right(p)
          case None => Left(s"parent of $configFile not exists")
        prntExists <- prnt.exists
        prntCreated <- if prntExists 
          then Right(false)
          else prnt.createDirectories.map(_ => true)
        confWrited <- activeConfigInst match
          case Some(conf) =>
            configFile.writeJson(conf).map( _ => true)
          case None => Right(false)
      yield (confWrited)
    }

  @volatile private var saveOnExitCalls = 0
  private def saveOnExit():Unit =
    this.synchronized {
      saveOnExitCalls += 1    
      Runtime.getRuntime().addShutdownHook(new Thread("save config on exit"){
        override def run(): Unit = 
          save
      })
    }

  object lens:  
    val mainWindow = Lens[AppConfig,MainWindowConfig](
      get= a => a.mainWindow,
      set= (a,b) => a.copy(mainWindow = b)
    )
    val saveOnExit = Lens[AppConfig,Boolean](
      get= a => a.saveOnExit,
      set= (a,b) => a.copy(saveOnExit = b)
    )
