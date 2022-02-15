package xyz.cofe.jtfm.alg;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.widget.IWidget;

import java.util.Optional;

/**
 * Методы дерева
 * @param <N> узел дерева
 */
public interface LikeTree<N> {
    /**
     * Получение родительского узла
     * @param node узел
     * @return родительский узел
     */
    public Optional<N> parentOf(@NonNull N node);

    public int childrenSize(@NonNull N node);
    public Optional<N> child(@NonNull N node, int idx);
    public Optional<Integer> indexOf(N parent,N child);
    public default Optional<N> sib(@NonNull N node,int idx) {
        var prnt = parentOf(node);
        if( prnt.isEmpty() )return Optional.empty();

        var oidx = indexOf(prnt.get(), node);
        if( oidx.isEmpty() )return Optional.empty();

        var chld_cnt = childrenSize(prnt.get());
        if( chld_cnt<1 )return Optional.empty();

        var t_idx = oidx.get() + idx;
        if( t_idx<0 )return Optional.empty();
        if( t_idx>=chld_cnt )return Optional.empty();

        return child(prnt.get(), t_idx);
    }

    public static LikeTree<IWidget<?>> widgetTree() {
        return new LikeTree<IWidget<?>>() {
            @Override
            public Optional<IWidget<?>> parentOf( @NonNull IWidget<?> node ){
                return node.parent().get();
            }

            @Override
            public int childrenSize( @NonNull IWidget<?> node ){
                return node.nestedWidgets().size();
            }

            @Override
            public Optional<IWidget<?>> child( @NonNull IWidget<?> node, int idx ){
                if( idx<0 )return Optional.empty();
                if( idx<childrenSize(node) )return Optional.of(node.nestedWidgets().get(idx));
                return Optional.empty();
            }

            @Override
            public Optional<Integer> indexOf( IWidget<?> parent, IWidget<?> child ){
                int i = parent.nestedWidgets().indexOf(child);
                return i<0 ? Optional.empty() : Optional.of(i);
            }
        };
    }
}
