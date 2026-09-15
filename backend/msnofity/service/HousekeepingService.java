package com.react.backend.msnofity.service;

import com.react.backend.msnofity.dto.HousekeepingTicketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HousekeepingService {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingService.class);

    // TODO: reemplazar por persistencia real (BD) o llamada a otro microservicio
    // cuando definas dónde vive el ticket de housekeeping.
    public void createTicket(HousekeepingTicketEvent event) {
        log.info("Creando TICKET -> Reserva: {} | Habitación: {} | Tarea: {}",
                event.reservationId(), event.roomId(), event.taskType());
    }
}