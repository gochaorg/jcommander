package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.MouseAction;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.cofe.fn.Fn1;
import xyz.cofe.fn.Tuple2;
import xyz.cofe.jtfm.Change;
import xyz.cofe.jtfm.RelTxtGraphics;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Основной цикл работы с виджетами.
 * <ol>
 *     <li> Для начала работы должен быть создан терминал {@link Terminal}
 *     <li> Должен быть создан цикл обработки {@link #create(Terminal)}
 *     <li> Эти операции должны быть выполнены в одном потоке {@link java.lang.Thread}
 * </ol>
 */
public class WidgetCycle {
    private void log( Throwable e ){}

    private final Terminal terminal;
    private final long threadId;

    protected WidgetCycle( @NonNull Terminal terminal ){
        this.terminal = terminal;
        threadId = Thread.currentThread().getId();
    }
    protected WidgetCycle( @NonNull Terminal terminal, long threadId ){
        this.terminal = terminal;
        this.threadId = threadId;
    }

    private static final InheritableThreadLocal<@Nullable WidgetCycle> currentCycle = new InheritableThreadLocal<>();
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

        return create(terminal, null);
    }

    /**
     * Создание цикла обработки терминала.
     * Важно что в текущем (или родительском) потоке {@link Thread} не был создан цикл обработки
     * @param terminal терминал
     * @param build функция создания WidgetCycle
     * @return цикл обработки
     */
    public static WidgetCycle create( @NonNull Terminal terminal, @Nullable Fn1<Terminal,WidgetCycle> build ){
        //noinspection ConstantConditions
        if( terminal==null )throw new IllegalArgumentException( "terminal==null" );

        var wc = currentCycle.get();
        if( wc!=null )throw new IllegalThreadStateException("current thread already contains WidgetCycle");

        wc = cycles.get(terminal);
        if( wc==null ){
            wc = build==null ? new WidgetCycle(terminal) : build.apply(terminal);
            cycles.put(terminal,wc);
        }

        currentCycle.set(wc);
        return wc;
    }

    private void onTerminalResized( TerminalSize size ){
    }

    //public boolean

    private final Queue<Job<?>> jobs = new ConcurrentLinkedQueue<>();

    /**
     * Добавление задания в очередь
     * @param job задание
     */
    public void addJob(@NonNull Job<?> job){
        //noinspection ConstantConditions
        if( job==null )throw new IllegalArgumentException( "job==null" );
        jobs.add(job);
    }

    //region widgets : List<IWidget<?>>
    private final List<IWidget<?>> widgets = new ArrayList<>();

    /**
     * Добавление виджета
     * @param widget виджет
     */
    public void addWidget( @NonNull IWidget<?> widget ){
        //noinspection ConstantConditions
        if( widget==null )throw new IllegalArgumentException( "widget==null" );
        checkThreadMatch("can't add widget");
        widgets.add(widget);
        addRenderRequest(widget);
    }

    /**
     * Удаление виджета
     * @param widget виджет
     */
    public void removeWidget( @NonNull IWidget<?> widget ){
        //noinspection ConstantConditions
        if( widget==null )throw new IllegalArgumentException( "widget==null" );
        checkThreadMatch("can't remove widget");
        widgets.remove(widget);
    }

    /**
     * Получение списка виджетов
     * @return список виджетов (readonly)
     */
    public List<IWidget<?>> widgets(){
        return Collections.unmodifiableList(widgets);
    }
    //endregion

    private long cycleSleep(){ return 1; }
    private boolean cycleYield(){ return true; }

    private long socket_timeout_cycleSleep(){ return -1; }
    private boolean socket_timeout_cycleYield(){ return false; }

    private final Set<IWidget<?>> redrawRequests = new HashSet<>();

    private void checkThreadMatch( @NonNull String message ){
        if( Thread.currentThread().getId()!=threadId ){
            throw new IllegalThreadStateException(
                message+"\n"+
                    "WidgetCycle thread.id="+threadId+"\n"+
                    "this thread.id="+Thread.currentThread().getId()
            );
        }
    }

    /**
     * Добавление запроса на перерисовку.
     * @param render рендер
     */
    public void addRenderRequest( @NonNull IWidget<?> render ){
        //noinspection ConstantConditions
        if( render==null )throw new IllegalArgumentException( "render==null" );
        addJob(new Job<>( ()->{ redrawRequests.add(render); return null; } ));
    }

    private @Nullable IWidget<?> focusOwner;
    private int focusOwnerPointer = -1;
    private int focusHistoryMaxSize(){ return 1000; }

    private List<WeakReference<IWidget<?>>> focusOwnerHistory = new LinkedList<>();

    /**
     * Смена фокуса
     * @param newFocusOwner допускается null, требуется что бы, было <br>
     *                      newFocusOwner.isFocusable() <br>
     *                      newFocusOwner.visible().get()
     * @return Смена фокуса
     */
    public Optional<Change<IWidget<?>,IWidget<?>>> setFocusOwner( @Nullable IWidget<?> newFocusOwner ){
        var oldFocusOwner = focusOwner;
        if( newFocusOwner==oldFocusOwner )return Optional.empty();

        if( newFocusOwner!=null ){
            if( !newFocusOwner.isFocusable() ) throw new IllegalArgumentException("!newFocusOwner.isFocusable()");
            if( !newFocusOwner.visible().get() ) throw new IllegalArgumentException("!newFocusOwner.visible().get()");
        }

        focusOwner = newFocusOwner;
        if( newFocusOwner!=null ){
            if( focusOwnerPointer<0 ){
                focusOwnerHistory.add(new WeakReference<>(newFocusOwner));
                int dropSize = focusOwnerHistory.size() - focusHistoryMaxSize();
                if( dropSize>0 ){
                    focusOwnerHistory.subList(0, dropSize).clear();
                }
            }
        }

        if( oldFocusOwner instanceof OnFocusLost ){
            ((OnFocusLost)oldFocusOwner).focusLost(
                newFocusOwner != null ? Optional.of(newFocusOwner) : Optional.empty()
            );
        }
        if( newFocusOwner instanceof OnFocusGain ){
            ((OnFocusGain)newFocusOwner).focusGain(
                oldFocusOwner != null ? Optional.of(oldFocusOwner) : Optional.empty()
            );
        }

        return Optional.of(
            Change.fromTo(
                oldFocusOwner,
                newFocusOwner
            )
        );
    }

    /**
     * Получение элемента содержащего фокус ввода
     * @return фокус ввода
     */
    public Optional<IWidget<?>> findFocusOwner(){
        if( focusOwner!=null )return Optional.of(focusOwner);
        if( widgets.isEmpty() )return Optional.empty();

        var itr = widgets.listIterator(widgets.size());
        while( itr.hasPrevious() ){
            var w = itr.previous();
            if( w==null )continue;
            if( !w.visible().get() )continue;
            if( !w.isFocusable() )continue;
            setFocusOwner(w);
            return Optional.of(w);
        }

        return Optional.empty();
    }

    /**
     * Переключение фокуса
     * @return смененный фокус
     */
    public Optional<IWidget<?>> switchNextFocus(){
        if( focusOwner==null )return findFocusOwner();
        if( widgets.isEmpty() )return Optional.empty();

        int focusIdx = widgets.indexOf(focusOwner);
        if( focusIdx<0 )return Optional.empty();

        var ranges = List.of(Tuple2.of(0,focusIdx), Tuple2.of(focusIdx+1, widgets.size()));
        for( var range : ranges ){
            var cnt = range.b() - range.a();
            if( cnt<1 )continue;

            for( var i=range.a(); i<range.b(); i++ ){
                var itm = widgets.get(i);
                if( itm==null || !itm.visible().get() || !itm.isFocusable() )continue;
                setFocusOwner(itm);
                return Optional.of(itm);
            }
        }

        return Optional.empty();
    }

    /**
     * Обработка очереди заданий
     * @return кол-во заданий
     */
    protected int processJobs(){
        int cnt=0;
        while( true ) {
            var job = jobs.poll();
            if( job==null )break;
            cnt++;

            try{
                job.run();
            } catch( Throwable e ){
                log(e);
            }
        }
        return cnt;
    }

    protected void processInput( KeyStroke ks ){
        if( ks instanceof MouseAction ){
            var ma = (MouseAction)ks;
            IWidget<?> found = null;
            for( int i=widgets.size()-1; i>=0; i-- ){
                var w = widgets.get(i);
                if( w==null || !w.visible().get() )continue;
                if( w.rect().get().include( ma.getPosition() ) ){
                    found = w;
                    break;
                }
            }
            if( found!=null ){
                if( found.isFocusable() ){
                    setFocusOwner(found);
                }
                found.input(ma);
            }
        }else {
            try{
                findFocusOwner().ifPresent(wid -> wid.input(ks));
            } catch( Throwable err ){
                log(err);
            }
        }
    }

    @SuppressWarnings("nullness")
    private void resetCursorPos( TerminalScreen screen ){
        screen.setCursorPosition(null);
    }

    /**
     * Запуск цикла обработки
     */
    public void run(){
        TerminalScreen screen = null;
        TerminalResizeListener resizer = (term,size) -> { onTerminalResized(size); };

        try {
            screen = new TerminalScreen(terminal);
            screen.startScreen();

            resetCursorPos(screen);

            terminal.addResizeListener( resizer );

            while( true ){
                processJobs();

                ///////////////////////
                // process input
                KeyStroke ks1 = null;
                while( true ) {
                    try{
                        ks1 = screen.pollInput();
                    } catch( SocketTimeoutException ex ){
                        long c_sleep = socket_timeout_cycleSleep();
                        if( c_sleep > 0 ){
                            try{
                                Thread.sleep(c_sleep);
                            } catch( InterruptedException e ){
                                log(e);
                                break;
                            }
                        }
                        if( socket_timeout_cycleYield() ) Thread.yield();
                    }
                    if( ks1==null )break;
                    processInput(ks1);
                }

                //////////////////
                // process render
                if( !redrawRequests.isEmpty() ){
                    screen.doResizeIfNecessary();
                    var g = screen.newTextGraphics();
                    for( var wid : widgets ){
                        if( redrawRequests.contains(wid) ){
                            try{
                                if( wid.visible().get() ){
                                    if( wid.relativeLayout() ){
                                        wid.render(new RelTxtGraphics(g));
                                    }else {
                                        wid.render(g);
                                    }
                                }
                            } catch( Throwable renderErr ){
                                log(renderErr);
                            }
                            redrawRequests.remove(wid);
                        }
                    }
                    screen.refresh();
                }

                //////////////////
                // exit ?
                if( jobs.isEmpty() && widgets.isEmpty() ){
                    break;
                }

                //////////////////
                // reduce load cpu
                long c_sleep = cycleSleep();
                if( c_sleep>0 ){
                    try{
                        Thread.sleep(c_sleep);
                    } catch( InterruptedException e ){
                        log(e);
                        break;
                    }
                }
                if( cycleYield() )Thread.yield();
            }

        } catch( IOException e ){
            log(e);
        } finally {
            try{
                terminal.removeResizeListener(resizer);
                if( screen!=null ){
                    screen.stopScreen();
                }
            } catch( IOException e ){
                log(e);
            }
        }
    }
}
