package com.akka.lab5;

/**
 * Команда для отправки приветствия.
 * 
 * Поля объявлены как final, чтобы обеспечить immutability —
 * это ключевое требование для сообщений в акторной модели.
 */
class SayHello implements Command {
    final String name;

    SayHello(String name) {
        this.name = name;
    }
}
