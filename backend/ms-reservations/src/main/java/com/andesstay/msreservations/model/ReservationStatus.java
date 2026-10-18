package com.andesstay.msreservations.model;

public enum ReservationStatus {
    CREADA,
    CONFIRMADA,
    CHECKIN_PENDIENTE,
    EN_ESTADIA,
    CHECKOUT,
    CANCELADA;

    // define el flujo permitido; checkout y cancelación son estados finales.
    public boolean canTransitionTo(ReservationStatus next) {
        return switch (this) {
            case CREADA -> next == CONFIRMADA || next == CANCELADA;
            case CONFIRMADA -> next == CHECKIN_PENDIENTE
                    || next == EN_ESTADIA
                    || next == CANCELADA;
            case CHECKIN_PENDIENTE -> next == EN_ESTADIA;
            case EN_ESTADIA -> next == CHECKOUT;
            case CHECKOUT, CANCELADA -> false;
        };
    }
}