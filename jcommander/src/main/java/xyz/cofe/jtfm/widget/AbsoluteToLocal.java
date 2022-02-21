package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.fn.Tuple2;
import xyz.cofe.jtfm.gr.Point;

public interface AbsoluteToLocal
    <SELF extends ParentProperty<SELF,SELF> & RectProperty<SELF>>
    extends NodePath<SELF>
{
    default Point toLocal( @NonNull Point p ){
        var path = nodePath();
        for( var n : path ){
            p = p.translate(n.rect().get().leftTop().invert());
        }
        return p;
    }

    default Point toAbsolute( @NonNull Point p ){
        var path = nodePath();
        for( var n : path ){
            p = p.translate(n.rect().get().leftTop());
        }
        return p;
    }
}
