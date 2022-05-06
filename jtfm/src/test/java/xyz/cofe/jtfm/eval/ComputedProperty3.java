package xyz.cofe.jtfm.eval;

import xyz.cofe.fn.Fn2;
import xyz.cofe.fn.Fn3;

import java.util.function.Supplier;

public class ComputedProperty3<Z,A,B,C> extends ComputedProperty<Z> {
    public ComputedProperty3( Property<A> src0, Property<B> src1, Property<C> src2, Fn3<A,B,C,Z> eval ){
        if( src0==null )throw new IllegalArgumentException( "src0==null" );
        if( src1==null )throw new IllegalArgumentException( "src1==null" );
        if( src2==null )throw new IllegalArgumentException( "src2==null" );
        if( eval==null )throw new IllegalArgumentException( "eval==null" );
        listen(src0,src1,src2);
        this.eval = ()-> eval.apply(src0.get(), src1.get(), src2.get());
    }

    private final Supplier<Z> eval;

    @Override
    protected Supplier<Z> eval(){
        return eval;
    }
}
