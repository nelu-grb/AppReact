INSERT INTO audit_events (
    id,
    event_type,
    aggregate_id,
    actor,
    payload,
    timestamp
) VALUES
    (
        1,
        'RESERVATION_CREATED',
        '1',
        'camila.rojas@example.com',
        $json${"reservationId":1,"unitId":1,"status":"CREADA","source":"demo"}$json$,
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
        2,
        'RESERVATION_STATUS_UPDATED',
        '1',
        'admin@example.com',
        $json${"reservationId":1,"unitId":1,"status":"CONFIRMADA","source":"demo"}$json$,
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        3,
        'RESERVATION_CREATED',
        '2',
        'matias.soto@example.com',
        $json${"reservationId":2,"unitId":2,"status":"CREADA","source":"demo"}$json$,
        CURRENT_TIMESTAMP - INTERVAL '20 days'
    ),
    (
        4,
        'RESERVATION_STATUS_UPDATED',
        '2',
        'admin@example.com',
        $json${"reservationId":2,"unitId":2,"status":"CHECKOUT","source":"demo"}$json$,
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    (
        5,
        'RESERVATION_CREATED',
        '3',
        'valentina.perez@example.com',
        $json${"reservationId":3,"unitId":3,"status":"CREADA","source":"demo"}$json$,
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    )
ON CONFLICT (id) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('audit_events', 'id'),
    COALESCE(MAX(id), 1),
    MAX(id) IS NOT NULL
)
FROM audit_events;