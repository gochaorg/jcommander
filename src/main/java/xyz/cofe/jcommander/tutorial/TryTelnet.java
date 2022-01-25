package xyz.cofe.jcommander.tutorial;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal;
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class TryTelnet {
    public static void main(String[] args) throws IOException {
        int port = 4024;
        TelnetTerminalServer telnetTerminalServer = new TelnetTerminalServer(port);
        System.out.println("Listening on port "+port+", please connect to it with a separate telnet process");
        //noinspection InfiniteLoopStatement
        while(true) {
            final TelnetTerminal telnetTerminal = telnetTerminalServer.acceptConnection();
            telnetTerminal.setMouseCaptureMode(MouseCaptureMode.CLICK);
            System.out.println("Accepted connection from " + telnetTerminal.getRemoteSocketAddress());
            Thread thread = new Thread(() -> {
                try {
                    runGUI(telnetTerminal);
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
                try {
                    telnetTerminal.close();
                }
                catch(IOException ignore) {}
            });
            thread.start();
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static void runGUI(final TelnetTerminal terminal) throws IOException {
        terminal.enterPrivateMode();
        terminal.clearScreen();
        terminal.setCursorVisible(false);

        final TextGraphics textGraphics = terminal.newTextGraphics();

            /*
            The TextGraphics object keeps its own state of the current colors, separate from the terminal. You can use
            the foreground and background set methods to specify it and it will take effect on all operations until
            further modified.
             */
        textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
            /*
            putString(..) exists in a couple of different flavors but it generally works by taking a string and
            outputting it somewhere in terminal window. Notice that it doesn't take the current position of the text
            cursor when doing this.
             */
        textGraphics.putString(2, 1, "Lanterna Tutorial 2 - Press ESC to exit", SGR.BOLD);
        textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        textGraphics.putString(5, 3, "Terminal Size: ", SGR.BOLD);
        textGraphics.putString(5 + "Terminal Size: ".length(), 3, terminal.getTerminalSize().toString());

            /*
            You still need to flush for changes to become visible
             */
        terminal.flush();

        terminal.addResizeListener((terminal1, newSize) -> {
            // Be careful here though, this is likely running on a separate thread. Lanterna is threadsafe in
            // a best-effort way so while it shouldn't blow up if you call terminal methods on multiple threads,
            // it might have unexpected behavior if you don't do any external synchronization
            textGraphics.drawLine(5, 3, newSize.getColumns() - 1, 3, ' ');
            textGraphics.putString(5, 3, "Terminal Size: ", SGR.BOLD);
            textGraphics.putString(5 + "Terminal Size: ".length(), 3, newSize.toString());
            try {
                terminal1.flush();
            }
            catch(IOException e) {
                // Not much we can do here
                throw new RuntimeException(e);
            }
        });

        textGraphics.putString(5, 4, "Last Keystroke: ", SGR.BOLD);
        textGraphics.putString(5 + "Last Keystroke: ".length(), 4, "<Pending>");
        terminal.flush();

            /*
            Now let's try reading some input. There are two methods for this, pollInput() and readInput(). One is
            blocking (readInput) and one isn't (pollInput), returning null if there was nothing to read.
             */

        while( true ) {
            try{
                KeyStroke keyStroke = terminal.readInput();
            /*
            The KeyStroke class has a couple of different methods for getting details on the particular input that was
            read. Notice that some keys, like CTRL and ALT, cannot be individually distinguished as the standard input
            stream doesn't report these as individual keys. Generally special keys are categorized with a special
            KeyType, while regular alphanumeric and symbol keys are all under KeyType.Character. Notice that tab and
            enter are not considered KeyType.Character but special types (KeyType.Tab and KeyType.Enter respectively)
             */
                while( keyStroke.getKeyType() != KeyType.Escape ) {
                    textGraphics.drawLine(5, 4, terminal.getTerminalSize().getColumns() - 1, 4, ' ');
                    textGraphics.putString(5, 4, "Last Keystroke: ", SGR.BOLD);
                    textGraphics.putString(5 + "Last Keystroke: ".length(), 4, keyStroke.toString());
                    textGraphics.putString(5, 5, "ev: " + keyStroke.getClass().getName());
                    terminal.flush();
                    keyStroke = terminal.readInput();
                }
            } catch( SocketTimeoutException ex ){
                //System.out.println("catch "+ex );
            }
        }
    }
}
