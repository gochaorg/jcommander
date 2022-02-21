package xyz.cofe.jtfm.alg;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    /**
     * Направление выборки
     */
    public static enum Direction
    {
        Next,
        Prev;
    }

    /**
     * Итератор
     * @param <N> элементы итератора
     */
    private static class NavIterator<N>
    implements Iterator<N>
    {
        private final NavTree<N> navTree;
        private final Direction direction;
        private Optional<N> cur;

        public NavIterator( @NonNull NavTree<N> navTree, @NonNull N from, @NonNull Direction d, boolean includeSelf ){
            this.navTree = navTree;
            this.direction = d;
            this.cur = includeSelf ? fetch(navTree, from,d) : Optional.of(from);
        }

        private static <N> Optional<N> fetch( NavTree<N> navTree, @NonNull N n, Direction d ){
            switch( d ){
                case Next:
                    return navTree.next(n);
                case Prev:
                    return navTree.prev(n);
            }
            throw new UnsupportedOperationException("!!!");
        }

        @Override
        public boolean hasNext(){
            return cur.isPresent();
        }

        @Override
        public N next(){
            N r = cur.get();
            cur = fetch(navTree, cur.get(), direction);
            return r;
        }
    }

    /**
     * Итератор относительно указанного элемента
     * @param n узел
     * @param d направление
     * @param includeSelf включать в выборку сам узел
     * @return итератор
     */
    @SuppressWarnings("nullness")
    public Iterator<N> iterator( @NonNull N n, @NonNull Direction d, boolean includeSelf ){
        return new NavIterator<N>(this, n, d, includeSelf);
    }

    private static class NavIterable<N> implements Iterable<N> {
        private final Supplier<Iterator<N>> iterator;

        public NavIterable( Supplier<Iterator<N>> iterator ){
            this.iterator = iterator;
        }

        @Override
        public Iterator<N> iterator(){
            return this.iterator.get();
        }
    }

    /**
     * Итератор
     * @param n от какого элемента итерирование
     * @param d направление
     * @param includeSelf включать сам элемент в выборку
     * @return итератор
     */
    public Iterable<N> iterable( @NonNull N n, @NonNull Direction d, boolean includeSelf ){
        return new NavIterable<N>( ()->this.iterator(n, d, includeSelf) );
    }

    /**
     * Поиск крайнего элемента
     * @param n элемент
     * @param d направление
     * @return крайний элемент
     */
    public Optional<N> extreme( @NonNull N n, @NonNull Direction d ){
        if( !filter.test(n) )return Optional.empty();
        while( true ){
            Optional<N> follow = Optional.empty();
            switch( d ){
                case Next:
                    follow = next(n);
                    break;
                case Prev:
                    follow = prev(n);
                    break;
            }
            if( follow.isEmpty() )break;
            n = follow.get();
        }
        return Optional.of(n);
    }

    /**
     * Итератор с последнего элемента к первому
     * @param n элемент дерева
     * @return итератор
     */
    public Iterator<N> lastToHeadIterator( @NonNull N n ){
        var e = extreme(n,Direction.Next);
        if( e.isEmpty() )return Collections.emptyIterator();
        return iterator(e.get(), Direction.Prev, true);
    }

    /**
     * Итератор с последнего элемента к первому
     * @param n элемент дерева
     * @return итератор
     */
    public Iterable<N> lastToHead( @NonNull N n ){
        return new NavIterable<>( ()->lastToHeadIterator(n) );
    }
}
