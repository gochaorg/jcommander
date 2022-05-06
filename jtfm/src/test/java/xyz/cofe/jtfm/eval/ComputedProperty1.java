package xyz.cofe.jtfm.eval;

import xyz.cofe.fn.Fn1;

import java.util.function.Supplier;

public class ComputedProperty1<Z,A> extends ComputedProperty<Z> {
    public ComputedProperty1( Property<A> src0, Fn1<A,Z> eval ){
        if( src0==null )throw new IllegalArgumentException( "src0==null" );
        if( eval==null )throw new IllegalArgumentException( "eval==null" );
        listen(src0);
        this.eval = ()-> eval.apply(src0.get());
    }

    private final Supplier<Z> eval;

    @Override
    protected Supplier<Z> eval(){
        return eval;
    }
}
