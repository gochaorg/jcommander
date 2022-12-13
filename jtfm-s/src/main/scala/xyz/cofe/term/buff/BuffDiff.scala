package xyz.cofe.term.buff

import xyz.cofe.term.common.Position

object BuffDiff:
  def diff(left:ScreenBuffer, right:ScreenBuffer):Seq[CharDifference] = 
    val diffs = (0 until (left.height min right.height)).flatMap { y => diffLine(y, left, right) }.toList

    val extra = {
      if( left.height < right.height ) {
        (left.height until right.height).map { y => 
          (0 until right.width).map { x => 
            right.get(x,y).map { chr =>
              insertedChar(x,y,chr)
            }
          }
        }.toList
      }else if( left.height > right.height ){
        (right.height until left.height).map { y => 
          (0 until right.width).map { x => 
            left.get(x,y).map { chr =>
              deletedChar(x,y,chr)
            }
          }
        }.toList
      }else{
        List()
      }
    }.flatten.flatMap {
      case Some(value) => List(value)
      case None => List()
    }

    diffs ++ extra

  def diffLine(y:Int, left:ScreenBuffer, right:ScreenBuffer):Seq[CharDifference] = 
    val diffs = (0 until (left.width min right.width)).map { x => 
      left.get(x,y).flatMap { lchr => 
        right.get(x,y).map { rchr => 
          diffChar(x,y,lchr,rchr)
        }
      }
    }.toList.map { 
      case Some(value) => value.toList
      case None => List()
    }.flatten

    val extra = {
      if left.width < right.width 
        then (left.width until right.width).map { x => right.get(x,y).map { chr => List(insertedChar(x,y,chr)) }.getOrElse(List()) }.toList
        else if left.width > right.width
          then (right.width until left.width).map { x => left.get(x,y).map { chr => List(deletedChar(x,y,chr)) }.getOrElse(List()) }.toList
          else List()
    }.flatten

    diffs ++ extra

  def diffChar( x:Int, y:Int, left:ScreenChar, right:ScreenChar ):Seq[CharDifference] = 
    if left!=right then
      List(CharDifference(
        pos = Position(x,y),
        left  = Some(left),
        right = Some(right),
      ))
    else
      List()

  def insertedChar(x:Int,y:Int,char:ScreenChar):CharDifference = 
    CharDifference(
      pos = Position(x,y),
      left  = None,
      right = Some(char),
    )

  def deletedChar(x:Int,y:Int,char:ScreenChar):CharDifference =
    CharDifference(
      pos = Position(x,y),
      left  = Some(char),
      right = None,
    )
