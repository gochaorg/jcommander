package xyz.cofe.jtfm;

import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.cofe.fn.Tuple2;

/**
 * Смена значения
 * @param <A> Исходное значение
 * @param <B> текущее значение
 */
public interface Change<A,B> extends Tuple2<A,B> {
    /**
     * Возвращает предыдущее значение
     * @return предыдущее значение
     */
    public default @Nullable A previous(){ return a(); }

    /**
     * Возвращает текущее значение
     * @return текущее значение
     */
    public default @Nullable B current(){ return b(); }

    /**
     * Конструктор
     * @param from предыдущее значение
     * @param to текущее значение
     * @param <A> предыдущее значение
     * @param <B> текущее значение
     * @return экземпляр
     */
    public static <@Nullable A,@Nullable B> Change<A,B> fromTo( @Nullable A from, @Nullable B to ){
        return new Change<A,B>() {
            @Override
            public @Nullable A a(){
                return from;
            }

            @Override
            public @Nullable B b(){
                return to;
            }
        };
    }
}
