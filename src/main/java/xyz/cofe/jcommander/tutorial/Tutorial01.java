package xyz.cofe.jcommander.tutorial;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class Tutorial01 {
    public static void main(String[] args) throws InterruptedException {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

        Terminal terminal = null;
        try {
            terminal = defaultTerminalFactory.createTerminal();

            /*
            If we got a terminal emulator (probably Swing) then we are currently looking at an empty terminal emulator
            window at this point. If the code ran in another terminal emulator (putty, gnome-terminal, konsole, etc) by
            invoking java manually, there is yet no changes to the content.
             */

            /*
            Let's print some text, this has the same as calling System.out.println("Hello");
             */
            terminal.putCharacter('H');
            terminal.putCharacter('e');
            terminal.putCharacter('l');
            terminal.putCharacter('l');
            terminal.putCharacter('o');
            terminal.putCharacter('\n');
            terminal.flush();
            /*
            Notice the flush() call above; it is necessary to finish off terminal output operations with a call to
            flush() both in the case of native terminal and the bundled terminal emulators. Lanterna's Unix terminal
            doesn't buffer the output by itself but one can assume the underlying I/O layer does. In the case of the
            terminal emulators bundled in lanterna, the flush call will signal a repaint to the underlying UI component.
             */
            Thread.sleep(2000);

            /*
            At this point the cursor should be on start of the next line, immediately after the Hello that was just
            printed. Let's move the cursor to a new position, relative to the current position. Notice we still need to
            call flush() to ensure the change is immediately visible (i.e. the user can see the text cursor moved to the
            new position).
            One thing to notice here is that if you are running this in a 'proper' terminal and the cursor position is
            at the bottom line, it won't actually move the text up. Attempts at setting the cursor position outside the
            terminal bounds are usually rounded to the first/last column/row. If you run into this, please clear the
            terminal content so the cursor is at the top again before running this code.
             */
            TerminalPosition startPosition = terminal.getCursorPosition();
            terminal.setCursorPosition(startPosition.withRelativeColumn(3).withRelativeRow(2));
            terminal.flush();
            Thread.sleep(2000);

            /*
            Let's continue by changing the color of the text printed. This doesn't change any currently existing text,
            it will only take effect on whatever we print after this.
             */
            terminal.setBackgroundColor(TextColor.ANSI.BLUE);
            terminal.setForegroundColor(TextColor.ANSI.YELLOW);

            /*
            Now print text with these new colors
             */
            terminal.putCharacter('Y');
            terminal.putCharacter('e');
            terminal.putCharacter('l');
            terminal.putCharacter('l');
            terminal.putCharacter('o');
            terminal.putCharacter('w');
            terminal.putCharacter(' ');
            terminal.putCharacter('o');
            terminal.putCharacter('n');
            terminal.putCharacter(' ');
            terminal.putCharacter('b');
            terminal.putCharacter('l');
            terminal.putCharacter('u');
            terminal.putCharacter('e');
            terminal.flush();
            Thread.sleep(2000);

            /*
            In addition to colors, most terminals supports some sort of style that can be selectively enabled. The most
            common one is bold mode, which on many terminal implementations (emulators and otherwise) is not actually
            using bold text at all but rather shifts the tint of the foreground color so it stands out a bit. Let's
            print the same text as above in bold mode to compare.
            Notice that startPosition has the same value as when we retrieved it with getTerminalSize(), the
            TerminalPosition class is immutable and calling the with* methods will return a copy. So the following
            setCursorPosition(..) call will put us exactly one row below the previous row.
             */
            terminal.setCursorPosition(startPosition.withRelativeColumn(3).withRelativeRow(3));
            terminal.flush();
            Thread.sleep(2000);
            terminal.enableSGR(SGR.BOLD);
            terminal.putCharacter('Y');
            terminal.putCharacter('e');
            terminal.putCharacter('l');
            terminal.putCharacter('l');
            terminal.putCharacter('o');
            terminal.putCharacter('w');
            terminal.putCharacter(' ');
            terminal.putCharacter('o');
            terminal.putCharacter('n');
            terminal.putCharacter(' ');
            terminal.putCharacter('b');
            terminal.putCharacter('l');
            terminal.putCharacter('u');
            terminal.putCharacter('e');
            terminal.flush();
            Thread.sleep(2000);

            /*
            Ok, that's enough for now. Let's reset colors and SGR modifiers and move down one more line
             */
            terminal.resetColorAndSGR();
            terminal.setCursorPosition(terminal.getCursorPosition().withColumn(0).withRelativeRow(1));
            terminal.putCharacter('D');
            terminal.putCharacter('o');
            terminal.putCharacter('n');
            terminal.putCharacter('e');
            terminal.putCharacter('\n');
            terminal.flush();

            Thread.sleep(2000);

            /*
            Beep and exit
             */
            terminal.bell();
            terminal.flush();
            Thread.sleep(200);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            if(terminal != null) {
                try {
                    /*
                    Closing the terminal doesn't always do something, but if you run the Swing or AWT bundled terminal
                    emulators for example, it will close the window and allow this application to terminate. Calling it
                    on a UnixTerminal will not have any affect.
                     */
                    terminal.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
