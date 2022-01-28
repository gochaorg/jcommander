package xyz.cofe.jtfm;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import xyz.cofe.fn.Tuple2;

import java.util.*;

/**
 * Сочетание клавиш
 *
 * <pre>
 * KeyShortcut ::= { modifier '+' } char_key
 * modifier    ::= shift_mod | alt_mod | control_mod
 * shift_mod   ::= 'shift' | 'shft'
 * alt_mod     ::= 'alt' | 'a'
 * control_mod ::= 'control' | 'ctrl' | 'c'
 * char_key    ::= unicode | alphabet  | name
 * alphabet    ::= 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9'
 * unicode     ::= 'u' hex hex hex hex
 * hex         ::= '0' .. '9' | 'a' .. 'f' | 'A' .. 'F'
 * name        ::= 'space'
 *               | 'esc' | 'escape'
 *               | 'backspace' | 'bs'
 *               | 'enter' | 'return'
 *               | 'tab'
 *               | 's-tab'
 *               | 'up'
 *               | 'down'
 *               | 'left'
 *               | 'right'
 *               | 'insert'
 *               | 'del'
 *               | 'home'
 *               | 'end'
 *               | 'pageup'
 *               | 'pagedown'
 *               | 'f1' .. 'f19'
 *               | 'plus'
 *               | 'minus'
 *               | '\t'
 *               | '\r'
 *               | '\n'
 * </pre>
 */
public class KeyShortcut {
    public static String toString( KeyStroke ks ){
        if( ks==null )throw new IllegalArgumentException( "ks==null" );
        StringBuilder sb = new StringBuilder();
        if( ks.isCtrlDown() )sb.append("ctrl+");
        if( ks.isAltDown() )sb.append("alt+");
        if( ks.isShiftDown() )sb.append("shift+");

        if( ks.getKeyType()==KeyType.Character ){
            var c_ = ks.getCharacter();
            if( c_==null )throw new IllegalArgumentException("ks.getCharacter()==null");

            var c = (char)c_;
            if( Character.isLetterOrDigit(c) ){
                sb.append(c);
            }else if( c=='+' ){ sb.append("plus");
            }else if( c=='-' ){ sb.append("minus");
            }else if( c==' ' ){ sb.append("space");
            }else if( c=='\t' ){ sb.append("\\t");
            }else if( c=='\r' ){ sb.append("\\r");
            }else if( c=='\n' ){ sb.append("\\n");
            }else{
                sb.append("u");
                int code = c;
                if( code<0 )throw new IllegalArgumentException("char code < 0");
                if( code<0x10 )sb.append("000").append(Integer.toHexString(code));
                if( code<0x100 )sb.append("00").append(Integer.toHexString(code));
                if( code<0x1000 )sb.append("0").append(Integer.toHexString(code));
                sb.append(Integer.toHexString(code));
            }
        }else if( ks.getKeyType()==null ){
            throw new IllegalArgumentException("ks.getKeyType()==null");
        }else{
            switch( ks.getKeyType() ){
                case EOF:
                case MouseEvent:
                case Unknown:
                case CursorLocation: sb.append(ks.getKeyType().name()); break;

                case Enter: case PageDown: case PageUp:
                case Insert: case Delete: case Home: case End:
                case Tab:

                case F1: case F2: case F3: case F4: case F5:
                case F6: case F7: case F8: case F9: case F10:
                case F11: case F12: case F13: case F14: case F15:
                case F16: case F17: case F18: case F19:
                    sb.append(ks.getKeyType().name().toLowerCase());
                    break;

                case Escape: sb.append("esc"); break;
                case Backspace: sb.append("bs"); break;
                case ArrowLeft: sb.append("left"); break;
                case ArrowRight: sb.append("right"); break;
                case ArrowUp: sb.append("up"); break;
                case ArrowDown: sb.append("down"); break;
                case ReverseTab: sb.append("s-tab"); break;
            }
        }
        return sb.toString();
    }
    private static Optional<Tuple2<KeyType,Character>> namedKeyOrChar(String str){
        if( str==null )throw new IllegalArgumentException( "str==null" );
        switch( str.toLowerCase() ){
            case "up": return Optional.of( Tuple2.of(KeyType.ArrowUp,null) );
            case "down": return Optional.of( Tuple2.of(KeyType.ArrowDown,null) );
            case "left": return Optional.of( Tuple2.of(KeyType.ArrowLeft,null) );
            case "right": return Optional.of( Tuple2.of(KeyType.ArrowRight,null) );
            case "insert": return Optional.of( Tuple2.of(KeyType.Insert,null) );
            case "del": return Optional.of( Tuple2.of(KeyType.Delete,null) );
            case "home": return Optional.of( Tuple2.of(KeyType.Home,null) );
            case "end": return Optional.of( Tuple2.of(KeyType.End,null) );
            case "esc": case "escape": return Optional.of( Tuple2.of(KeyType.Escape,null) );
            case "bs": case "backspace": return Optional.of( Tuple2.of(KeyType.Backspace,null) );
            case "enter": case "return": return Optional.of( Tuple2.of(KeyType.Enter,null) );
            case "tab": return Optional.of( Tuple2.of(KeyType.Tab,null) );
            case "s-tab": return Optional.of( Tuple2.of(KeyType.ReverseTab,null) );
            case "pageup": return Optional.of( Tuple2.of(KeyType.PageUp,null) );
            case "pagedown": return Optional.of( Tuple2.of(KeyType.PageDown,null) );

            case "f1": return Optional.of( Tuple2.of(KeyType.F1,null) );
            case "f2": return Optional.of( Tuple2.of(KeyType.F2,null) );
            case "f3": return Optional.of( Tuple2.of(KeyType.F3,null) );
            case "f4": return Optional.of( Tuple2.of(KeyType.F4,null) );
            case "f5": return Optional.of( Tuple2.of(KeyType.F5,null) );
            case "f6": return Optional.of( Tuple2.of(KeyType.F6,null) );
            case "f7": return Optional.of( Tuple2.of(KeyType.F7,null) );
            case "f8": return Optional.of( Tuple2.of(KeyType.F8,null) );
            case "f9": return Optional.of( Tuple2.of(KeyType.F9,null) );
            case "f10": return Optional.of( Tuple2.of(KeyType.F10,null) );
            case "f11": return Optional.of( Tuple2.of(KeyType.F11,null) );
            case "f12": return Optional.of( Tuple2.of(KeyType.F12,null) );
            case "f13": return Optional.of( Tuple2.of(KeyType.F13,null) );
            case "f14": return Optional.of( Tuple2.of(KeyType.F14,null) );
            case "f15": return Optional.of( Tuple2.of(KeyType.F15,null) );
            case "f16": return Optional.of( Tuple2.of(KeyType.F16,null) );
            case "f17": return Optional.of( Tuple2.of(KeyType.F17,null) );
            case "f18": return Optional.of( Tuple2.of(KeyType.F18,null) );
            case "f19": return Optional.of( Tuple2.of(KeyType.F19,null) );

            case "\\t": return Optional.of( Tuple2.of(null,'\t') );
            case "\\r": return Optional.of( Tuple2.of(null,'\r') );
            case "\\n": return Optional.of( Tuple2.of(null,'\n') );
            case "\\b": return Optional.of( Tuple2.of(null,'\b') );
            case "space": return Optional.of( Tuple2.of(null,' ') );
            case "plus": return Optional.of( Tuple2.of(null,'+') );
            case "minus": return Optional.of( Tuple2.of(null,'-') );
        }
        try{
            return Optional.of( Tuple2.of(KeyType.valueOf(str), null));
        } catch( IllegalArgumentException ex ){
            return Optional.empty();
        }
    }

    private static final List<String> mod_ctrl_name = List.of("ctrl", "c", "control" );
    private static final List<String> mod_alt_name = List.of("alt", "a");
    private static final List<String> mod_shift_name = List.of("shift", "shft", "s");
    private static final List<String> mod_name;

    static {
        var mod_name_1 = new ArrayList<String>();
        mod_name_1.addAll(mod_alt_name);
        mod_name_1.addAll(mod_ctrl_name);
        mod_name_1.addAll(mod_shift_name);
        mod_name = Collections.unmodifiableList(mod_name_1);
    }

    private static Optional<Integer> fromUnicode(String str){
        if( str==null )throw new IllegalArgumentException( "str==null" );
        if( str.length()<5 )return Optional.empty();
        if( !str.startsWith("u") )return Optional.empty();
        str = str.substring(1);
        try{
            var hex = Integer.parseInt(str, 16);
            return Optional.of(hex);
        } catch( NumberFormatException e ){
            return Optional.empty();
        }
    }
    public static KeyStroke from( String str ){
        if( str==null )throw new IllegalArgumentException( "str==null" );

        var state = 0;
        boolean mod_ctrl = false;
        boolean mod_shift = false;
        boolean mod_alt = false;

        KeyType k_type = null;
        Character chr = null;

        var sb = new StringBuilder();
        for( int ci=0; ci<str.length(); ci++ ){
            var c0 = str.charAt(ci);
            switch( state ){
                case 0:
                    if( Character.isWhitespace(c0) ) continue;
                    if( c0=='+' ){
                        if( sb.length()<1 ){
                            throw new IllegalArgumentException("modifier not specified");
                        }else if( mod_name.contains(sb.toString().toLowerCase()) ){
                            if( mod_ctrl_name.contains(sb.toString().toLowerCase()) )mod_ctrl = true;
                            if( mod_alt_name.contains(sb.toString().toLowerCase()) )mod_alt = true;
                            if( mod_shift_name.contains(sb.toString().toLowerCase()) )mod_shift = true;
                        }else{
                            throw new IllegalArgumentException("undefined modifier \""+sb.toString()+"\"");
                        }
                        sb.setLength(0);
                    }else{
                        sb.append(c0);
                    }
                    break;
            }
        }

        if( sb.length()<1 )throw new IllegalArgumentException("key or char not specified");

        var named = namedKeyOrChar(sb.toString());
        if( named.isPresent() ){
            if( named.get().a()!=null ){
                return new KeyStroke(named.get().a(), mod_ctrl, mod_alt, mod_shift);
            }else if( named.get().b()!=null ){
                return new KeyStroke(named.get().b(), mod_ctrl, mod_alt, mod_shift);
            }else{
                throw new IllegalStateException("internal bug! namedKeyOrChar() return null value");
            }
        }

        if( sb.length()==1 ){
            return new KeyStroke(sb.toString().charAt(0), mod_ctrl, mod_alt, mod_shift);
        }

        var unicode = fromUnicode(sb.toString());
        if( unicode.isPresent() ){
            char c = (char)(int)unicode.get();
            return new KeyStroke(c, mod_ctrl, mod_alt, mod_shift);
        }

        throw new IllegalArgumentException("key or char not specified");
    }
}
