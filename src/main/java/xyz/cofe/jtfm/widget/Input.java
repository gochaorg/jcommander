package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.input.KeyStroke;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Input {
    default void input( @NonNull KeyStroke ks ){}
}
