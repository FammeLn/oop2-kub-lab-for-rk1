package com.akka.lab6;

import akka.actor.typed.ActorRef;

/**
 * Команда запроса к сервису.
 * 
 * Демонстрирует паттерн Request-Response в акторной модели:
 * - text — данные запроса (immutable)
 * - replyTo — ActorRef для отправки ответа
 * 
 * Ключевой момент: передача ActorRef как части сообщения
 * позволяет ServiceActor узнать, куда отправить ответ.
 */
class Request implements Command {
    final String text;
    final ActorRef<Response> replyTo;

    Request(String text, ActorRef<Response> replyTo) {
        this.text = text;
        this.replyTo = replyTo;
    }
}
