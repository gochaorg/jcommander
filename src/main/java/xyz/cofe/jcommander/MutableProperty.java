package xyz.cofe.jcommander;

/**
 * Изменяемое свойство
 * @param <A> Тип данных
 * @param <Self> "Собственный тип"
 */
public interface MutableProperty<A,Self extends Property<A,Self>> extends Property<A,Self> {
    /**
     * Установка значения свойства
     * @param newValue значение
     */
    void set(A newValue);
}
