package com.react.backend.msnofity.exception;

/**
 * Error que NO se arregla reintentando: mensaje mal formado, dato inválido,
 * tipo de notificación no soportado, etc.
 *
 * Al lanzarla, el consumidor omite los reintentos y envía el mensaje
 * directamente a la DLQ (NACK sin requeue).
 */
public class NonRecoverableMessageException extends RuntimeException {

    public NonRecoverableMessageException(String message) {
        super(message);
    }

    public NonRecoverableMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}