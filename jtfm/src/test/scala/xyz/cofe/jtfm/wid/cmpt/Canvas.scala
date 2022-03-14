package xyz.cofe.jtfm.wid.cmpt

class Canvas( val width:Int, val height:Int ) {
  require(width>0)
  require(height>0)
  private var chars: Array[Array[Char]] = Array.ofDim[Char](height,width)
  for(
    y <- (0 until height);
    x <- (0 until width)
  ) chars(y)(x) = ' ';
  
  def put( x:Int, y:Int, c:Char ):Unit = {
    require(x>=0 && x<width)
    require(y>=0 && y<height)
    chars(y)(x) = c
  }
  
  override def toString(): String ={
    chars.foldLeft("")( (a,b) => {
      val line = b.map( c=>""+c ).foldLeft( "" )( (x,y)=>x+y )
      if( a.length>0 ){
        a + "\n" + line
      }else{
        line
      }
    })
  }
}
