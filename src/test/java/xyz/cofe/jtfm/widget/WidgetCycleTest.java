package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer;
import xyz.cofe.jtfm.gr.Rect;

import java.io.IOException;

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
        w2.rect().set(Rect.of(2,9,20, 3));
        wc.addWidget(w2);

        var w3 = new Widget1Test();
        w3.setText("three");
        w3.rect().set(Rect.of(2,2,16,3));
        w2.getNestedWidgets().add(w3);

        for( var wid : WidgetsWalk.visibleTree(wc.widgets()).go() ){
            System.out.println(".. "+wid.getNode());
        }

        System.out.println("-".repeat(40));

        System.out.println("w3 "+w3.getText()
            +" next: "+WidgetCycle.findNextVisible(w3).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
            +" prev: "+WidgetCycle.findPrevVisible(w3).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
        );

        System.out.println("w1 "+w1.getText()
            +" next: "+WidgetCycle.findNextVisible(w1).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
            +" prev: "+WidgetCycle.findPrevVisible(w1).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
        );

        System.out.println("w2 "+w2.getText()
            +" next: "+WidgetCycle.findNextVisible(w2).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
            +" prev: "+WidgetCycle.findPrevVisible(w2).map(w -> ((Widget1Test)w).getText() )
        );

        System.out.println("w3 "+w3.getText()
            +" next: "+WidgetCycle.findNextVisible(w3).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
            +" prev: "+WidgetCycle.findPrevVisible(w3).map(w -> w instanceof Widget1Test ? ((Widget1Test)w).getText() : "?" )
        );

        try{
            wc.run();
        } catch( Error err ){
            if( !"end processing".equals(err.getMessage()) ){
                err.printStackTrace();
            }
        }
    }
}
