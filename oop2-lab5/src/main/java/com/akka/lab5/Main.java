package com.akka.lab5;

import akka.actor.typed.ActorSystem;

/**
 * Главный класс приложения.
 * 
 * Демонстрирует:
 * 1. Создание ActorSystem
 * 2. Отправку сообщений через tell()
 * 3. Асинхронную обработку сообщений
 * 4. Гарантию порядка сообщений
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Лабораторная работа №5: Actor Model ===");
        System.out.println();

        // Создаём ActorSystem — это точка входа в мир акторов
        // ActorSystem сам является актором (корневым)
        ActorSystem<Command> system = ActorSystem.create(
                HelloActor.create(),
                "hello-system"
        );

        System.out.println("--- Эксперимент 1: Базовая отправка сообщений ---");
        // Отправляем сообщения асинхронно через tell()
        // Порядок сообщений гарантирован
        system.tell(new SayHello("Alice"));
        system.tell(new SayHello("Bob"));
        system.tell(new SayHello("Charlie"));
        
        // Небольшая пауза, чтобы увидеть результат
        sleep(1000);

        System.out.println();
        System.out.println("--- Эксперимент 2: Отправка 1000 сообщений ---");
        System.out.println("Проверяем, что порядок сохраняется даже при большом количестве сообщений");
        
        // Отправляем 1000 сообщений
        for (int i = 1; i <= 1000; i++) {
            system.tell(new SayHello("User#" + i));
        }
        
        System.out.println("Отправлено 1000 сообщений. Обработка идёт асинхронно...");
        System.out.println("Обратите внимание: нет race condition, порядок строго сохраняется!");
        
        // Даём время на обработку
        sleep(3000);

        System.out.println();
        System.out.println("--- Завершение работы ---");
        
        // Корректное завершение ActorSystem
        system.terminate();
        
        System.out.println();
        System.out.println("=== Ключевые выводы ===");
        System.out.println("✓ Актор обрабатывает сообщения последовательно");
        System.out.println("✓ Порядок сообщений гарантирован");
        System.out.println("✓ Нет блокировок в коде");
        System.out.println("✓ Нет race condition");
        System.out.println("✓ Состояние актора полностью изолировано");
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
