package xyz.cofe.jcommander;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer;
import xyz.cofe.collection.BasicEventList;
import xyz.cofe.collection.EventList;
import xyz.cofe.fn.Fn1;
import xyz.cofe.io.fs.File;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("nullness")
public class Main {
    public Main(){
        this(new String[]{});
    }

    public Main(String[] args){
        if( args==null )throw new IllegalArgumentException( "args==null" );
        starting = this::startDefault;

        List<String> cargs = new ArrayList<>(List.of(args));

        while( !cargs.isEmpty() ){
            var arg = cargs.remove(0);
            switch( arg ){
                case "-telnet":
                    if( cargs.isEmpty() )throw new IllegalArgumentException("expect port num after -telnet");
                    telnetPort = Integer.parseInt(cargs.remove(0));
                    starting = this::startTelnet;
                    break;
                case "-?":
                case "--help":
                case "-help":
                    help();
                    break;
                case "-v:input":
                    logInput = ks -> {
                        System.out.println("input "+ks+" shift="+ks.isShiftDown()+" alt="+ks.isAltDown()+" ctrl="+ks.isCtrlDown());
                    };
                    break;
                default:
                    System.out.println("undefined option \""+arg+"\"");
                    break;
            }
        }
    }

    private void help(){
        System.out.println("options:");
        System.out.println("  -telnet <port>");
    }

    public static void main(String[] args) {
        new Main(args).start();
    }

    private Runnable starting = this::startDefault;
    private int telnetPort = 4044;
    private Consumer<KeyStroke> logInput = k -> {};

    private void start( Terminal terminal ){
        TerminalScreen screen = null;
        try{
            screen = new TerminalScreen(terminal);
            screen.startScreen();
            screen.setCursorPosition(null);

            var terminalSize = screen.getTerminalSize();

            var g = screen.newTextGraphics();
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.setBackgroundColor(TextColor.ANSI.BLACK);
            g.fillRectangle(TerminalPosition.TOP_LEFT_CORNER,terminalSize,' ');

            var filesTable = filesTable(terminalSize);
            filesTable.render(g);
            screen.refresh();

            terminal.addResizeListener( (term,size) -> {
                filesTable.setRect( Rect.of(0,0, size.getColumns()/2, size.getRows()-1) );
            });

            long start_t0 = System.currentTimeMillis();
            long timeout = 1000L * 20L;

            while( true ){
                long t_diff = System.currentTimeMillis() - start_t0;
                if( t_diff>timeout )break;

                try{
                    KeyStroke ks = screen.pollInput();
                    if( ks != null ){
                        if( logInput!=null )logInput.accept(ks);
                        if( ks.getKeyType() == KeyType.Escape ){
                            break;
                        }

                        filesTable.input(ks);

                        screen.doResizeIfNecessary();

                        g = screen.newTextGraphics();
                        filesTable.render(g);
                        screen.refresh();
                        continue;
                    }
                } catch(SocketTimeoutException ex ){
                    try{
                        Thread.sleep(10);
                        Thread.yield();
                    } catch( InterruptedException e ){
                        e.printStackTrace();
                    }

                    continue;
                }

                try{
                    Thread.sleep(1);
                } catch( InterruptedException e ){
                    e.printStackTrace();
                }

                Thread.yield();
            }
        } catch( IOException e ){
            e.printStackTrace();
        } finally {
            try{
                if( screen!=null ){
                    screen.stopScreen();
                }
            } catch( IOException e ){
                e.printStackTrace();
            }
        }
    }
    private void startDefault(){
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        try{
            Terminal terminal = defaultTerminalFactory.createTerminal();
            start(terminal);
        } catch( IOException e ){
            e.printStackTrace();
        }
    }
    private void startTelnet(){
        try{
            TelnetTerminalServer telnetTerminalServer = new TelnetTerminalServer(telnetPort);
            System.out.println("Listening on port "+telnetPort+", please connect to it with a separate telnet process");

            while(true) {
                final TelnetTerminal telnetTerminal = telnetTerminalServer.acceptConnection();

                telnetTerminal.setMouseCaptureMode(MouseCaptureMode.CLICK);
//                telnetTerminal.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);

                System.out.println("Accepted connection from " + telnetTerminal.getRemoteSocketAddress());
                Thread thread = new Thread(() -> {
                    start(telnetTerminal);
                    try{
                        telnetTerminal.close();
                    } catch( IOException e ){
                        e.printStackTrace();
                    }
                    System.out.println("finish session "+telnetTerminal.getRemoteSocketAddress());
                });
                thread.start();
            }
        } catch( IOException e ){
            throw new RuntimeException("can't start telnet");
        }
    }
    public void start(){
        starting.run();
    }

    private FilesTable filesTable( TerminalSize s ){
        var curDir = new File("/home/uzer/Загрузки");
        if( !curDir.isDir() )curDir = new File(".");

        var filesTable = new DirectoryTable(curDir.path);
        filesTable.setRect( Rect.of(0,0, s.getColumns()/2, s.getRows()) );

        return filesTable;
    }
}
