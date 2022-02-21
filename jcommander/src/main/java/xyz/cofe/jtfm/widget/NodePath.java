package xyz.cofe.jtfm.widget;

import java.util.ArrayList;
import java.util.List;

public interface NodePath<N extends ParentProperty<N,N>> extends ParentProperty<N,N> {
    public default List<N> nodePath(){
        List<N> p = new ArrayList<>();
        N n = parent().owner();
        while( true ) {
            p.add(0, n);
            if( n.parent().get().isEmpty() )break;
            n = n.parent().get().get();
        }
        return p;
    }
}
