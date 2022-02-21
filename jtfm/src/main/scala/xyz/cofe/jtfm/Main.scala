package xyz.cofe.jtfm

@main def hello(args: String*): Unit =
  println("Hello world!")
  println(s"args(${args.length})")
  args.zip(0 until args.length).foreach{ (arg,argi) => {
    println(s"arg[$argi] = $arg")
  }}

