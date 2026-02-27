package com.akka.lab6;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Актор-клиент, получающий ответы от сервиса.
 * 
 * Демонстрирует:
 * - Получение асинхронных ответов
 * - Обработку результатов без блокировок
 * - Типобезопасность: принимает только Response
 */
class ClientActor {

    /**
     * Создаёт поведение актора-клиента.
     * 
     * ClientActor принимает Response и обрабатывает результаты.
     */
    static Behavior<Response> create() {
        return Behaviors.receiveMessage(res -> {
            // Обработка полученного ответа
            System.out.println("[ClientActor] Получен ответ: " + res.result);
            
            // Продолжаем работу с тем же поведением
            return Behaviors.same();
        });
    }
}
