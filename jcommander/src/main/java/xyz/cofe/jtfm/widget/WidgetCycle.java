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
import xyz.cofe.jtfm.alg.LikeTree;
import xyz.cofe.jtfm.alg.NavTree;
import xyz.cofe.jtfm.gr.Point;
import xyz.cofe.jtfm.gr.Rect;
import xyz.cofe.jtfm.gr.RelTxtGraphics;

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
        if( size==null )return;
        widgetRoot().rect().set(0,0, size.getColumns(), size.getRows() );
    }

    //region jobs : Queue<Job<?>> - конкурентная очередь задач
    private final Queue<Job<?>> jobs = new ConcurrentLinkedQueue<>();

    /**
     * Добавление задания в очередь, thread safe
     * @param job задание
     */
    public void addJob(@NonNull Job<?> job){
        //noinspection ConstantConditions
        if( job==null )throw new IllegalArgumentException( "job==null" );
        jobs.add(job);
    }
    //endregion

    //region widgetRoot : VirtualRoot - Виртуальный корень виджетов
    private final VirtualRoot widgetRoot = new VirtualRoot(List.of());

    /**
     * Виртуальный корень виджетов
     * @return корень виджетов
     */
    public VirtualRoot widgetRoot(){ return widgetRoot; }
    //endregion

    // TODO config
    private long cycleSleep(){ return 1; }

    // TODO config
    private boolean cycleYield(){ return true; }

    // TODO config
    private long socket_timeout_cycleSleep(){ return -1; }

    // TODO config
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

    // TODO config
    private int focusHistoryMaxSize(){ return 1000; }

    private final List<WeakReference<IWidget<?>>> focusOwnerHistory = new LinkedList<>();

    /**
     * Смена фокуса
     * @param newFocusOwner допускается null, требуется что бы, было <br>
     *                      newFocusOwner.isFocusable() <br>
     *                      newFocusOwner.visible().get()
     * @return Смена фокуса
     */
    public Optional<Change<Optional<IWidget<?>>,Optional<IWidget<?>>>> setFocusOwner( @Nullable IWidget<?> newFocusOwner ){
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
                oldFocusOwner !=null ? Optional.of(oldFocusOwner) : Optional.empty(),
                newFocusOwner != null ? Optional.of(newFocusOwner) : Optional.empty()
            )
        );
    }

    /**
     * Получение элемента содержащего фокус ввода
     * @return фокус ввода
     */
    public Optional<IWidget<?>> findFocusOwner(){
        if( focusOwner!=null )return Optional.of(focusOwner);
        if( widgetRoot.nestedNodes().isEmpty() )return Optional.empty();

        var itr = widgetRoot.nestedNodes().listIterator(widgetRoot.nestedNodes().size());
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
    public Optional<Change<Optional<IWidget<?>>,Optional<IWidget<?>>>> switchNextFocus(){
        if( focusOwner==null ){
            var fo = findFocusOwner();
            return fo.isEmpty() ? Optional.empty() :
                Optional.of(
                    Change.fromTo(
                        Optional.empty(),
                        fo
                    )
                );
        }
        if( widgetRoot.nestedNodes().isEmpty() )return Optional.empty();

        if( focusOwner==null )return Optional.empty();
        IWidget<?> fo = focusOwner;
        boolean startFromFo = true;

        while( true ) {
            while( true ) {
                var nxt = visibleTreeNavigation.next(fo);
                if( nxt.isEmpty() ) break;
                if( nxt.get().isFocusable() ){
                    return setFocusOwner(nxt.get());
                }
            }
            if( startFromFo ){
                startFromFo = false;
                fo = widgetRoot;
            }else{
                break;
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

    /**
     * Обработка ввода (мыши или клавиатуры)
     * @param ks событие клавиатура
     */
    protected void processInput( KeyStroke ks ){
        if( ks instanceof MouseAction ){
            var ma = (MouseAction)ks;

            for( var wid : visibleTreeNavigation.lastToHead(widgetRoot()) ){
                var prnt_loc_ma = wid.parent().get().map( prnt_w -> prnt_w.toLocal(ma) ).orElse(ma);
                if( wid.rect().get().include( prnt_loc_ma.getPosition() ) ){
                    if( wid.isFocusable() ){
                        setFocusOwner(wid);
                    }
                    if( wid.input(wid.toLocal(ma)) ){
                        break;
                    }
                }
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

    // TODO config
    private boolean render_checkRequests(){ return true; }

    private final NavTree<IWidget<?>> visibleTreeNavigation =
        new NavTree<>(LikeTree.widgetTree()).filter(w -> w instanceof VirtualRoot || w.visible().get()
    );

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

            var term_size = screen.getTerminalSize();
            widgetRoot().rect().set( 0,0,term_size.getColumns(),term_size.getRows() );

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
                                //noinspection BusyWait
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
                if( !render_checkRequests() || !redrawRequests.isEmpty() ){
                    screen.doResizeIfNecessary();
                    var g = screen.newTextGraphics();

                    IWidget<?> wid = widgetRoot;
                    while( true ){
                        int rel_x = 0;
                        int rel_y = 0;
                        var w = wid;
                        while( true ){
                            if( w.relativeLayout() ){
                                rel_x += w.rect().get().left();
                                rel_y += w.rect().get().top();
                            }
                            var w_o = w.parent().get();
                            if( w_o.isEmpty() )break;
                            w = w_o.get();
                        }

                        if( wid.relativeLayout() ){
                            var rel_g = new RelTxtGraphics(g);
                            rel_g = rel_g.withLeftTop(rel_x, rel_y);
                            wid.render( rel_g );
                        }else{
                            wid.render( g );
                        }

                        var w_o = visibleTreeNavigation.next(wid);
                        if( w_o.isEmpty() )break;
                        wid = w_o.get();
                    }

                    if( !redrawRequests.isEmpty() ){
                        // TODO !!!!!
                        redrawRequests.clear();
                    }

                    screen.refresh();
                }

                //////////////////
                // exit ?
                if( jobs.isEmpty() && widgetRoot.nestedNodes().isEmpty() ){
                    break;
                }

                //////////////////
                // reduce load cpu
                long c_sleep = cycleSleep();
                if( c_sleep>0 ){
                    try{
                        //noinspection BusyWait
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