package xyz.cofe.jtfm.eval;

public interface Property<A> {
    A get();
    AutoCloseable onChange(PropertyListener<A> ls);
}
