package xyz.cofe.jtfm.eval;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public interface Compute<R> extends Supplier<R>, Serializable {
}
