package com.akka.lab7;

import akka.actor.typed.ActorSystem;

/**
 * Главный класс для демонстрации управления состоянием в акторах.
 * 
 * Демонстрирует:
 * - Корректное хранение состояния (не в полях!)
 * - Behavior switching при изменении состояния
 * - Immutable state
 * - Актор как конечный автомат (FSM)
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Лабораторная работа №7: Управление состоянием ===");
        System.out.println();
        System.out.println("Демонстрация:");
        System.out.println("✓ Состояние как параметр поведения (не в полях!)");
        System.out.println("✓ Behavior switching");
        System.out.println("✓ Immutable state");
        System.out.println("✓ Актор = State Machine");
        System.out.println();

        // Создаём ActorSystem с CounterActor
        ActorSystem<Command> counter = ActorSystem.create(
            CounterActor.create(),
            "counter-system"
        );

        // --- Задание 3: Проверка поведения ---
        
        System.out.println("--- Эксперимент 1: Инкрементирование ---");
        // Отправляем несколько Increment
        counter.tell(new Increment());
        counter.tell(new Increment());
        counter.tell(new Increment());
        
        sleep(500);
        System.out.println();

        System.out.println("--- Эксперимент 2: Проверка значения ---");
        // Проверяем текущее значение
        counter.tell(new PrintValue());
        
        sleep(500);
        System.out.println();

        System.out.println("--- Эксперимент 3: Добавление произвольного значения ---");
        // Отправляем Add(10)
        counter.tell(new Add(10));
        counter.tell(new PrintValue());
        
        sleep(500);
        System.out.println();

        System.out.println("--- Эксперимент 4: Ещё операции ---");
        counter.tell(new Increment());
        counter.tell(new Increment());
        counter.tell(new Add(5));
        counter.tell(new PrintValue());
        
        sleep(500);
        System.out.println();

        System.out.println("--- Эксперимент 5: Сброс ---");
        // Отправляем Reset
        counter.tell(new Reset());
        
        sleep(500);
        System.out.println();

        System.out.println("--- Эксперимент 6: Проверка после сброса ---");
        // Снова PrintValue - должно быть 0
        counter.tell(new PrintValue());
        
        sleep(500);
        System.out.println();

        System.out.println("--- Эксперимент 7: Работа после сброса ---");
        counter.tell(new Increment());
        counter.tell(new Add(7));
        counter.tell(new PrintValue());
        
        sleep(1000);
        System.out.println();

        System.out.println("=== Ключевые выводы ===");
        System.out.println("✓ Состояние НЕ хранится в полях актора");
        System.out.println("✓ Состояние передаётся как параметр поведения");
        System.out.println("✓ Каждое изменение → новый CounterState");
        System.out.println("✓ behavior(newState) = behavior switching");
        System.out.println("✓ Immutable state гарантирует безопасность");
        System.out.println("✓ Актор работает как конечный автомат");
        System.out.println("✓ Это подготовка к Akka Persistence");
        System.out.println();

        // Корректное завершение
        System.out.println("=== Завершение работы ===");
        counter.terminate();
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
