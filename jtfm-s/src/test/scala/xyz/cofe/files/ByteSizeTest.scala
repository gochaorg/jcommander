package xyz.cofe.files

class ByteSizeTest extends munit.FunSuite:
  test("parts") {
    val kSize = ByteSize(1024*5+512)
    assert(kSize.kbytePart == 5)
    assert(kSize.bytePart == 512)

    val mSize = ByteSize(1024*1024*6+1024*5+512)
    assert(mSize.bytePart == 512)
    assert(mSize.kbytePart == 5)
    assert(mSize.mbytePart == 6)

    val gSize = ByteSize(1024L*1024L*1024L*7L + 1024L*1024L*6L + 1024L*5L + 512)
    assert(gSize.bytePart == 512)
    assert(gSize.kbytePart == 5)
    assert(gSize.mbytePart == 6)
    assert(gSize.gbytePart == 7)
  }

  test("size suff") {
    assert(SizeSuff.parse("12",0) == Some((SizeSuff.Byte(12), 2)))
    assert(SizeSuff.parse("12b",0) == Some((SizeSuff.Byte(12), 3)))
    assert(SizeSuff.parse("12B",0) == Some((SizeSuff.Byte(12), 3)))
    assert(SizeSuff.parse("12 b",0) == Some((SizeSuff.Byte(12), 4)))
    assert(SizeSuff.parse("12k",0) == Some((SizeSuff.KByte(12), 3)))
    assert(SizeSuff.parse("12 k",0) == Some((SizeSuff.KByte(12), 4)))
    assert(SizeSuff.parse("12kb",0) == Some((SizeSuff.KByte(12), 4)))
    assert(SizeSuff.parse("12kb ",0) == Some((SizeSuff.KByte(12), 4)))
    assert(SizeSuff.parse("12Kb ",0) == Some((SizeSuff.KByte(12), 4)))
  }

  test("sizes parse") {
    println(SizeSuff.parseSizes("12m 5k 34"))
  }

  test("hs") {
    val s = ByteSize(1024L*1024L*2 + 1024L*256)
    println(s.humanReadable)
  }
