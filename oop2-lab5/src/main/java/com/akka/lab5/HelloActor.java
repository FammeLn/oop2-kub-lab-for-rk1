package com.akka.lab5;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Простой актор для демонстрации базовых концепций акторной модели.
 * 
 * Ключевые особенности:
 * - Обрабатывает сообщения последовательно (по одному)
 * - Не имеет публичных методов
 * - Взаимодействие только через сообщения
 * - Состояние полностью изолировано
 */
class HelloActor {

    /**
     * Создаёт поведение (Behavior) актора.
     * 
     * Behavior<Command> — актор принимает только сообщения типа Command.
     * 
     * Behaviors.receiveMessage — обработка одного сообщения за раз.
     * 
     * Behaviors.same() — состояние актора не меняется,
     * актор продолжает обрабатывать сообщения с тем же поведением.
     */
    static Behavior<Command> create() {
        return Behaviors.receiveMessage(message -> {

            if (message instanceof SayHello hello) {
                System.out.println("Hello, " + hello.name + "!");
            }

            // Возвращаем то же поведение — актор продолжает работать
            return Behaviors.same();
        });
    }
}
