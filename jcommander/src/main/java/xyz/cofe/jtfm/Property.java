package xyz.cofe.jtfm;

import xyz.cofe.fn.Consumer3;

/**
 * Свойство объекта
 * @param <A> Тип данных
 * @param <Self> "Собственный тип"
 */
public interface Property<A,Self extends Property<A,Self>> {
    /**
     * Получение значение свойства
     * @return значение свойства
     */
    A get();

    /**
     * Подписка на изменение свойства
     * @param listener подписчик
     * @return отписка от уведомлений
     */
    Runnable listen( Consumer3<Self,A,A> listener );
}
