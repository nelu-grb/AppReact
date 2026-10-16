package com.andesstay.msreservations.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationStatusTest {

    @Test
    void allowsOnlyTheReservationFlowTransitions() {
        assertTrue(ReservationStatus.CREADA.canTransitionTo(ReservationStatus.CONFIRMADA));
        assertTrue(ReservationStatus.CREADA.canTransitionTo(ReservationStatus.CANCELADA));
        assertTrue(ReservationStatus.CONFIRMADA.canTransitionTo(ReservationStatus.CHECKIN_PENDIENTE));
        assertTrue(ReservationStatus.CONFIRMADA.canTransitionTo(ReservationStatus.EN_ESTADIA));
        assertTrue(ReservationStatus.CONFIRMADA.canTransitionTo(ReservationStatus.CANCELADA));
        assertTrue(ReservationStatus.CHECKIN_PENDIENTE.canTransitionTo(ReservationStatus.EN_ESTADIA));
        assertTrue(ReservationStatus.EN_ESTADIA.canTransitionTo(ReservationStatus.CHECKOUT));
    }

    @Test
    void doesNotAllowTransitionsFromFinalStatesOrBackwards() {
        assertFalse(ReservationStatus.CREADA.canTransitionTo(ReservationStatus.EN_ESTADIA));
        assertFalse(ReservationStatus.CANCELADA.canTransitionTo(ReservationStatus.CONFIRMADA));
        assertFalse(ReservationStatus.CHECKOUT.canTransitionTo(ReservationStatus.EN_ESTADIA));
        assertFalse(ReservationStatus.EN_ESTADIA.canTransitionTo(ReservationStatus.CANCELADA));
    }
}
