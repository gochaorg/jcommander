package xyz.cofe.jtfm.widget;

import java.util.Optional;

public interface OnFocusGain {
    void focusGain( Optional<IWidget<?>> previousOwner );
}
