Evaluación I Fullstack III: Caso AndesStay
Repositorio Backend

Contexto
La red vende por Instagram, WhatsApp y un par de Excel distintos. Hay overbooking, el huésped no recibe voucher formal y housekeeping se entera tarde. 

Una red de 20 hostales, cabañas y lodges de turismo de intereses especiales necesita una plataforma unificada para:
Tomar reservas web, administrar disponibilidad de habitaciones/cabañas y coordinar check-in / check-out.
Notificar al huésped (email/push) y a housekeeping (ticket de preparación de habitación).
Generar un panel de operaciones en tiempo real (reservas por hora, tiempo de ciclo, ocupación activa).
Auditar eventos de hospedaje (quién creó, confirmó, hizo check-in o check-out de una reserva).

Arquitectura

Seguridad
MSAL/Azure para proteger rutas y añadir token.
Grupos/Roles: Admin, Recepcionista, Huésped, Auditor.

Frontend
React, hosteado en Azure
Vistas:
Dashboards, Reservas, Catálogo, Reportería, Auditoría

Backend
SpringBoot, Spring Security para autenticación de tokens, hosteado en AWS. Debe existir una AWS API Gateway 

Microservicios: Reservations, Catalog, Notify, Audit, Report, BFF.

Mensajeria
RabbitMQ

Auditoría
Kafka.

