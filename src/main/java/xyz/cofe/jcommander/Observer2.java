package xyz.cofe.jcommander;

import xyz.cofe.fn.Consumer1;
import xyz.cofe.fn.Consumer2;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Observer2<A,B> {
    private Set<Consumer2<A,B>> listeners;

    public Observer2(){
        listeners = new HashSet<>();
    }

    public Observer2( boolean concurrent){
        listeners = concurrent ? new CopyOnWriteArraySet<>() : new HashSet<>();
    }

    public void fire( A event_a, B event_b ){
        if( event_a==null )throw new IllegalArgumentException( "event_a==null" );
        if( event_b==null )throw new IllegalArgumentException( "event_b==null" );
        for( var ls : listeners ){
            if( ls==null )continue;
            ls.accept(event_a, event_b);
        }
    }

    public AutoCloseable listen( Consumer2<A,B> listener ){
        if( listener==null )throw new IllegalArgumentException( "listener==null" );
        listeners.add(listener);
        WeakReference<Consumer2<A,B>> ref = new WeakReference<>(listener);
        return () -> {
            var trgt = ref.get();
            if( trgt==null )return;

            listeners.remove(trgt);
            ref.clear();
        };
    }
}
