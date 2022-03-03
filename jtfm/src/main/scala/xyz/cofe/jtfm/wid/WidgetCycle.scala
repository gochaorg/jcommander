package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.terminal.Terminal

/**
 * Цикл управления событиями ввода и рендера.
 * 
 * <p>
 * 1 поток = 1 цикл обработки
 */
class WidgetCycle( 
    private val terminal: Terminal 
) {
  
}

object WidgetCycle {
    private val currentCycle : InheritableThreadLocal[WidgetCycle] =
        new InheritableThreadLocal()

    def apply( term: Terminal ):Either[WidgetCycle,Error] = {
        val wc0 = currentCycle.get
        if( wc0==null ){
            val wc1 = new WidgetCycle(term)
            currentCycle.set(wc1)
            Left(wc1)
        }else{
            if( wc0.terminal==term ){
                Left(wc0)
            }else{
                Right(new Error("has already created WidgetCycle for different Terminal"))
            }
        }
    }

    def tryGet:Option[WidgetCycle] = {
        val x = currentCycle.get
        if( x!=null ) Some(x) else None
    }
}