package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.OwnProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Свойство - родительский виджет
 */
public interface ParentProperty<PARENT,SELF> {
    /**
     * Свойство - родительский виджет
     * @return виджет
     */
    OwnProperty<Optional<PARENT>,SELF> parent();

    public static <E extends ParentProperty<E,?>> List<E> nodePath( @NonNull E e ) {
        List<E> lst = new ArrayList<>();
        lst.add(e);
        while( true ){
            var p = e.parent().get();
            if( p.isPresent() ){
                e = p.get();
                lst.add(0,e);
            }else {
                break;
            }
        }
        return lst;
    }
}
