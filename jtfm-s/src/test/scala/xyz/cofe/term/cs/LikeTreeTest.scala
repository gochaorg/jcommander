package xyz.cofe.term.cs

class LikeTreeTest extends munit.FunSuite:
  enum Node(val value:String):
    case Leaf(override val value:String) extends Node(value)
    case Branch(override val value:String, left:Node, right:Node) extends Node(value)

  object Node:
    given LikeTree[Node] with
      def nodes(n:Node):List[Node] =
        n match
          case Branch(value, left, right) => List(left,right)
          case Leaf(value) => List()

  import Node._

  /*
                          root (0)
              l(1)                         r(8)
       ll(2)         lr(5)         rl(9)          rr(12)
  lll(3) llr(4) lrl(6) lrr(7) rll(10) rlr(11) rrl(13) rrr(14)

  /////////////////////////
  deep order

  0  root
  1  root/l
  2  root/l/ll
  3  root/l/ll/lll
  4  root/l/ll/llr
  5  root/l/lr
  6  root/l/lr/lrl
  7  root/l/lr/lrr
  8  root/r
  9  root/r/rl
  10 root/r/rl/rll
  11 root/r/rl/rlr
  12 root/r/rr
  13 root/r/rr/rrl
  14 root/r/rr/rrr



  forward

  case root -> l  | root.fistLeft
  case l -> ll    | l.firstLeft
  case ll -> lll  | ll.firstLeft
  case lll -> llr | llr.rightSib
  case llr -> lr  | lr.parent.rightSib
  case lr -> lrl  | lr.firstLeft
  case lrl -> lrr | lrl.rightSib
  case lrr -> r   | lrr.parent.parent(l).rightSib - exists r
  case r -> rl    | r.firstLeft
  case rl -> rll  | rl.firstLeft
  case rll -> rlr | rl.rightSib
  case rlr -> rr  | rlr.parent.rightSib
  case rr -> rrl  | rr.firstLeft
  case rrl -> rrr | rrl.rightSib 


  backward

  case rrr -> rrl | rr.leftSib
  case rrl -> rr  | rrl.parent , rr.leftSib not exists
  case rr -> rlr  | rr.leftSib.deeperRight
  case rlr -> rll | rr.leftSib
  case rll -> rl  | rll.parent
  case rl -> r    | rl.parent , rl.leftSib not exists
  case r ->  lrr  | r.leftSib.deeperRight 
  case lrr -> lrl | lrl.leftSib
  case lrl -> lr  | lrl.parent
  case lr -> llr  | lr.leftSib.deeperRight
  case llr -> lll | llr.leftSib
  case lll -> ll  | lll.parent
  case ll -> l    | ll.parent
  case l -> root  | l.parent

  */
  val tree = Branch(
    "root", 
    Branch("l", 
      Branch("ll", Leaf("lll"), Leaf("llr")),
      Branch("lr", Leaf("lrl"), Leaf("lrr"))
    ),
    Branch("r",
      Branch("rl", Leaf("rll"), Leaf("rlr")),
      Branch("rr", Leaf("rrl"), Leaf("rrr"))
    )
  )

  test("likeTree") {
    assert( tree.nodesCount()==2 )
    assert( tree.node(0).map(n => n.value) == Some("l") )
    assert( tree.node(1).map(n => n.value) == Some("r") )
    assert( tree.node(2).map(n => n.value) == None )
  }

  test("walk") {
    tree.walk.path.foreach(path => {
      println( path.listToLeaf.map(_.value).mkString("/") )
    })
  }

  test("leftSib") {
    val lrPath = tree.walk.path.find { _.node.value == "lr" }.get
    val llPath = tree.walk.path.find { _.node.value == "ll" }.get
    assert( lrPath.leftSib == Some(llPath) )
  }

  test("rightSib") {
    val lrPath = tree.walk.path.find { _.node.value == "lr" }.get
    val llPath = tree.walk.path.find { _.node.value == "ll" }.get
    assert( llPath.rightSib == Some(lrPath) )
  }

  test("firstLeftChild") {
    val lPath = tree.walk.path.find { _.node.value == "l" }.get
    val llPath = tree.walk.path.find { _.node.value == "ll" }.get
    assert( lPath.firstLeftChild == Some(llPath) )
  }

  test("righter") {
    val lrlPath = tree.walk.path.find { _.node.value == "lrl" }.get
    val lrrPath = tree.walk.path.find { _.node.value == "lrr" }.get
    val rPath = tree.walk.path.find { _.node.value == "r" }.get

    assert(lrlPath.righter == Some(lrrPath))
    assert(lrrPath.righter == Some(rPath))
  }

  test("nextByDeep") {
    val l   = tree.walk.path.find { _.node.value == "l" }.get
    val ll  = tree.walk.path.find { _.node.value == "ll" }.get
    val lll = tree.walk.path.find { _.node.value == "lll" }.get
    val llr = tree.walk.path.find { _.node.value == "llr" }.get
    val lr  = tree.walk.path.find { _.node.value == "lr" }.get
    val lrl = tree.walk.path.find { _.node.value == "lrl" }.get
    val lrr = tree.walk.path.find { _.node.value == "lrr" }.get
    val r   = tree.walk.path.find { _.node.value == "r" }.get
    val rl  = tree.walk.path.find { _.node.value == "rl" }.get
    val rll = tree.walk.path.find { _.node.value == "rll" }.get
    val rlr = tree.walk.path.find { _.node.value == "rlr" }.get
    val rr  = tree.walk.path.find { _.node.value == "rr" }.get
    val rrl = tree.walk.path.find { _.node.value == "rrl" }.get
    val rrr = tree.walk.path.find { _.node.value == "rrr" }.get

    assert( l.nextByDeep   == Some(ll),  "l -> ll" )
    assert( ll.nextByDeep  == Some(lll), "ll -> ll" )
    assert( lll.nextByDeep == Some(llr), "lll -> llr" )
    assert( llr.nextByDeep == Some(lr),  "llr -> lr" )
    assert( lr.nextByDeep  == Some(lrl), "lr -> lrl" )
    assert( lrl.nextByDeep == Some(lrr), "lrl -> lrr" )
    assert( lrr.nextByDeep == Some(r),   "lrr -> r" )
    assert( r.nextByDeep   == Some(rl),  "r -> rl" )
    assert( rl.nextByDeep  == Some(rll), "rl -> rll" )
    assert( rll.nextByDeep == Some(rlr))
    assert( rlr.nextByDeep == Some(rr))
    assert( rr.nextByDeep  == Some(rrl))
    assert( rrl.nextByDeep == Some(rrr))
    assert( rrr.nextByDeep == None)
  }