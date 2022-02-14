package xyz.cofe.jtfm.alg;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public interface LikeTree<N> {
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
}
