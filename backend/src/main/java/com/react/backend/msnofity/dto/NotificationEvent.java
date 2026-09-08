package com.react.backend.msnofity.dto;

public record NotificationEvent(
    String type,      // "EMAIL" o "WEBPUSH"
    String recipient, // Correo o token del destinatario
    String subject,   // Asunto del mensaje
    String message    // Contenido del mensaje
) {}