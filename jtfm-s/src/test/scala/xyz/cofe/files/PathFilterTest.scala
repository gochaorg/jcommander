package xyz.cofe.files

import java.nio.file.Path

class PathFilterTest extends munit.FunSuite:
  test("wildcard") {
    assert(  PathFilter.Wildcard("abc?1*")(Path.of("abce1")) )
    assert(  PathFilter.Wildcard("abc?1*")(Path.of("abcf1")) )
    assert( !PathFilter.Wildcard("abc?1*")(Path.of("abcf2")) )
    assert(  PathFilter.Wildcard("abc?1*")(Path.of("abcf1xxx")) )
  }
