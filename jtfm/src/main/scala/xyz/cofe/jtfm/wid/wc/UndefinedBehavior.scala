package xyz.cofe.jtfm.wid.wc

import scala.collection.immutable.SortedMap

/**
 * Поведение при обработки ошибок виджетов/заданий и т.д.
 */
sealed trait UndefinedBehavior {
  def apply(state:State, err:Throwable):State
}

object UndefinedBehavior {

  /** Пропускать все ошибки */
  case class SkipAll() extends UndefinedBehavior {
    def apply(state:State, err:Throwable):State = {
      state
    }
  }

  /** Не пропускать ни какую ошибку, сразу останавлливаться */
  case class PanicFirst() extends UndefinedBehavior {
    def apply(state:State, err:Throwable):State = {
      state.finish()
    }
  }

  /** Ограничить максимальной частотой ошибок, за определенный период времени */
  case class TimeRateLimit( 
    val timeMillisec:Long, 
    val maxErrCount:Long 
  ) extends UndefinedBehavior {
    var counter:SortedMap[Long,Long] = SortedMap()
    def apply( state:State, err:Throwable ):State = {
      val now = System.currentTimeMillis
      
      val cnt0 = counter.get(now).getOrElse(0.toLong)
      counter = counter + (now -> (cnt0+1.toLong))

      if( counter.rangeFrom( now - timeMillisec ).values.sum > maxErrCount ){
        state.finish()
      }else{
        state
      }
    }
  }

  /** Пересылает сообщение о ошибке и передает управление дальше */
  case class ErrorHandler( 
    next:UndefinedBehavior,  
    handler: Throwable=>Unit
  ) extends UndefinedBehavior {
    def apply( state:State, err:Throwable ):State = {
      handler(err)
      next(state, err)
    }
  }
  
}