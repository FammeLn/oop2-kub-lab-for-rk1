package com.akka.lab7;

/**
 * Команда для добавления произвольного значения к счётчику.
 * 
 * Демонстрирует изменение состояния с параметром.
 */
class Add implements Command {
    final int amount;

    Add(int amount) {
        this.amount = amount;
    }
}
