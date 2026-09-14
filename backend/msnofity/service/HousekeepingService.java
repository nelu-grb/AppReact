package com.react.backend.msnofity.service;

import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HousekeepingService {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingService.class);

    public void createTicket(HousekeepingTicketEvent event) {
        log.info("Creando TICKET -> Reserva: {} | Habitación: {} | Tarea: {}", 
                event.reservationId(), event.roomId(), event.taskType());
    }
}