package xyz.cofe.jtfm.gr

/** Тип расширения символа табцляции */
enum TabExpand:
  case OneSpace
  case TwoSpace
  case FourSpace
  case EightSpace
  case TwoAligned
  case FourAligned
  case EightAligned
  def apply(line:String):String =
    line.zip(0 until line.length).map { case(chr,idx) => 
      chr match {
        case '\t' => this match {
          case TabExpand.OneSpace => " "
          case TabExpand.TwoSpace => " "*2
          case TabExpand.FourSpace => " "*4
          case TabExpand.EightSpace => " "*8
          case TabExpand.TwoAligned => " "*(2-(idx%2))
          case TabExpand.FourAligned => " "*(4-(idx%4))
          case TabExpand.EightAligned => " "*(8-(idx%8))
        }
        case _ => chr.toString
      }
    }.mkString

/** Строка текстового блока */
case class TextLine(line:String,offset:Int=0):
  lazy val xFrom = offset
  lazy val xTo = offset+line.length

/** Текстовый блок */
case class TextBlock(lines:List[TextLine]):
  /** высота блока в строчках */
  lazy val height = lines.length

  /** границы по горизонтали */
  lazy val xBounds = {
    val _fMin:Option[Int] = None
    val _bnds = lines.foldLeft( (_fMin,_fMin,_fMin,_fMin) ){ case ( (fromMin,fromMax,toMin,toMax),line ) => {
      val f_min = fromMin match {
        case None => line.xFrom
        case Some(x) => line.xFrom min x
      }
      val f_max = fromMin match {
        case None => line.xFrom
        case Some(x) => line.xFrom max x
      }
      val t_min = fromMin match {
        case None => line.xTo
        case Some(x) => line.xTo min x
      }
      val t_max = fromMin match {
        case None => line.xTo
        case Some(x) => line.xTo max x
      }

      (Some(f_min), Some(f_max), Some(t_min), Some(t_max))
    }}
    new {
      val xFromMin = _bnds._1
      val xFromMax = _bnds._2
      val xToMin = _bnds._3
      val xToMax = _bnds._4
    }
  }

  /** минимальная/максимальная ширина строки */
  lazy val widthMinMax:Option[(Int,Int)] = {
    val x:Option[(Int,Int)] = None
    lines.foldLeft( x )( (a,b) => 
      val w = b.line.length
      a match {
        case Some((xmin,xmax)) =>
          Some( (xmin min w, xmax max w) )
        case None => 
          Some( (w,w) )
      }
    )
  }

  /** 
   * выравнивае по левому краю,
   * для всех строк offset присвается 0
   */
  def alignLeft:TextBlock = 
    TextBlock( lines.map( l => TextLine(l.line, 0) ) )

  /** выравнивание по правому краю, максимальная ширина берется от TextLine.line.length */
  def alignRight:TextBlock = 
    widthMinMax.map( (minWidth,maxWidth) => 
      alignRight(maxWidth)
    ).getOrElse( this )

  /** выравнивание по правому краю */
  def alignRight(maxWidth:Int):TextBlock =
    TextBlock( lines.map(line => 
      TextLine( line.line, maxWidth-line.line.length )
    ))

  def alignCenter(maxWidth:Int):TextBlock =
    TextBlock( lines.map( tline => 
      val w = tline.line.length + tline.offset
      if( w>=maxWidth ){
        tline
      }else{
        tline.copy(
          offset = (maxWidth - tline.line.length)/2
        )
      }
    ))
  
  def alignCenter:TextBlock = 
    widthMinMax.map( (minWidth,maxWidth) => 
      alignCenter(maxWidth)
    ).getOrElse( this )

  /** обрезка ширины блока слева до указанной ширины */
  def cropLeft( width:Int ):TextBlock =
    TextBlock( lines.map(line => {
      val w = line.line.length+line.offset
      if( w<=width ){
        line
      }else{
        val delta = w-width
        if( line.offset>=delta ){
          TextLine(line.line, line.offset-delta )
        }else{
          val w2 = line.line.length
          val csize = w2 - width
          if( csize>=0 ){
            TextLine(line.line.substring(csize),0)
          }else{
            line
          }
        }
      }
    }))

  /** обрезка ширины блока справа до указанной ширины */
  def cropRight( width:Int ):TextBlock =
    TextBlock( lines.map( sline => {
      val w = sline.line.length+sline.offset
      if( w<=width ){
        sline
      }else{
        val delta = w - width
        if( sline.line.length<=delta ){
          TextLine("",sline.offset)
        }else{
          TextLine(sline.line.substring( 0, sline.line.length-delta ),sline.offset)
        }
      }
    }))

  /** Расширение блока слева до указанной ширины */
  def expandLeft( width:Int ):TextBlock =
    TextBlock( lines.map( sline => {
      val w = sline.line.length+sline.offset
      if( w>=width ){
        sline
      }else{
        sline.copy(offset = sline.offset+(width-w))
      }
    }))

object TextBlock:
  def apply(str:String,tab:TabExpand=TabExpand.FourAligned):TextBlock =
    TextBlock(str.split("\\r?\\n").toList.map { line =>
      TextLine(tab(line))
    })