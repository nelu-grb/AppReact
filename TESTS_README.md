# Pruebas de verificación

Evidencia de las pruebas de seguridad de la API, el funcionamiento de RabbitMQ y las operaciones de administración.

## Seguridad y reglas de negocio

**Sin token** — La solicitud protegida es rechazada.

![Sin token](img/sin-token.png)

**Token válido con rol autorizado** — La solicitud es procesada correctamente.

![Token válido](img/token-valido.png)

**Regla de negocio inválida** — La operación no se ejecuta y no se publica ningún mensaje.

![Regla inválida](img/regla-invalida.png)

## RabbitMQ

**Estado del clúster** — `rabbitmqctl cluster_status` o el Management UI muestran los dos nodos en ejecución.

![Estado del clúster](img/cluster.png)

**Publicación y consumo normal en cada una de las tres colas** — El mensaje es consumido y confirmado con ACK; la cola queda sin mensajes pendientes.

![Colas tras la primera reserva](img/cola-publicacion-1.png)
![Colas tras la segunda reserva](img/cola-publicacion-2.png)
![Logs del consumo](img/cola-logs.png)

**Mensaje con error no recuperable** — El consumidor envía NACK sin requeue; el mensaje llega a la DLQ de su cola y queda registrado en logs.

![NACK y DLQ](img/nack-dlq.png)

**Política de retención** — Al alcanzar el límite o la expiración definidos, los mensajes se descartan o pasan a la DLQ según la política, sin crecimiento descontrolado.

![Política de retención](img/retencion.png)

## Administración

> El Management UI no es visible en este caso porque el despliegue está en EC2; la verificación se hace por API.

**Crear cola, exchange o binding válidos** — Se crean y la API responde correctamente.

![Crear](img/admin-crear.png)

**Eliminar cola o exchange** — Se eliminan y dejan de aparecer.

![Eliminar](img/admin-eliminar.png)

**Nombre vacío o configuración inválida** — La API responde `400` con un mensaje claro y no se crea nada.

![Configuración inválida](img/admin-invalido.png)
