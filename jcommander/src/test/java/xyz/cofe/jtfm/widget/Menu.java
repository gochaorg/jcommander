package xyz.cofe.jtfm.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Menu<SELF extends IWidget<SELF>> extends IWidget<SELF> {
    default Optional<Menu<?>> parentMenu() {
        IWidget<? extends IWidget<?>> w = this;
        while( true ){
            var p =w.parent();
            if( p.get().isEmpty() )break;

            w = p.get().get();
            if( w instanceof Menu )return Optional.of((Menu<?>)w);
        }
        return Optional.empty();
    }

    default List<Menu<?>> childrenMenus(){
        List<Menu<?>> lst = new ArrayList<>();
        for( var c : nestedNodes() ){
            if( c instanceof Menu ){
                lst.add((Menu<?>) c);
            }
        }
        return lst;
    }
}
