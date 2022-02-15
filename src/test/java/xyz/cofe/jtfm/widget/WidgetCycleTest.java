package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer;
import xyz.cofe.fn.Tuple;
import xyz.cofe.jtfm.alg.LikeTree;
import xyz.cofe.jtfm.alg.NavTree;
import xyz.cofe.jtfm.gr.Rect;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WidgetCycleTest {
    public static void main(String[] args){
        new WidgetCycleTest().start();
    }

    private int telnetPort = 4044;

    public void start(){
        try{
            TelnetTerminalServer telnetTerminalServer = new TelnetTerminalServer(telnetPort);
            System.out.println("Listening on port "+telnetPort+", please connect to it with a separate telnet process");

            while(true) {
                final TelnetTerminal telnetTerminal = telnetTerminalServer.acceptConnection();
                telnetTerminal.setMouseCaptureMode(MouseCaptureMode.CLICK);

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
            e.printStackTrace();
        }
    }

    private void start( Terminal terminal ){
        WidgetCycle wc = WidgetCycle.create(terminal, MyWCycle::new);

        var w1 = new Widget1Test();
        w1.setText("one");
        w1.rect().set(Rect.of(2,2,20, 3));
        wc.addWidget(w1);

        var w2 = new Widget1Test();
        w2.setText("two");
        w2.rect().set(Rect.of(2,9,20, 3+4));
        wc.addWidget(w2);

        var w3 = new Widget1Test();
        w3.setText("three");
        w3.rect().set(Rect.of(2,2,16,3));
        w2.getNestedWidgets().add(w3);

//        for( var wid : WidgetsWalk.visibleTree(wc.widgets()).go() ){
//            System.out.println(".. "+wid.getNode());
//        }
//        System.out.println("-".repeat(40));

//        var navTree = new NavTree<>(LikeTree.widgetTree());
//
//        List.of(
//            Tuple.of("w2", w2),
//            Tuple.of("w1", w1),
//            Tuple.of("w3", w3)
//        ).forEach(t->{
//            System.out.println("a: "+t.a()+" "+t.b().getText()
//                +" next: "+WidgetCycle.findNextVisible(t.b()).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
//                +" prev: "+WidgetCycle.findPrevVisible(t.b()).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
//            );
//
//            System.out.println("b: "+t.a()+" "+t.b().getText()
//                +" next: "+navTree.next(t.b()).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
//                +" prev: "+navTree.prev(t.b()).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
//            );
//        });

        try{
            wc.run();
        } catch( Error err ){
            if( !"end processing".equals(err.getMessage()) ){
                err.printStackTrace();
            }
        }
    }
}
