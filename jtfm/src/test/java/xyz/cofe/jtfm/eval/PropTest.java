package xyz.cofe.jtfm.eval;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class PropTest {
    @Test
    public void directBinding(){
        var a = new SimpleProperty<>(1);
        var b = new SimpleProperty<>(2);
        var c = new SimpleProperty<>(3);

        var x = ComputedProperty
            .with(a)
            .with(b)
            .with(c)
            .build( (q,w,e) -> q*2+w*3+e );

        var cnt = new AtomicInteger(0);

        x.onChange((p)->{
            cnt.incrementAndGet();
            System.out.println("x changed");
        });

        System.out.println("x="+x.get());

        System.out.println("set a");
        a.set(2);

        System.out.println("x="+x.get());
        System.out.println("x="+x.get());

        System.out.println("set b");
        b.set(3);

        System.out.println("set c");
        c.set(4);

        System.out.println("x="+x.get());
        System.out.println("x="+x.get());

        System.out.println("set b");
        b.set(5);

        System.out.println("x="+x.get());
        System.out.println("set c");
        c.set(8);

        System.out.println("x="+x.get());
        System.out.println("x="+x.get());

        System.out.println("cnt = "+cnt);
    }

    @Test
    public void caputeTest(){
        var a = new SimpleProperty<>(1);
        var b = new SimpleProperty<>(2);
        var c = new SimpleProperty<>(3);

        var z = ComputedProperty.capture( ()->a.get()+b.get() );
    }

    @Test
    public void magicBinding(){
        var a = new SimpleProperty<>(1);
        var b = new SimpleProperty<>(2);
        var c = new SimpleProperty<>(3);

        var x = ComputedProperty.capture(()->
            a.get()*2+b.get()*3+c.get()
        );

        var cnt = new AtomicInteger(0);

        x.onChange((p)->{
            cnt.incrementAndGet();
            System.out.println("x changed");
        });

        System.out.println("x="+x.get());

        System.out.println("set a");
        a.set(2);

        System.out.println("x="+x.get());
        System.out.println("x="+x.get());

        System.out.println("set b");
        b.set(3);

        System.out.println("set c");
        c.set(4);

        System.out.println("x="+x.get());
        System.out.println("x="+x.get());

        System.out.println("set b");
        b.set(5);

        System.out.println("x="+x.get());
        System.out.println("set c");
        c.set(8);

        System.out.println("x="+x.get());
        System.out.println("x="+x.get());

        System.out.println("cnt = "+cnt);
    }
}
