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
import xyz.cofe.io.fs.File;
import xyz.cofe.text.Align;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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

    private Runnable starting;
    private int telnetPort;
    private Consumer<KeyStroke> logInput;

    private void start( Terminal terminal){
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

    private String humanSize( long bytes ){
        String sz = "";
        if( bytes>=(1024*1024*1024) ){
            sz = Long.toString(bytes >> 30)+"g";
        }else if( bytes>=(1024*1024) ){
            sz = Long.toString(bytes >> 20)+"m";
        }else if( bytes>=(1024) ){
            sz = Long.toString(bytes >> 10)+"k";
        }else {
            sz = Long.toString(bytes);
        }

        if( sz.length()<5 ){
            sz = " ".repeat(5-sz.length())+sz;
        }

        return sz;
    }

    private Table<File> filesTable( TerminalSize s ){
        EventList<File> files = new BasicEventList<>();

        Table<xyz.cofe.io.fs.File> filesTable = new Table<>(files);
        Set<String> specialNames = Set.of(".", "..");
        filesTable.columns.append(
            f -> specialNames.contains(f.getName()) ? f.getName() :
                 f.getBasename().length()<1 ? f.getName() :
                     (f.isDir() ? "/" : " ") + f.getBasename(),

            bld -> bld.width(20).minMaxWidth(10,100).name("name")
        );
        var extCol = filesTable.columns.append(
            f -> specialNames.contains(f.getName()) ? "" :
                 f.getBasename().length()<1 ? "" : f.getExtension(),
            bld -> bld.width(10).name("ext")
        );
        filesTable.columns.append(
            f -> !f.isExists() ? -1 :
                humanSize(f.getSize()),
            bld -> bld.width(6).name("size")
        );

        files.add(new File(".."));
//        var exts = List.of("txt","pcx","jpg","js","json","java");
//        for( var i=0;i<200;i++ ){
//            files.add(new File("file_"+i+"."+exts.get(i%exts.size())));
//        }

        var curDir = new File("/home/uzer/Загрузки");
        if( !curDir.isDir() )curDir = new File(".");
        files.addAll(curDir.dirList());

        filesTable.setRect( Rect.of(0,0, s.getColumns()/2, s.getRows()) );
        if( files.size()>0 ) {
            filesTable.setFocused( files.get(0) );
        }

//        filesTable.addFormatter( cf -> {
//            switch( cf.getItem().getExtension() ){
//                case "txt":
//                    cf.setForeground(TextColor.ANSI.BLUE_BRIGHT);
//                    break;
//                case "js":
//                    cf.setForeground(TextColor.ANSI.BLACK);
//                    break;
//                case "json":
//                    cf.setForeground(TextColor.ANSI.GREEN_BRIGHT);
//                    break;
//                case "pcx":
//                    if( cf.getColumn() == extCol )cf.setHalign(Align.Center);
//                    break;
//                case "java":
//                    if( cf.getColumn() == extCol )cf.setHalign(Align.End);
//                    break;
//            }
//        });

        return filesTable;
    }
}
