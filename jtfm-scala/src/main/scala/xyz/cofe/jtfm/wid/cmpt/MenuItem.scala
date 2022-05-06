package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.TextProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.WidgetCycle
import xyz.cofe.jtfm.wid.Widget.likeTree
import xyz.cofe.jtfm.tree._
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

/**
 * Пункт меню
 */
trait MenuItem[SELF <: Widget[SELF]] 
  extends Widget[SELF]
  with TextProperty[SELF]
  with FocusProperty[SELF]
{
  /** ссылка на главное меню */
  def menuBar:Option[MenuBar] = widgetPath.reverse.find( _.isInstanceOf[MenuBar] ).map( _.asInstanceOf[MenuBar] )

  /** Какая ширина отображаемого пункта меню */
  def renderableWidth:Int = text.value.length
    
  /** Следующие меню за текущим */
  def nextMenu:Option[MenuContainer|MenuAction] = {
    val me:Widget[SELF] = this.asInstanceOf[Widget[SELF]]
    LikeTreeOps(me)(Widget.likeTree).siblings.find { _ match {
      case _:MenuAction => true
      case _:MenuContainer => true
      case _ => false
    }}.map( _.asInstanceOf[MenuContainer|MenuAction] )
  }

  /** Предыдущее меню перед текущим */
  def prevMenu:Option[MenuContainer|MenuAction] = {
    val me:Widget[SELF] = this.asInstanceOf[Widget[SELF]]
    LikeTreeOps(me)(Widget.likeTree).siblings.reverse.find { _ match {
      case _:MenuAction => true
      case _:MenuContainer => true
      case _ => false
    }}.map( _.asInstanceOf[MenuContainer|MenuAction] )
  }

  /** Родительское меню */
  def upMenu:Option[MenuContainer] = {
    val me:Widget[SELF] = this.asInstanceOf[Widget[SELF]]
    me.widgetPath.reverse.drop(1).find ( w => w match {
      case m:MenuContainer => true
      case _ => false
    }).map( _.asInstanceOf[MenuContainer] )
  }

  protected def nestedMenuLevel:Int = widgetPath.reverse.filter( it => { 
    it match {
      case _:MenuItem[_] => true
      case _:MenuBar => true
      case _ => false
    }
  }).takeWhile( it => !(it.isInstanceOf[MenuBar]) ).size

  /** Расшифрока нажатий на пункты меню 
   *  - Next - переход к следующему пункту меню
   *  - Prev - переход к предыдущему пункту меню
   *  - GoSub - переход во вложенное меню
   *  - GoUp - переход к родительскому меню
   *  - Esc - выход их меню
   */
  enum MenuKey {
    case Next, Prev, GoSub, GoUp, Esc    
  }
  
  object MenuKey {
    /**
     * Определяет тип перехода к пунктам меню в зависимости от клавиши и текущего уровня вложенности меню
     */
    def what(ks:KeyStroke):Option[MenuKey] = {      
      val lvl = nestedMenuLevel-1
      ks.getKeyType match {
        case KeyType.ArrowRight if lvl==0 => Some(Next)
        case KeyType.ArrowLeft  if lvl==0 => Some(Prev)
        case KeyType.ArrowDown  if lvl==0 => Some(GoSub)
        case KeyType.ArrowDown  if lvl>0 => Some(Next)
        case KeyType.ArrowUp    if lvl>0 => Some(Prev)
        case KeyType.ArrowRight if lvl>0 => Some(GoSub)
        case KeyType.Escape => Some(Esc)
        case KeyType.Enter => Some(GoSub)
        case _:AnyRef => None
      }
    }
  }

  /**
   * Переход к следующему пункту меню
   * @return успешно или нет
   */
  protected def switchNextMenu():Boolean = {
    nextMenu match {
      case Some(nm) => nm.focus.request { _ =>
          nested.foreach { _.visible.value = false }
        }
        true
      case None => false
    }
  }
  
  /**
   * Переход к предыдущему пункту меню
   * @return успешно или нет
   */
  protected def switchPrevMenu():Boolean = {    
    prevMenu match {
      case Some(nm) => nm.focus.request { _ =>
          nested.foreach { _.visible.value = false }
        }
        true
      case None => 
        switchUpMenu()
    }
  }

  /**
   * Переход к родительскому пункту меню
   * @return успешно или нет
   */
  private def switchUpMenu():Boolean = {
    upMenu.map { menu => 
      menu.focus.request()
      true
    }.getOrElse( false )
  }

  /**
   * Добавляет подписчика на фокус
   * При получении фокуса от виджета который не входит в меню, уведомляет MenuBar куда вохвращать фокус
   */
  protected def menuItemInit():Unit = {
    focus.onGain(from => {
      from.foreach { wfrom => 
        menuBar.foreach { mb =>
          if( !wfrom.widgetPath.contains(mb) ){
            mb.acceptFocusFrom( wfrom )
          }
        }
      }
    })
  }
}
