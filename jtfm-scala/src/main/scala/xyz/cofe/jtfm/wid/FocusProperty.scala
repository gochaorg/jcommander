package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.{EvalProperty, Property}
import scala.ref.WeakReference

/**
 * Свойство - наличие фокуса в виджете
 */
trait FocusProperty[SELF : RepaitRequest]
(
  /** перерисовывать при получении фокуса */
  private val repait:Boolean=false,

  /** максимальная длина истории смены фокуса */
  private val historyMaxSize:Int=0
) {
  self: Widget[_] =>
  
  /** 
   * value = true | если содержит фокус и является владельцем фокуса
   */
  class FocusProp extends EvalProperty[Boolean, SELF](
    compute = ()=>{
      WidgetCycle.tryGet.flatMap( _.workState ).flatMap( _.inputProcess.focusOwner.map( w => w==self ) ).getOrElse( false )
    },
    initial = Some(false)
  ) {
    import FocusProperty._
    private var _history:List[HistoryAction] = List()
    private var _historyNonChild:List[HistoryAction] = List()

    /**
     * история получения/потери фокуса
     */
    object history extends Iterable[HistoryAction] {
      override def iterator: Iterator[HistoryAction] = _history.iterator

      /** От кого получил фокус */
      def lastGain:Option[Widget[_]] = _history.find { it => it match {
        case HistoryAction.Gained( g ) if g.isDefined => true
        case _ => false
      }}.flatMap( _.widget.get.get )

      /** Кто захватил фокус */
      def lastLost:Option[Widget[_]] = _history.find { it => it match {
        case HistoryAction.Lost( g ) if g.isDefined => true
        case _ => false
      }}.flatMap( _.widget.get.get )

      def lastGainNonChild:Option[Widget[_]] = _historyNonChild.find { it => it match {
        case HistoryAction.Gained( g ) if g.isDefined => true
        case _ => false
      }}.flatMap( _.widget.get.get )
    }

    private var onGainListeners = List[Option[Widget[_]]=>Unit]()
  
    /** 
     * Вызывается при получении фокуса
     * @param from кто ранее владел фокусом
     */
    def onGain( from:Option[Widget[_]] ):Unit = {
      if( historyMaxSize>0 ){
        _history = HistoryAction.Gained(from.map(w => Some(WeakReference(w))).getOrElse(None) ) :: _history
        if( _history.length>historyMaxSize ){
          _history = _history.take(historyMaxSize)
        }
      }

      if( historyMaxSize>0 ){
        from match {
          case Some(widFrom) =>
            if( !widFrom.widgetPath.reverse.exists( w => w==self ) ){
              _historyNonChild = HistoryAction.Gained(Some(WeakReference(widFrom))) :: _historyNonChild
            }
          case _ =>
        }
        if( _historyNonChild.length>historyMaxSize ){
          _historyNonChild = _historyNonChild.take(historyMaxSize)
        }
      }

      recompute()
      onGainListeners.foreach { _(from) }
    }

    /** 
     * Добавляет подписчика на событие получения фокуса 
     * @param l:Option[Widget[_]]=>Unit - функция(лямбда) где параметр - кто ранее владел фокусом
     */
    def onGain( l:Option[Widget[_]]=>Unit ):Unit = {
      onGainListeners = l :: onGainListeners
    }

    private var onLostListeners = List[Option[Widget[_]]=>Unit]()

    /** 
     * Вызывается при потере фокуса
     * @param newOwner кто сейчас фладеет фокусом
     */
    def onLost( newOwner:Option[Widget[_]] ):Unit = {
      if( historyMaxSize>0 ){
        _history = HistoryAction.Lost( newOwner.map( wid => Some(WeakReference(wid)) ).getOrElse(None) ) :: _history
        if( _history.length>historyMaxSize ){
          _history = _history.take(historyMaxSize)
        }
      }

      if( historyMaxSize>0 ){
        newOwner match {
          case Some(wid) =>
            if( !wid.widgetPath.reverse.exists( w => w==self ) ){
              _historyNonChild = HistoryAction.Lost(Some(WeakReference(wid))) :: _historyNonChild
            }
          case _ =>
        }
        if( _historyNonChild.length>historyMaxSize ){
          _historyNonChild = _historyNonChild.take(historyMaxSize)
        }
      }

      recompute()
      onLostListeners.foreach { _(newOwner) }
    }

    /** 
     * Добавляет подписчика на событие потери фокуса 
     * @param l:Option[Widget[_]]=>Unit - функция(лямбда) где параметр - кто сейчас фладеет фокусом
     */
    def onLost( l:Option[Widget[_]]=>Unit ):Unit = {
      onLostListeners = l :: onLostListeners
    }
    
    /**
     * true - если виджет или его дочерний виджет содержит фокус
     */
    def contains:Boolean = {
      val focusPath = WidgetCycle.tryGet.flatMap( _.workState ).flatMap( _.inputProcess.focusOwner ).map( _.widgetPath )
      focusPath.map( _.contains(FocusProperty.this) ).getOrElse(false)
    }
    
    /** Запрос на получение фокуса ввода */
    def request():Unit = {
      WidgetCycle.tryGet.flatMap( _.workState ).map( _.inputProcess ).foreach( inp => {
        inp.focusOwner match {
          case Some(fo) =>
            if ( fo!=self ) {
              inp.focusRequest(self)
            }
          case _ =>
            inp.focusRequest(self)
        }
      })
    }

    /**
     * Запрос на получение фокуса ввода
     * @param accepted - fn(lastFocusOwner) вызывается при получении фокуса
     */
    def request[Z]( accepted:Option[Widget[_]]=>Z ):Unit = {
      WidgetCycle.tryGet.flatMap( _.workState ).map( _.inputProcess ).foreach( inp => {
        inp.focusOwner match {
          case Some(fo) =>
            if ( fo!=self ) {
              inp.focusRequest(self) match {
                case Right(swt) =>
                  accepted(swt)
                case Left(_) =>
              }
            }
          case _ =>
            inp.focusRequest(self) match {
              case Right(swt) =>
                accepted(swt)
              case Left(_) =>
            }
        }
      })
    }
  }
  
  /** 
   * focus.value = true | если содержит фокус и является владельцем фокуса <br>
   * focus.contains = true - если виджет или его дочерний виджет содержит фокус
   */
  lazy val focus : FocusProp = {
    if (repait) {
      val prop = FocusProp()
      prop.observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(self.asInstanceOf[SELF])
      })
      prop
    } else {
      FocusProp()
    }
  }
}

object FocusProperty {
  enum HistoryAction( val widget:Option[WeakReference[Widget[_]]] ){
    case Gained( w:Option[WeakReference[Widget[_]]] ) extends HistoryAction( w )
    case Lost( w:Option[WeakReference[Widget[_]]] ) extends HistoryAction( w )
  }
}