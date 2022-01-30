package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import org.checkerframework.checker.nullness.qual.NonNull;

class MyWCycle extends WidgetCycle {
    protected MyWCycle( @NonNull Terminal terminal ){
        super(terminal);
    }
    protected MyWCycle( @NonNull Terminal terminal, long threadId ){
        super(terminal, threadId);
    }

    @Override
    protected void processInput( KeyStroke ks ){
        System.out.println(ks);
        if( ks!=null ){
            if( ks.getKeyType() == KeyType.Escape ){
                throw new Error("end processing");
            }else if( ks.getKeyType()==KeyType.Tab ){
                switchNextFocus();
                return;
            }
        }
        super.processInput(ks);
    }
}
