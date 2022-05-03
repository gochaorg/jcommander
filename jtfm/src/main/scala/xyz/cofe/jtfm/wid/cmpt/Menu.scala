package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Shortcut

/**
 * Создание меню
 */
@Deprecated
object Menu {
  def menubar( init:MenuBar ?=> Unit ):MenuBar = {
    given mb:MenuBar = MenuBar()
    init
    mb
  }
  def menu( text:String )( init:MenuContainer ?=> Unit )(using mb:MenuBar):MenuContainer = {
    given mc:MenuContainer = MenuContainer()
    mc.text.value = text
    mb.nested.append(mc)
    init
    mc
  }

  case class MABuild( 
    var whatDo:Option[()=>Unit]=None,
    var text:Option[String]=None,
    var shortcut:Option[Shortcut]=None
  ) {
    def tryMenuAction:Option[MenuAction] = whatDo match {
      case Some(call) =>         
        var ma = MenuAction( 
          action   = (_)=>call(),
          shortcut = shortcut
        )
        Some(ma)
      case None => None
    }
  }

  def action( init:MABuild ?=> Unit )( using mc:MenuContainer ):Option[MenuAction] = {
    given ma:MABuild = MABuild()
    init
    ma.tryMenuAction.map( m => {
      mc.nested.append(m)
      m
    })
  }

  def text( txt:String )( using ma:MABuild ):Unit = {
    ma.text = Some(txt)
  }

  def click( whatDo: => Unit )( using ma:MABuild ):Unit = {
    ma.whatDo = Some(()=>{ whatDo })
  }

  def shortcut( sc:String )( using ma:MABuild ):Unit = {
    ma.shortcut = Shortcut.parse(sc)
  }
}
