package xyz.cofe.jcommander;

import com.googlecode.lanterna.input.KeyStroke;
import org.junit.jupiter.api.Test;

public class KeyStrokeTest {
    @Test
    public void test01(){
        System.out.println(KeyShortcut.from("up"));
        System.out.println(KeyShortcut.from("shift+up"));
        System.out.println(KeyShortcut.from("a"));
        System.out.println(KeyShortcut.from("A"));
        System.out.println(KeyShortcut.from("f1"));
        System.out.println(KeyShortcut.from("ctrl + f1"));
        System.out.println(KeyShortcut.from("ctrl + alt + f2"));
        System.out.println("--------------------");

        System.out.println(
            KeyShortcut.toString(KeyShortcut.from("shift+up"))
        );
    }
}
