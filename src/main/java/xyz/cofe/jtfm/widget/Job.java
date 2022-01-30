package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Задача для выполнения
 */
public class Job<R> {
    public Job(@NonNull Supplier<@Nullable R> code){
        //noinspection ConstantConditions
        if( code==null )throw new IllegalArgumentException( "code==null" );
        this.code = code;
        startedNanoTime = 0;
        finishedNanoTime = 0;
        result = null;
    }

    private volatile long startedNanoTime =0;
    private volatile long finishedNanoTime=0;

    private volatile @Nullable R result;
    private final Supplier<@Nullable R> code;
    private final Queue<Consumer<@Nullable R>> consumers = new ArrayDeque<>();

    public long getStartedNanoTime(){ return startedNanoTime; }
    public long getFinishedNanoTime(){ return finishedNanoTime; }

    public synchronized void run(){
        try {
            startedNanoTime = System.nanoTime();
            result = code.get();
        } finally {
            finishedNanoTime = System.nanoTime();
            while( true ){
                Consumer<@Nullable R> c = consumers.poll();
                if( c==null )break;
                c.accept(result);
            }
        }
    }

    public synchronized Job<R> after( @NonNull Consumer<@Nullable R> consumer ){
        //noinspection ConstantConditions
        if( consumer==null )throw new IllegalArgumentException( "consumer==null" );
        if( finishedNanoTime>0 ){
            consumer.accept(result);
        }else{
            consumers.add(consumer);
        }
        return this;
    }
}
