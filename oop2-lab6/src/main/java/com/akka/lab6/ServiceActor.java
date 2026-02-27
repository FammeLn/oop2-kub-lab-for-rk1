package com.akka.lab6;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Актор-сервис, обрабатывающий запросы.
 * 
 * Демонстрирует ключевые концепции:
 * - Получение запроса с ActorRef для ответа
 * - Обработка данных
 * - Отправка ответа через replyTo (без возврата значения)
 * - Асинхронное взаимодействие без блокировок
 */
class ServiceActor {

    /**
     * Создаёт поведение актора-сервиса.
     * 
     * ServiceActor принимает Request и отправляет Response
     * через ActorRef, указанный в запросе.
     */
    static Behavior<Request> create() {
        return Behaviors.receiveMessage(req -> {
            // Обработка запроса
            String processed = "Processed: " + req.text;
            
            // Отправка ответа клиенту через replyTo
            // Это асинхронная отправка - нет ожидания, нет блокировок
            req.replyTo.tell(new Response(processed));
            
            // Логирование для демонстрации
            System.out.println("[ServiceActor] Обработан запрос: " + req.text);
            
            // Продолжаем работу с тем же поведением
            return Behaviors.same();
        });
    }
}
