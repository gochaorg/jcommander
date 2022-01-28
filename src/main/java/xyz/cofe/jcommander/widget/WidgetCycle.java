package xyz.cofe.jcommander.widget;

import com.googlecode.lanterna.terminal.Terminal;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Основной цикл работы с виджетами.
 * <ol>
 *     <li> Для начала работы должен быть создан терминал {@link Terminal}
 *     <li> Должен быть создан цикл обработки {@link #create(Terminal)}
 *     <li> Эти операции должны быть выполнены в одном потоке {@link java.lang.Thread}
 * </ol>
 */
public class WidgetCycle {
    private final Terminal terminal;
    private final long threadId;

    private WidgetCycle( @NonNull Terminal terminal ){
        this.terminal = terminal;
        threadId = Thread.currentThread().getId();
    }
    private WidgetCycle( @NonNull Terminal terminal, long threadId ){
        this.terminal = terminal;
        this.threadId = threadId;
    }

    private static final InheritableThreadLocal<WidgetCycle> currentCycle = new InheritableThreadLocal<>();
    private static final WeakHashMap<Terminal,WidgetCycle> cycles = new WeakHashMap<>();

    /**
     * Получение ранее созданного цикла, если цикл не был создан, то будет исключение {@link IllegalThreadStateException}
     * @return цикл
     */
    public static WidgetCycle get(){
        var wc = currentCycle.get();
        if( wc==null )throw new IllegalThreadStateException("current thread has not attached terminal");
        return wc;
    }

    /**
     * Получение ранее созданного цикла
     * @return цикл
     */
    public static Optional<WidgetCycle> tryGet(){
        var wc = currentCycle.get();
        return wc!=null ? Optional.of(wc) : Optional.empty();
    }

    /**
     * Создание цикла обработки терминала.
     * Важно что в текущем (или родительском) потоке {@link Thread} не был создан цикл обработки
     * @return цикл обработки
     */
    public static WidgetCycle create( @NonNull Terminal terminal ){
        //noinspection ConstantConditions
        if( terminal==null )throw new IllegalArgumentException( "terminal==null" );

        var wc = currentCycle.get();
        if( wc!=null )throw new IllegalThreadStateException("current thread already contains WidgetCycle");

        wc = cycles.get(terminal);
        if( wc==null ){
            wc = new WidgetCycle(terminal);
            cycles.put(terminal,wc);
        }

        currentCycle.set(wc);
        return wc;
    }

    /**
     * Запуск цикла обработки
     */
    public void run(){

    }
}
