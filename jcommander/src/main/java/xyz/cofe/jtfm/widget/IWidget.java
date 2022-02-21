package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.input.MouseAction;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.gr.Point;

import java.util.ArrayList;
import java.util.List;

public interface IWidget<SELF extends IWidget<SELF>>
    extends
        Render,
        RectProperty<SELF>,
        IsFocusable,
        VisibleProperty<SELF>,
        Input,
        RelativeLayout,
        NestedWidgets,
        ParentProperty<IWidget<?>,SELF>
{
    public default List<IWidget<?>> nodePath(){
        List<IWidget<?>> lst = new ArrayList<>();
        IWidget<? extends IWidget<?>> n = this;
        lst.add(n);
        while( true ){
            var p = n.parent().get();
            if( p.isEmpty() )break;
            var pp = p.get();
            n = pp;
            lst.add(0,pp);
        }
        return lst;
    }

    default Point toLocal( @NonNull Point p ){
        var path = nodePath();
        for( var n : path ){
            p = p.translate(n.rect().get().leftTop().invert());
        }
        return p;
    }

    default MouseAction toLocal( @NonNull MouseAction a ){
        var loc = toLocal(new Point(a.getPosition()));
        return new MouseAction(
            a.getActionType(), a.getButton(), loc.toTerminalPosition()
        );
    }

    default Point toAbsolute( @NonNull Point p ){
        var path = nodePath();
        for( var n : path ){
            p = p.translate(n.rect().get().leftTop());
        }
        return p;
    }
}
