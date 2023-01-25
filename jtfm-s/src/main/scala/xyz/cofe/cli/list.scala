package xyz.cofe.cli

object list:
  given commaSeparated[A:FromCmdLine]:FromCmdLine[List[A]] with
    def parse(args:List[String]):Either[String,(List[A],List[String])] = 
      if args.isEmpty 
      then Left("no data")      
      else 
        val prse : FromCmdLine[A] = summon[FromCmdLine[A]]
        val strs = args.head.split(",").toList
        val items = strs.map( str => prse.parse(List(str)) )
        val unpacked = items.foldLeft( Right(List.empty):Either[String,List[A]] ){ case (res,itm) => 
          res.flatMap { lst => 
            itm.map { itm => 
              lst :+ itm._1
            }
          }
        }
        unpacked.map(lst => (lst,args.tail))
