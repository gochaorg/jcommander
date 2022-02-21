package xyz.cofe.jtfm;

import xyz.cofe.fn.Consumer1;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Observer1<A> {
    private Set<Consumer1<A>> listeners;

    public Observer1(){
        listeners = new HashSet<>();
    }

    public Observer1(boolean concurrent){
        listeners = concurrent ? new CopyOnWriteArraySet<>() : new HashSet<>();
    }

    public void fire( A event ){
        if( event==null )throw new IllegalArgumentException( "event==null" );
        for( var ls : listeners ){
            if( ls==null )continue;
            ls.accept(event);
        }
    }

    public AutoCloseable listen( Consumer1<A> listener ){
        if( listener==null )throw new IllegalArgumentException( "listener==null" );
        listeners.add(listener);
        WeakReference<Consumer1<A>> ref = new WeakReference<>(listener);
        return () -> {
            var trgt = ref.get();
            if( trgt==null )return;

            listeners.remove(trgt);
            ref.clear();
        };
    }
}
