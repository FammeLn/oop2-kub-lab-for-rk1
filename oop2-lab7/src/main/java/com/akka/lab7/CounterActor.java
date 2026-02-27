package com.akka.lab7;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Актор-счётчик с корректным управлением состоянием.
 * 
 * КЛЮЧЕВЫЕ КОНЦЕПЦИИ:
 * 
 * 1. НЕТ ПОЛЕЙ ДЛЯ СОСТОЯНИЯ
 *    ❌ private int value = 0;  // Так делать НЕЛЬЗЯ!
 *    
 * 2. СОСТОЯНИЕ = ПАРАМЕТР ПОВЕДЕНИЯ
 *    ✅ Behavior<Command> behavior(CounterState state)
 *    
 * 3. BEHAVIOR SWITCHING
 *    При изменении состояния возвращаем НОВОЕ поведение:
 *    return behavior(newState);
 *    
 * 4. IMMUTABLE STATE
 *    CounterState всегда immutable - нет скрытых мутаций
 *    
 * 5. АКТОР = КОНЕЧНЫЙ АВТОМАТ (FSM)
 *    Состояние → Поведение
 *    Сообщение → Переход
 *    
 * Это фундамент реактивной логики и подготовка к Akka Persistence.
 */
class CounterActor {

    /**
     * Фабричный метод для создания актора.
     * 
     * Создаёт начальное поведение с состоянием = 0.
     */
    static Behavior<Command> create() {
        // Начальное состояние: счётчик = 0
        return behavior(new CounterState(0));
    }

    /**
     * Поведение актора с заданным состоянием.
     * 
     * ВАЖНО: Это private static метод!
     * - Нет полей класса
     * - Состояние передаётся как параметр
     * - Каждый вызов с новым state = новое поведение
     * 
     * Это и есть BEHAVIOR SWITCHING:
     * Возвращая behavior(newState), мы говорим актору:
     * "Следующее сообщение обрабатывай с ЭТИМ состоянием"
     * 
     * @param state текущее состояние актора
     * @return поведение для обработки сообщений
     */
    private static Behavior<Command> behavior(CounterState state) {
        return Behaviors.receiveMessage(msg -> {
            
            // Обработка команды Increment
            if (msg instanceof Increment) {
                // Создаём НОВОЕ состояние
                CounterState newState = state.increment();
                
                System.out.println("[CounterActor] Increment: " + state.value + " -> " + newState.value);
                
                // Возвращаем НОВОЕ поведение с новым состоянием
                // Это BEHAVIOR SWITCHING!
                return behavior(newState);
            }
            
            // Обработка команды Add
            if (msg instanceof Add) {
                Add addCmd = (Add) msg;
                
                // Создаём НОВОЕ состояние
                CounterState newState = state.add(addCmd.amount);
                
                System.out.println("[CounterActor] Add " + addCmd.amount + ": " + 
                                   state.value + " -> " + newState.value);
                
                // Возвращаем НОВОЕ поведение
                return behavior(newState);
            }
            
            // Обработка команды Reset
            if (msg instanceof Reset) {
                // Создаём НОВОЕ состояние (сброс в 0)
                CounterState newState = state.reset();
                
                System.out.println("[CounterActor] Reset: " + state.value + " -> " + newState.value);
                
                // Возвращаем НОВОЕ поведение
                return behavior(newState);
            }
            
            // Обработка команды PrintValue
            if (msg instanceof PrintValue) {
                // Чтение состояния без изменения
                System.out.println("[CounterActor] Current value = " + state.value);
                
                // Возвращаем ТО ЖЕ поведение (состояние не изменилось)
                return Behaviors.same();
            }
            
            // Неизвестная команда - игнорируем
            return Behaviors.same();
        });
    }
}
