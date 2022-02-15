package xyz.cofe.jtfm.alg;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.widget.IWidget;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Навигация по дереву в порядке - в глубину.
 * @param <N> Узел дерева
 */
public class NavTree<N> {
    /**
     * Методы дерева
     */
    public final LikeTree<N> likeTree;

    /**
     * Конструктор
     * @param likeTree методы дерева
     */
    public NavTree( @NonNull LikeTree<N> likeTree ){
        this.likeTree = likeTree;
        this.filter = (x) -> true;
    }

    /**
     * Конструктор
     * @param likeTree методы дерева
     * @param filter фильтр узлов
     */
    public NavTree( @NonNull LikeTree<N> likeTree, @NonNull Predicate<N> filter ){
        this.likeTree = likeTree;
        this.filter = filter;
    }

    private final Predicate<N> filter;

    /**
     * Клонировать и указать фильтр
     * @param filter фильтр
     * @return клонс с фильтром
     */
    public NavTree<N> filter( @NonNull Predicate<N> filter ){
        return new NavTree<>( this.likeTree, filter );
    }

    /**
     * Поиск следующего узла (виджета для рендера) относительно указанного узла
     * @param wid узел
     * @return следующий
     */
    public Optional<N> next( @NonNull N wid ){
        //noinspection ConstantConditions
        if( wid==null )throw new IllegalArgumentException( "wid==null" );
        int childrenSize = likeTree.childrenSize(wid);
        if( childrenSize>0 ){
            for( int ni=0; ni<childrenSize; ni++ ){
                var nwid = likeTree.child(wid,ni);
                if( nwid.isEmpty() )continue;
                if( !filter.test(nwid.get()) )continue;
                return nwid;
            }
        }

        while( true ) {
            while( true ) {
                var w_sib = likeTree.sib(wid, 1);
                if( w_sib.isEmpty() ) break;
                if( filter.test(w_sib.get()) ) return w_sib;
                wid = w_sib.get();
            }

            var w_prnt = likeTree.parentOf(wid);
            if( w_prnt.isEmpty() ) return Optional.empty();
            wid = w_prnt.get();
        }
    }

    private Optional<N> last_child_or_self_visible(@NonNull N wid){
        if( !filter.test(wid) )return Optional.empty();

        while( true ) {
            int cs = likeTree.childrenSize(wid);
            if( cs<1 )return Optional.of(wid);

            for( int ni=cs-1; ni>=0; ni++ ){
                var w_ch = likeTree.child(wid,ni);
                if( w_ch.isEmpty() )continue;
                if( !filter.test(w_ch.get()) )continue;

                var n_cs = likeTree.childrenSize(w_ch.get());
                if( n_cs<1 )return w_ch;

                wid = w_ch.get();
                break;
            }
        }
    }

    /**
     * Поиск предыдущего узла (виджета для рендера) для рендера относительно указанного узла
     * @param wid узел
     * @return предыдущий
     */
    public Optional<N> prev( @NonNull N wid){
        //noinspection ConstantConditions
        if( wid==null )throw new IllegalArgumentException( "wid==null" );

        var w_sib = likeTree.sib(wid,-1);
        if( w_sib.isPresent() ){
            return last_child_or_self_visible(w_sib.get());
        }

        var p = likeTree.parentOf(wid);
        if( p.isEmpty() )return Optional.empty();
        if( filter.test(p.get()) )return p;
        return Optional.empty();
    }
}
