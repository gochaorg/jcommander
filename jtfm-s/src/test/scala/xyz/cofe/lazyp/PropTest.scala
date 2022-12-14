package xyz.cofe.lazyp

class PropTest extends munit.FunSuite:
  test("prop") {
    val pa = Prop.rw(0)
    val pb = Prop.rw(0)
    val pc = Prop.eval(pa,pb)( (a,b) => a + b )
    assert(pa.get == 0)
    assert(pb.get == 0)
    assert(pc.get == 0)
    pa.set(1)
    assert(pc.get == 1)
    pb.set(1)
    assert(pc.get == 2)
  }