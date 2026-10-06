INSERT INTO reservations (
    id,
    unit_id,
    guest_id,
    guest_email,
    start_date,
    end_date,
    total_amount,
    status,
    created_at,
    updated_at
) VALUES
    (
        1, 1, 'demo-guest-001', 'camila.rojas@example.com',
        CURRENT_DATE + 10, CURRENT_DATE + 13,
        255.00, 'CONFIRMADA',
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
        2, 2, 'demo-guest-002', 'matias.soto@example.com',
        CURRENT_DATE - 10, CURRENT_DATE - 7,
        195.00, 'CHECKOUT',
        CURRENT_TIMESTAMP - INTERVAL '20 days',
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    (
        3, 3, 'demo-guest-003', 'valentina.perez@example.com',
        CURRENT_DATE + 20, CURRENT_DATE + 23,
        420.00, 'CREADA',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        4, 4, 'demo-guest-004', 'diego.morales@example.com',
        CURRENT_DATE - 2, CURRENT_DATE + 2,
        440.00, 'EN_ESTADIA',
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        5, 5, 'demo-guest-005', 'sofia.torres@example.com',
        CURRENT_DATE + 4, CURRENT_DATE + 8,
        760.00, 'CANCELADA',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    )
ON CONFLICT (id) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('reservations', 'id'),
    COALESCE(MAX(id), 1),
    MAX(id) IS NOT NULL
)
FROM reservations;