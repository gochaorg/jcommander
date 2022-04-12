package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.{EvalProperty, Property}

/**
 * Свойство - наличие фокуса в виджете
 */
trait FocusProperty[SELF : RepaitRequest]
(
  /** перерисовывать при получении фокуса */
  val repait:Boolean=false,

  /** максимальная длина истории смены фокуса */
  val historyMaxSize:Int=0
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

    /**
     * история получения/потери фокуса
     */
    object history extends Iterable[HistoryAction] {
      override def iterator: Iterator[HistoryAction] = _history.iterator
      def lastGain:Option[Widget[_]] = _history.find { it => it match {
        case HistoryAction.Gained( g ) if g.isDefined => false
        case _ => false
      }}.map( _.widget.get )
      def lastLost:Option[Widget[_]] = _history.find { it => it match {
        case HistoryAction.Lost( g ) if g.isDefined => false
        case _ => false
      }}.map( _.widget.get )
    }

    private var onGainListeners = List[Option[Widget[_]]=>Unit]()
  
    def onGain( from:Option[Widget[_]] ):Unit = {
      if( historyMaxSize>0 ){
        _history = HistoryAction.Gained(from) :: _history
        if( _history.length>historyMaxSize ){
          _history = _history.take(historyMaxSize)
        }
      }
      recompute()
      onGainListeners.foreach { _(from) }
    }
    def onGain( l:Option[Widget[_]]=>Unit ):Unit = {
      onGainListeners = l :: onGainListeners
    }

    private var onLostListeners = List[Option[Widget[_]]=>Unit]()

    def onLost( newOwner:Option[Widget[_]] ):Unit = {
      if( historyMaxSize>0 ){
        _history = HistoryAction.Lost(newOwner) :: _history
        if( _history.length>historyMaxSize ){
          _history = _history.take(historyMaxSize)
        }
      }
      recompute()
      onLostListeners.foreach { _(newOwner) }
    }
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
  enum HistoryAction( val widget:Option[Widget[_]] ){
    case Gained( w:Option[Widget[_]] ) extends HistoryAction( w )
    case Lost( w:Option[Widget[_]] ) extends HistoryAction( w )
  }
}