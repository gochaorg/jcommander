package xyz.cofe.jtfm.prop;

public interface Property<A> {
    A get();
    AutoCloseable onChange(PropertyListener<A> ls);
    public static <X> SimpleProperty<X> writeable(X initial){ return new SimpleProperty<>(initial); }
    public static <X> ComputedProperty<X> computable(Compute<X> compute){ return ComputedProperty.capture(compute); }
}
