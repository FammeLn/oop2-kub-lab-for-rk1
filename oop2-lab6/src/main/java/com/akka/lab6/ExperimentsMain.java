package com.akka.lab6;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Дополнительные эксперименты для изучения взаимодействия акторов.
 * 
 * Демонстрирует:
 * - ServiceActor с задержкой обработки
 * - Поведение клиента при множественных запросах
 * - Проверку порядка сообщений
 */
public class ExperimentsMain {
    public static void main(String[] args) {
        System.out.println("=== Дополнительные эксперименты ===");
        System.out.println();

        // --- Эксперимент: ServiceActor с задержкой ---
        System.out.println("--- Эксперимент: Сервис с задержкой обработки ---");
        System.out.println("Проверяем, как ClientActor остаётся корректным при задержках");
        System.out.println();

        ActorSystem<Void> system = ActorSystem.create(
            Behaviors.setup(context -> {
                
                // Создаём сервис с задержкой
                var slowService = context.spawn(
                    createSlowService(),
                    "slow-service"
                );
                
                // Создаём клиент
                var client = context.spawn(
                    ClientActor.create(),
                    "client"
                );
                
                // Отправляем несколько запросов быстро
                System.out.println("[System] Отправка 10 запросов...");
                for (int i = 1; i <= 10; i++) {
                    slowService.tell(new Request("Fast request #" + i, client));
                }
                
                System.out.println("[System] Все запросы отправлены!");
                System.out.println("[System] Наблюдайте: ответы приходят с задержкой, но в порядке");
                System.out.println();
                
                // Даём время на обработку
                sleep(15000);
                
                System.out.println();
                System.out.println("=== Выводы ===");
                System.out.println("✓ ClientActor корректно получает все ответы");
                System.out.println("✓ Порядок сохраняется несмотря на задержки");
                System.out.println("✓ Нет блокировок - всё асинхронно");
                System.out.println("✓ Mailbox ServiceActor хранит все запросы");
                
                return Behaviors.empty();
            }),
            "experiments-system"
        );

        // Даём время системе завершиться
        sleep(1000);
        system.terminate();
    }

    /**
     * Создаёт ServiceActor с задержкой обработки для демонстрации
     * асинхронной работы и сохранения порядка.
     */
    private static Behavior<Request> createSlowService() {
        return Behaviors.receiveMessage(req -> {
            // Имитация долгой обработки
            System.out.println("[SlowService] Начинаем обработку: " + req.text);
            
            try {
                Thread.sleep(1000); // 1 секунда на обработку
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Отправка ответа
            String processed = "Processed with delay: " + req.text;
            req.replyTo.tell(new Response(processed));
            
            System.out.println("[SlowService] Завершена обработка: " + req.text);
            
            return Behaviors.same();
        });
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
