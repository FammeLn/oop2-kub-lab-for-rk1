package com.akka.lab6;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Главный класс для демонстрации взаимодействия акторов.
 * 
 * Демонстрирует:
 * - Создание ActorSystem с корневым поведением
 * - Spawn дочерних акторов через context
 * - Передачу ActorRef между акторами
 * - Request-Response паттерн без блокировок
 * - Асинхронное взаимодействие через сообщения
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Лабораторная работа №6: Взаимодействие акторов ===");
        System.out.println();

        // Создаём ActorSystem с корневым поведением
        ActorSystem<Void> system = ActorSystem.create(
            Behaviors.setup(context -> {
                System.out.println("[System] Инициализация системы акторов...");
                System.out.println();

                // Создаём ServiceActor как дочерний актор
                // context.spawn() возвращает ActorRef<Request>
                var service = context.spawn(
                    ServiceActor.create(),
                    "service"
                );
                System.out.println("[System] ServiceActor создан");

                // Создаём ClientActor как дочерний актор
                // context.spawn() возвращает ActorRef<Response>
                var client = context.spawn(
                    ClientActor.create(),
                    "client"
                );
                System.out.println("[System] ClientActor создан");
                System.out.println();

                // --- Эксперимент 1: Базовое взаимодействие ---
                System.out.println("--- Эксперимент 1: Одиночный запрос ---");
                
                // Отправляем запрос от клиента к сервису
                // Передаём ActorRef клиента в поле replyTo
                service.tell(new Request("Hello from Client", client));
                
                // Пауза для наблюдения результата
                sleep(1000);
                System.out.println();

                // --- Эксперимент 2: Множественные запросы ---
                System.out.println("--- Эксперимент 2: Множественные запросы ---");
                
                // Отправляем несколько запросов подряд
                for (int i = 1; i <= 5; i++) {
                    service.tell(new Request("Request #" + i, client));
                }
                
                sleep(2000);
                System.out.println();

                // --- Эксперимент 3: Демонстрация изоляции ---
                System.out.println("--- Эксперимент 3: Создание второго клиента ---");
                
                // Создаём второй клиент
                var client2 = context.spawn(
                    ClientActor.create(),
                    "client-2"
                );
                
                // Оба клиента отправляют запросы
                service.tell(new Request("From Client 1", client));
                service.tell(new Request("From Client 2", client2));
                service.tell(new Request("Again from Client 1", client));
                
                sleep(2000);
                System.out.println();

                System.out.println("=== Ключевые наблюдения ===");
                System.out.println("✓ Нет возврата значений - только сообщения");
                System.out.println("✓ Нет блокировок - всё асинхронно");
                System.out.println("✓ ActorRef безопасно передаётся в сообщениях");
                System.out.println("✓ Порядок сообщений от одного отправителя гарантирован");
                System.out.println("✓ Акторы полностью изолированы друг от друга");

                // Возвращаем пустое поведение для корневого актора
                // Он просто держит систему активной
                return Behaviors.empty();
            }),
            "interaction-system"
        );

        // Даём время системе поработать
        sleep(1000);

        System.out.println();
        System.out.println("=== Завершение работы ===");
        
        // Корректное завершение ActorSystem
        system.terminate();
    }

    /**
     * Вспомогательный метод для паузы
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
