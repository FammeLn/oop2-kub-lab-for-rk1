package com.akka.lab5;

import akka.actor.typed.ActorSystem;

/**
 * Дополнительные эксперименты для самостоятельного изучения.
 * 
 * Демонстрирует:
 * - Актор с изменяемым состоянием (счётчик)
 * - Как даже с состоянием не возникает race condition
 * - Функциональный подход к изменению состояния
 */
public class ExperimentsMain {
    public static void main(String[] args) {
        System.out.println("=== Дополнительные эксперименты ===");
        System.out.println();

        // Эксперимент 1: Актор со счётчиком
        System.out.println("--- Эксперимент: Актор с изменяемым состоянием ---");
        System.out.println("Отправляем сообщения актору, который ведёт подсчёт");
        System.out.println();
        
        ActorSystem<Command> counterSystem = ActorSystem.create(
            CounterActor.create(),
            "counter-system"
        );
        
        // Отправляем 20 сообщений
        for (int i = 1; i <= 20; i++) {
            counterSystem.tell(new SayHello("User" + i));
        }
        
        // Даём время на обработку
        sleep(2000);
        
        System.out.println();
        System.out.println("Обратите внимание:");
        System.out.println("- Счётчик увеличивается строго последовательно");
        System.out.println("- Нет пропусков или дублирования номеров");
        System.out.println("- Это работает даже без synchronized/locks!");
        System.out.println();
        
        counterSystem.terminate();
        
        // Эксперимент 2: Множественные экземпляры акторов
        System.out.println("--- Эксперимент: Несколько независимых акторов ---");
        System.out.println();
        
        ActorSystem<Command> system1 = ActorSystem.create(
            CounterActor.create(), "system-1"
        );
        ActorSystem<Command> system2 = ActorSystem.create(
            CounterActor.create(), "system-2"
        );
        
        System.out.println("Отправляем сообщения в два разных актора:");
        
        for (int i = 1; i <= 5; i++) {
            system1.tell(new SayHello("Actor1-User" + i));
            system2.tell(new SayHello("Actor2-User" + i));
        }
        
        sleep(2000);
        
        System.out.println();
        System.out.println("Каждый актор ведёт свой независимый счётчик!");
        System.out.println("Состояние полностью изолировано.");
        System.out.println();
        
        system1.terminate();
        system2.terminate();
        
        System.out.println("=== Ключевые выводы ===");
        System.out.println("✓ Акторы могут иметь изменяемое состояние");
        System.out.println("✓ Состояние безопасно меняется без блокировок");
        System.out.println("✓ Каждый актор полностью изолирован");
        System.out.println("✓ Функциональный подход: возвращаем новое поведение");
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
