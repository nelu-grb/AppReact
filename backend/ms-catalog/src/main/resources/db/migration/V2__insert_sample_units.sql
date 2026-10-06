-- Unidad 1: Cabaña en la montaña (ID obligatorio para resolver la comunicación actual)
INSERT INTO units (
    unit_id, name, description, address, type, city, 
    availability, rooms, bathrooms, price_per_night, max_occupancy
) VALUES (
    1,
    'Cabaña Vista Los Andes',
    'Acogedora cabaña de madera con terraza privada, tinaja caliente y vista panorámica a la cordillera.',
    'Camino al Volcán 24500',
    'CABANA',
    'Cajón del Maipo',
    true,
    2,
    1,
    85.00,
    4
) ON CONFLICT (unit_id) DO NOTHING;

-- Unidad 2: Departamento urbano ejecutivo
INSERT INTO units (
    unit_id, name, description, address, type, city, 
    availability, rooms, bathrooms, price_per_night, max_occupancy
) VALUES (
    2,
    'Apartamento Condominio Lomas',
    'Moderno apartamento completamente equipado en sector residencial con piscina, quincho y excelente conectividad.',
    'Av. Las Condes 10320, Dpto 502',
    'APARTAMENTO',
    'Santiago',
    true,
    1,
    1,
    65.00,
    2
) ON CONFLICT (unit_id) DO NOTHING;

-- Unidad 3: Refugio de lujo frente al lago
INSERT INTO units (
    unit_id, name, description, address, type, city, 
    availability, rooms, bathrooms, price_per_night, max_occupancy
) VALUES (
    3,
    'Refugio del Lago Pucón',
    'Exclusiva cabaña a orillas del lago Villarrica con muelle propio, chimenea a leña y amplio jardín nativo.',
    'Camino Pucón a Villarrica Km 7',
    'CABANA',
    'Pucón',
    true,
    3,
    2,
    140.00,
    6
) ON CONFLICT (unit_id) DO NOTHING;

-- Unidad 4: Loft estilo desértico
INSERT INTO units (
    unit_id, name, description, address, type, city, 
    availability, rooms, bathrooms, price_per_night, max_occupancy
) VALUES (
    4,
    'Loft Atacameño Sol del Desierto',
    'Rústico y elegante loft construido en barro y piedra, ideal para parejas y observación astronómica.',
    'Calle Ayuquira 145',
    'SUITE',
    'San Pedro de Atacama',
    true,
    1,
    1,
    110.00,
    2
) ON CONFLICT (unit_id) DO NOTHING;

-- Unidad 5: Casona familiar en el sur
INSERT INTO units (
    unit_id, name, description, address, type, city, 
    availability, rooms, bathrooms, price_per_night, max_occupancy
) VALUES (
    5,
    'Cabaña Germanica Puerto Varas',
    'Amplia residencia de arquitectura sureña con vista directa al volcán Osorno, estacionamiento y calefacción central.',
    'Vicente Pérez Rosales 890',
    'CABANA',
    'Puerto Varas',
    true,
    4,
    3,
    190.00,
    8
) ON CONFLICT (unit_id) DO NOTHING;

-- Actualiza la secuencia de PostgreSQL para que la creación automática de nuevos registros comience en el ID 6
SELECT setval(pg_get_serial_sequence('units', 'unit_id'), COALESCE(MAX(unit_id), 1)) FROM units;