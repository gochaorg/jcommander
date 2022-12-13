package xyz.cofe.term.buff

enum ScreenBufferError:
  case AgrumentOutRange[A](argument:String,value:A,min:A,max:A)