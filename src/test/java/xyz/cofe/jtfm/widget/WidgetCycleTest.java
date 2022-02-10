package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer;
import xyz.cofe.jtfm.Rect;

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

        var w = new Widget1Test();
        w.setText("sample one");
        w.rect().set(Rect.of(2,2,20, 3));
        wc.addWidget(w);

        var w2 = new Widget1Test();
        w2.setText("sample two");
        w2.rect().set(Rect.of(2,6,20, 3));
        wc.addWidget(w2);

        wc.run();
    }
}
