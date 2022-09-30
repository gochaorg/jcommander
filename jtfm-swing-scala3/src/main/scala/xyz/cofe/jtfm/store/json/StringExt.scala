package xyz.cofe.jtfm.store.json

extension ( str:String )
  def decodeLitteral:String =
    def unescape( strContent:String ):String = {
      val sb = new StringBuilder()
      var skip = 0
      for 
        idx <- (0 until strContent.length)
        ch0 = strContent.charAt(idx)
        ch1opt = 
          if idx<(strContent.length-1) then 
            Some(strContent.charAt(idx+1)) 
          else 
            None
      do
        if skip>0 then skip -= 1 
        else
          (ch0, ch1opt) match
            case ('\\', Some('\n')) => sb += '\n'; skip=1
            case ('\\', Some('\r')) => sb += '\r'; skip=1
            case ('\\', Some('\t')) => sb += '\t'; skip=1
            case ('\\', Some(ch1)) => sb += ch1; skip=1
            case _ => sb += ch0
      sb.toString()
    }
    if str.startsWith("'") && str.endsWith("'") then
      unescape(str.substring(1,str.length()-1))
    else if str.startsWith("\"") && str.endsWith("\"") then
      unescape(str.substring(1,str.length()-1))
    else
      throw new IllegalArgumentException(s"can't decode js string litteral: $str")
  def encodeLitteral:String =
    str match
    case null => "null"
    case _ => 
      "\"" + str.flatMap( chr => chr match
        case '"'  => "\\\""
        case '\\' => "\\\\"
        case '\n' => "\\n"
        case '\r' => "\\r"
        case '\t' => "\\t"
        case _ => chr.toString
      ) + "\""

