package com.akka.lab6;

/**
 * Ответ от сервиса клиенту.
 * 
 * Immutable-сообщение, содержащее результат обработки запроса.
 */
class Response {
    final String result;

    Response(String result) {
        this.result = result;
    }
}
