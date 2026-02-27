package com.akka.lab5;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Расширенная версия HelloActor с демонстрацией изменяемого состояния.
 * 
 * Этот актор подсчитывает количество полученных сообщений,
 * демонстрируя, что даже с изменяемым состоянием нет race condition,
 * так как сообщения обрабатываются последовательно.
 */
class CounterActor {
    
    /**
     * Создаёт актор со счётчиком.
     * Начальное значение счётчика = 0.
     */
    static Behavior<Command> create() {
        return counter(0);
    }
    
    /**
     * Поведение актора с текущим значением счётчика.
     * 
     * Каждый раз при получении сообщения возвращается НОВОЕ поведение
     * с обновлённым значением счётчика.
     * 
     * Это демонстрирует функциональный подход к изменению состояния:
     * мы не мутируем переменную, а возвращаем новое поведение.
     */
    private static Behavior<Command> counter(int count) {
        return Behaviors.receiveMessage(message -> {
            
            if (message instanceof SayHello hello) {
                // Увеличиваем счётчик
                int newCount = count + 1;
                
                System.out.println(
                    String.format("[Message #%d] Hello, %s!", newCount, hello.name)
                );
                
                // Возвращаем новое поведение с обновлённым счётчиком
                return counter(newCount);
            }
            
            // Для других типов сообщений просто возвращаем то же поведение
            return Behaviors.same();
        });
    }
}
