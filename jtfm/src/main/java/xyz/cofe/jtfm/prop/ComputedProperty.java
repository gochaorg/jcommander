package xyz.cofe.jtfm.prop;

import xyz.cofe.ecolls.Closeables;
import xyz.cofe.fn.Fn1;
import xyz.cofe.fn.Fn2;
import xyz.cofe.fn.Fn3;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public abstract class ComputedProperty<A> implements Property<A> {
    protected final Closeables listenProps = new Closeables();

    protected void listen( Property<?> ... props ){
        for( var prop : props ){
            prop.onChange( (p)->{
                if( computed ){
                    computed = false;
                    fireChanged();
                }
            });
        }
    }

    protected void listen( Iterable<Property<?>> props ){
        for( var prop : props ){
            prop.onChange( (p)->{
                if( computed ){
                    computed = false;
                    fireChanged();
                }
            });
        }
    }

    protected abstract Supplier<A> eval();

    protected A value;
    protected boolean computed = false;

    @Override
    public A get(){
        if( computed ){
            return value;
        }else{
            value = recurProtectEval( eval() );
            computed = true;
            return value;
        }
    }

    protected final ThreadLocal<Integer> recurLevel = ThreadLocal.withInitial(() -> 0);

    protected A recurProtectEval( Supplier<A> eval ){
        try{
            var level = recurLevel.get();
            if( level>0 ){
                throw new IllegalStateException("recursive compute value");
            }
            recurLevel.set(level+1);
            return eval.get();
        } finally {
            var level = recurLevel.get();
            recurLevel.set(level-1);
        }
    }

    protected void fireChanged(){
        for( var ls:listeners ){
            ls.propertyChanged(this);
        }
    }

    protected final Set<PropertyListener<A>> listeners = new HashSet<>();

    protected void remove(PropertyListener<A> ls){
        listeners.remove(ls);
    }

    @Override
    public AutoCloseable onChange( PropertyListener<A> ls ){
        if( ls==null )throw new IllegalArgumentException("ls==null");
        listeners.add(ls);
        var wref = new WeakReference<>(ls);
        return () -> {
            var ref = wref.get();
            if( ref!=null ){
                remove(ref);
            }
            wref.clear();
        };
    }

    public static <A> Compute1Builder<A> with( Property<A> prop ){
        if( prop==null )throw new IllegalArgumentException( "prop==null" );
        return new Compute1Builder<>(prop);
    }

    public static class Compute1Builder<A> {
        public final Property<A> prop_a;

        public Compute1Builder( Property<A> prop_a ){
            this.prop_a = prop_a;
        }

        public <Z> ComputedProperty1<Z,A> build( Fn1<A,Z> eval ){
            if( eval==null )throw new IllegalArgumentException( "eval==null" );
            return new ComputedProperty1<>(prop_a, eval);
        }

        public <B> Compute2Builder<A,B> with( Property<B> prop ){
            return new Compute2Builder<>(prop_a, prop);
        }
    }

    public static class Compute2Builder<A,B> {
        public final Property<A> prop_a;
        public final Property<B> prop_b;

        public Compute2Builder( Property<A> prop_a, Property<B> prop_b ){
            this.prop_a = prop_a;
            this.prop_b = prop_b;
        }

        public <Z> ComputedProperty2<Z,A,B> build( Fn2<A,B,Z> eval ){
            if( eval==null )throw new IllegalArgumentException( "eval==null" );
            return new ComputedProperty2<>(prop_a, prop_b, eval);
        }

        public <C> Compute3Builder<A,B,C> with( Property<C> prop ){
            return new Compute3Builder<>(prop_a, prop_b, prop);
        }
    }

    public static class Compute3Builder<A,B,C> {
        public final Property<A> prop_a;
        public final Property<B> prop_b;
        public final Property<C> prop_c;

        public Compute3Builder( Property<A> prop_a, Property<B> prop_b, Property<C> prop_c ){
            this.prop_a = prop_a;
            this.prop_b = prop_b;
            this.prop_c = prop_c;
        }

        public <Z> ComputedProperty3<Z,A,B,C> build( Fn3<A,B,C,Z> eval ){
            if( eval==null )throw new IllegalArgumentException( "eval==null" );
            return new ComputedProperty3<>(prop_a, prop_b, prop_c, eval);
        }
    }

    public static <R> ComputedProperty<R> capture( Compute<R> computing ){
        if( computing==null )throw new IllegalArgumentException( "computing==null" );

        Method writeReplace = null;
        try{
            writeReplace = computing.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda sl = (SerializedLambda) writeReplace.invoke(computing);

            var props = new ArrayList<Property<?>>();

            for( var ai=0; ai<sl.getCapturedArgCount(); ai++ ){
                var arg = sl.getCapturedArg(ai);
                if( arg instanceof Property ){
                    props.add((Property<?>) arg);
                }
            }

            var computedProp = new ComputedProperty<R>(){
                @Override
                protected Supplier<R> eval(){
                    return computing;
                }
            };

            computedProp.listen(props);

            return computedProp;
        } catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ){
            throw new Error(e);
        }
    }
}
