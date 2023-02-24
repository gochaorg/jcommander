package xyz.cofe.files

class SymLinkTest extends munit.FunSuite:
  test("direct") {
    assert( SymLink.resolve("a", from => Right(None)) == Right("a") )
  }

  test("direct err") {
    assert( SymLink.resolve("a", from => Left("err")) == Left(SymLink.ResolveErr.ReadLink("err")) )
  }

  test("one hop") {
    assert( SymLink.resolve("a", {
      case "a" => Right(Some("b"))
      case _ => Right(None)
    }) == Right("b") )
  }

  test("two hop") {
    assert( SymLink.resolve("a", {
      case "a" => Right(Some("b"))
      case "b" => Right(Some("c"))
      case _ => Right(None)
    }) == Right("c") )
  }

  test("inner err") {
    assert( SymLink.resolve("a", {
      case "a" => Right(Some("b"))
      case "b" => Left("err")
      case _ => Right(None)
    }) == Left(SymLink.ResolveErr.ReadLink("err")) )
  }

  test("cycle err") {
    val res = SymLink.resolve("a", {
      case "a" => Right(Some("b"))
      case "b" => Right(Some("c"))
      case "c" => Right(Some("a"))
      case _ => Right(None)
    })
    assert(res.isLeft)
    assert(res.left.getOrElse(None).isInstanceOf[SymLink.ResolveErr.CycledLink[?,?]])
  }

