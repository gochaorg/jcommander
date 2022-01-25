package xyz.cofe.jcommander;

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
    public default A previous(){ return a(); }

    /**
     * Возвращает текущее значение
     * @return текущее значение
     */
    public default B current(){ return b(); }

    /**
     * Конструктор
     * @param from предыдущее значение
     * @param to текущее значение
     * @param <A> предыдущее значение
     * @param <B> текущее значение
     * @return экземпляр
     */
    public static <A,B> Change<A,B> fromTo( A from, B to ){
        return new Change<A,B>() {
            @Override
            public A a(){
                return from;
            }

            @Override
            public B b(){
                return to;
            }
        };
    }
}
