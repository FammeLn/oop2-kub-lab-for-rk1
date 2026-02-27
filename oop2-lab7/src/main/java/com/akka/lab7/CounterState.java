package com.akka.lab7;

/**
 * Immutable состояние счётчика.
 * 
 * Ключевые принципы:
 * - Все поля final (неизменяемые)
 * - Нет setter-ов
 * - Методы "изменения" возвращают НОВЫЙ объект
 * - Это не mutable state в поле актора!
 * 
 * Почему это важно:
 * - Состояние явно передаётся как параметр поведения
 * - Каждое изменение создаёт новый объект
 * - Нет скрытых мутаций
 * - Функциональный стиль внутри акторов
 */
class CounterState {
    final int value;

    /**
     * Конструктор создаёт новое состояние с заданным значением.
     */
    CounterState(int value) {
        this.value = value;
    }

    /**
     * "Увеличивает" счётчик на 1.
     * 
     * На самом деле возвращает НОВЫЙ объект CounterState
     * с увеличенным значением. Текущий объект остаётся неизменным.
     * 
     * @return новое состояние с value + 1
     */
    CounterState increment() {
        return new CounterState(value + 1);
    }

    /**
     * "Добавляет" значение к счётчику.
     * 
     * Возвращает НОВЫЙ объект CounterState.
     * 
     * @param amount сколько добавить
     * @return новое состояние с value + amount
     */
    CounterState add(int amount) {
        return new CounterState(value + amount);
    }

    /**
     * "Сбрасывает" счётчик в 0.
     * 
     * Возвращает НОВЫЙ объект CounterState.
     * 
     * @return новое состояние с value = 0
     */
    CounterState reset() {
        return new CounterState(0);
    }

    @Override
    public String toString() {
        return "CounterState{value=" + value + "}";
    }
}
