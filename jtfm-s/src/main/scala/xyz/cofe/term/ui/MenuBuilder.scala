package xyz.cofe.term.ui

import xyz.cofe.term.ui.prop.WidgetChildren
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig

object menuBuilder {
  def menuBar( init: (WidgetChildren[Menu], MenuColorConfig) ?=> Unit )
  (using ses:Session, mbConfig:MenuBarColorConfig, mConf:MenuColorConfig)
  :MenuBar =
    implicit val bar : MenuBar = new MenuBar
    init
    bar.install(ses.rootWidget)
    bar

  def menu ( text:String )
           ( init: (WidgetChildren[Menu]) ?=> Unit )
           ( using menuParent:WidgetChildren[Menu] 
           ,       mConf:MenuColorConfig
           )
  :MenuContainer =
    val mc = new MenuContainer(text)
    given newParent:WidgetChildren[Menu] = mc
    init
    menuParent.children.append(mc)
    mc

  def action( text:String )
            ( using 
              menuParent:WidgetChildren[Menu], 
              config: MenuColorConfig
            )
  :ActionConf =
    val ma = MenuAction(text)
    val ac = ActionConf(ma)
    menuParent.children.append(ma)
    ac

  case class ActionConf( ma:MenuAction ):
    def keyStroke(ks:KeyStroke):ActionConf =
      ma.keyStroke(ks)
      this
    def apply( code: => Unit ):MenuAction =
      ma.action(code)
      ma
    def exec( code: () => Unit ):MenuAction =
      ma.action { code() }
      ma
      
}
