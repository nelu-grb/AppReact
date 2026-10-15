-- =====================================================================
-- ms-catalog | Database Initialization & Seed Data for UNITS
-- Idempotent: safe to run on every deployment/startup.
-- =====================================================================

-- 1. Create the table first so relations exist
CREATE TABLE IF NOT EXISTS units (
    unit_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    address VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    availability BOOLEAN,
    rooms INT NOT NULL,
    bathrooms INT NOT NULL,
    price_per_night NUMERIC(10, 2) NOT NULL,
    max_occupancy INT NOT NULL
);

-- =====================================================================
-- Seed Data for UNITS
-- Idempotent: each row is only inserted if a unit with the same NAME
-- does not exist yet. Prices are per night in CLP.
-- =====================================================================

-- ---------- HABITACION ----------
INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Cordillera', 'Habitación luminosa con vista a la cordillera de los Andes, cama king y escritorio.', 'Av. Providencia 1234', 'HABITACION', 'Santiago', TRUE, 1, 1, 45000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Cordillera');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Barrio Italia', 'Habitación acogedora a pasos de cafés y tiendas de diseño en Barrio Italia.', 'Av. Italia 1450', 'HABITACION', 'Santiago', TRUE, 1, 1, 38000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Barrio Italia');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Cerro Alegre', 'Habitación con balcón y vista a la bahía en el histórico Cerro Alegre.', 'Pasaje Gálvez 210', 'HABITACION', 'Valparaíso', FALSE, 1, 1, 42000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Cerro Alegre');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Lastarria', 'Habitación pequeña pero cómoda, muy cerca de museos y restaurantes.', 'José Victorino Lastarria 420', 'HABITACION', 'Santiago', TRUE, 1, 1, 55000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Lastarria');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Mercado Central', 'Habitación económica sobre el Mercado Central, ideal para visitas cortas.', 'San Pablo 940', 'HABITACION', 'Santiago', TRUE, 1, 1, 32000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Mercado Central');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Bellavista', 'Habitación luminosa en el barrio bohemio de Bellavista.', 'Pío Nono 120', 'HABITACION', 'Santiago', TRUE, 1, 1, 40000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Bellavista');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Ñuñoa', 'Habitación tranquila en zona residencial, con cocina compartida.', 'Av. Irarrázaval 2350', 'HABITACION', 'Santiago', TRUE, 1, 1, 36000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Ñuñoa');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Paseo Gervasoni', 'Habitación con vista a los cerros de Valparaíso.', 'Paseo Gervasoni 45', 'HABITACION', 'Valparaíso', TRUE, 1, 1, 48000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Paseo Gervasoni');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Playa Ancha', 'Habitación simple cerca de la universidad y miradores.', 'Av. Altamirano 1500', 'HABITACION', 'Valparaíso', TRUE, 1, 1, 34000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Playa Ancha');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Recreo Viña', 'Habitación cómoda a pasos del metro y centros comerciales.', 'Av. Valparaíso 980', 'HABITACION', 'Viña del Mar', TRUE, 1, 1, 41000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Recreo Viña');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Habitación Providencia Terraza', 'Habitación con pequeña terraza y buen acceso a transporte.', 'Av. Providencia 2100', 'HABITACION', 'Santiago', FALSE, 1, 1, 50000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Habitación Providencia Terraza');

-- ---------- SUITE ----------
INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Presidencial Andes', 'Suite de lujo con sala de estar, jacuzzi privado y terraza panorámica.', 'Av. Apoquindo 4500', 'SUITE', 'Santiago', TRUE, 2, 2, 180000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Presidencial Andes');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Ejecutiva Las Condes', 'Suite pensada para viajes de negocios: escritorio amplio, sala de reuniones y desayuno incluido.', 'Av. El Bosque Norte 500', 'SUITE', 'Santiago', TRUE, 1, 1, 95000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Ejecutiva Las Condes');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Vista al Mar', 'Suite frente al océano con tina de hidromasaje y terraza privada.', 'Av. San Martín 600', 'SUITE', 'Viña del Mar', TRUE, 2, 1, 150000.00, 3
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Vista al Mar');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Mirador Santiago', 'Suite con vistas panorámicas a la ciudad y desayuno buffet.', 'Av. Santa María 1800', 'SUITE', 'Santiago', TRUE, 2, 2, 210000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Mirador Santiago');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Lastarria', 'Suite boutique en edificio histórico, ideal para escapadas románticas.', 'José Victorino Lastarria 520', 'SUITE', 'Santiago', TRUE, 1, 1, 105000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Lastarria');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Marina Valparaíso', 'Suite amplia con living, minibar y vista al puerto.', 'Av. Errázuriz 900', 'SUITE', 'Valparaíso', TRUE, 2, 1, 165000.00, 3
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Marina Valparaíso');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Parque Viña', 'Suite con terraza privada frente a jardines y piscina.', 'Av. Marina 1100', 'SUITE', 'Viña del Mar', TRUE, 2, 2, 175000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Parque Viña');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Volcán Pucón', 'Suite familiar con chimenea y vista al volcán Villarrica.', 'Camino Pucón-Villarrica Km 5', 'SUITE', 'Pucón', TRUE, 3, 2, 220000.00, 5
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Volcán Pucón');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Suite Sky Costanera', 'Suite corporativa en torre con acceso a lounge y cowork.', 'Av. Andrés Bello 2425', 'SUITE', 'Santiago', FALSE, 1, 1, 120000.00, 2
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Suite Sky Costanera');

-- ---------- APARTAMENTO ----------
INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Bellas Artes', 'Departamento completamente equipado frente al Parque Forestal, ideal para estadías largas.', 'José Miguel de la Barra 480', 'APARTAMENTO', 'Santiago', TRUE, 2, 1, 70000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Bellas Artes');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Costanera', 'Departamento moderno con cocina completa, gimnasio y piscina en el edificio.', 'Av. Andrés Bello 2425', 'APARTAMENTO', 'Santiago', TRUE, 3, 2, 110000.00, 6
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Costanera');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Playa Reñaca', 'Departamento a una cuadra de la playa, con balcón y estacionamiento.', 'Av. Borgoño 14500', 'APARTAMENTO', 'Viña del Mar', FALSE, 2, 1, 85000.00, 5
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Playa Reñaca');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Moneda', 'Departamento cercano al centro cívico con cocina equipada.', 'Huérfanos 980', 'APARTAMENTO', 'Santiago', TRUE, 2, 1, 75000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Moneda');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Ñuñoa Terraza', 'Departamento con terraza, ideal para grupos pequeños.', 'Av. Grecia 850', 'APARTAMENTO', 'Santiago', TRUE, 2, 1, 68000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Ñuñoa Terraza');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Puente Alto Familiar', 'Departamento amplio con estacionamiento y comercio cercano.', 'Av. Concha y Toro 1200', 'APARTAMENTO', 'Santiago', TRUE, 3, 2, 62000.00, 6
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Puente Alto Familiar');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Puerto Varas', 'Departamento con vista al lago, a pasos del centro.', 'Walker Martínez 320', 'APARTAMENTO', 'Puerto Varas', TRUE, 2, 1, 95000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Puerto Varas');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento La Serena', 'Departamento luminoso a minutos de la playa y el faro.', 'Av. Francisco de Aguirre 700', 'APARTAMENTO', 'La Serena', TRUE, 2, 1, 72000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento La Serena');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Reñaca Vista', 'Departamento con amplio balcón y piscina en el edificio.', 'Av. Borgoño 12000', 'APARTAMENTO', 'Viña del Mar', TRUE, 3, 2, 125000.00, 6
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Reñaca Vista');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Apartamento Pucón Centro', 'Departamento cómodo a pasos del lago y supermercados.', 'Fresia 150', 'APARTAMENTO', 'Pucón', FALSE, 2, 1, 88000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Apartamento Pucón Centro');

-- ---------- CABANA ----------
INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Lago Villarrica', 'Cabaña de madera con vista al lago y volcán, tinaja de leña y quincho.', 'Camino Pucón-Villarrica Km 8', 'CABANA', 'Pucón', TRUE, 3, 2, 120000.00, 6
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Lago Villarrica');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Valle Nevado', 'Cabaña de montaña con chimenea, a minutos de las pistas de ski.', 'Camino a Farellones Km 12', 'CABANA', 'Lo Barnechea', TRUE, 2, 1, 135000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Valle Nevado');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Cajón del Maipo', 'Cabaña rodeada de naturaleza junto al río, perfecta para trekking y rafting.', 'Camino al Volcán 18000', 'CABANA', 'San José de Maipo', TRUE, 2, 1, 80000.00, 5
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Cajón del Maipo');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Bosque Nativo', 'Cabaña rústica entre árboles nativos, con estufa a leña.', 'Camino a Coñaripe Km 6', 'CABANA', 'Pucón', TRUE, 2, 1, 95000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Bosque Nativo');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Lago Llanquihue', 'Cabaña amplia con vista al lago y volcán Osorno.', 'Ruta 225 Km 18', 'CABANA', 'Puerto Varas', TRUE, 3, 2, 150000.00, 6
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Lago Llanquihue');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Atacama', 'Cabaña de adobe con terraza y vista al desierto.', 'Camino a Valle de la Luna 4500', 'CABANA', 'San Pedro de Atacama', TRUE, 2, 1, 110000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Atacama');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Elqui', 'Cabaña luminosa en el valle, ideal para observación de estrellas.', 'Ruta 41 Km 32', 'CABANA', 'La Serena', TRUE, 2, 1, 95000.00, 5
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Elqui');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Termas', 'Cabaña junto a termas naturales con piscina exterior.', 'Camino a Huife Km 14', 'CABANA', 'Pucón', TRUE, 3, 2, 125000.00, 6
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Termas');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Aysén', 'Cabaña remota para pesca y trekking, con quincho cubierto.', 'Ruta 7 Km 210', 'CABANA', 'Coyhaique', TRUE, 2, 1, 130000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Aysén');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Maule', 'Cabaña familiar en zona de viñedos y lagos.', 'Ruta 5 Sur Km 280', 'CABANA', 'Talca', TRUE, 2, 1, 70000.00, 4
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Maule');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Cochamó', 'Cabaña de montaña rodeada de bosque, ideal para escalada.', 'Valle Cochamó', 'CABANA', 'Puerto Varas', FALSE, 2, 1, 115000.00, 5
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Cochamó');

INSERT INTO UNITS (NAME, DESCRIPTION, ADDRESS, TYPE, CITY, AVAILABILITY, ROOMS, BATHROOMS, PRICE_PER_NIGHT, MAX_OCCUPANCY)
SELECT 'Cabaña Mistral', 'Cabaña decorada con detalles locales y patio privado.', 'Av. del Mar 420', 'CABANA', 'La Serena', TRUE, 1, 1, 88000.00, 3
WHERE NOT EXISTS (SELECT 1 FROM UNITS WHERE NAME = 'Cabaña Mistral');