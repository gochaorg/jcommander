package xyz.cofe.jtfm.prop;

import xyz.cofe.fn.Fn2;

import java.util.function.Supplier;

public class ComputedProperty2<Z,A,B> extends ComputedProperty<Z> {
    public ComputedProperty2( Property<A> src0, Property<B> src1, Fn2<A,B,Z> eval ){
        if( src0==null )throw new IllegalArgumentException( "src0==null" );
        if( src1==null )throw new IllegalArgumentException( "src1==null" );
        if( eval==null )throw new IllegalArgumentException( "eval==null" );
        listen(src0,src1);
        this.eval = ()-> eval.apply(src0.get(), src1.get());
    }

    private final Supplier<Z> eval;

    @Override
    protected Supplier<Z> eval(){
        return eval;
    }
}
