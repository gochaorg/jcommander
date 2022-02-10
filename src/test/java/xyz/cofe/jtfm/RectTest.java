package xyz.cofe.jtfm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.cofe.jtfm.gr.Rect;

public class RectTest {
    @Test
    public void include01(){
        var r = Rect.of(2,2,10, 10);

        Assertions.assertTrue(r.include(2,2));
        Assertions.assertTrue(!r.include(12,2));
        Assertions.assertTrue(!r.include(12,12));
        Assertions.assertTrue(!r.include(2,12));

        Assertions.assertTrue(r.include(11,11));
    }
}
