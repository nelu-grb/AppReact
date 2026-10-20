<img width="796" height="791" alt="image1" src="https://github.com/user-attachments/assets/a88299d6-cb30-44e2-a26d-8afda122718a" /># Pruebas de verificación

Evidencia de las pruebas de seguridad de la API, el funcionamiento de RabbitMQ y las operaciones de administración.

## Seguridad y reglas de negocio

**Sin token** — La solicitud protegida es rechazada.

![Sin token]<img width="835" height="98" alt="image5" src="https://github.com/user-attachments/assets/d38f15c3-7fa6-41e8-b996-9c88c5e67de4" />


**Token válido con rol autorizado** — La solicitud es procesada correctamente.

![Token válido]<img width="913" height="320" alt="image6" src="https://github.com/user-attachments/assets/81b0dab7-a123-464e-aa79-fed03c6aaee6" />


**Regla de negocio inválida** — La operación no se ejecuta y no se publica ningún mensaje.

![Regla inválida]<img width="739" height="213" alt="image4" src="https://github.com/user-attachments/assets/37c07395-371b-462e-9d4e-31ac0b0000f3" />


## RabbitMQ

**Estado del clúster** — `rabbitmqctl cluster_status` o el Management UI muestran los dos nodos en ejecución.

![Estado del clúster]<img width="893" height="240" alt="image2" src="https://github.com/user-attachments/assets/2e546442-a942-4b6d-893d-590704b71b3f" />


**Publicación y consumo normal en cada una de las tres colas** — El mensaje es consumido y confirmado con ACK; la cola queda sin mensajes pendientes.

![Colas tras la primera reserva]<img width="636" height="67" alt="image7" src="https://github.com/user-attachments/assets/5da42ba8-bd86-4775-95f3-9fdfef2eb11c" />

![Colas tras la segunda reserva]<img width="632" height="57" alt="image11" src="https://github.com/user-attachments/assets/1a89abbe-19ab-4ff3-adb5-cb15d136ed02" />

![Logs del consumo]<img width="1495" height="74" alt="image3" src="https://github.com/user-attachments/assets/6af539d2-640d-4ff2-a2ec-022c48f80dc5" />


**Mensaje con error no recuperable** — El consumidor envía NACK sin requeue; el mensaje llega a la DLQ de su cola y queda registrado en logs.

![NACK y DLQ]<img width="716" height="369" alt="image8" src="https://github.com/user-attachments/assets/0d2b6700-ce85-4868-9315-574585849f93" />


**Política de retención** — Al alcanzar el límite o la expiración definidos, los mensajes se descartan o pasan a la DLQ según la política, sin crecimiento descontrolado.

![Política de retención]<img width="769" height="101" alt="image12" src="https://github.com/user-attachments/assets/9716aba1-e3dc-4ee5-8d12-db379abb86f4" />


## Administración

> El Management UI no es visible en este caso porque el despliegue está en EC2; la verificación se hace por API.

**Crear cola, exchange o binding válidos** — Se crean y la API responde correctamente.

![Crear]<img width="716" height="369" alt="image8" src="https://github.com/user-attachments/assets/b0679a96-2203-4f6c-8706-f2fda395b3bf" />

**Eliminar cola o exchange** — Se eliminan y dejan de aparecer.

![Eliminar]<img width="880" height="451" alt="image9" src="https://github.com/user-attachments/assets/5a10b89a-193a-44b0-8024-8893b7e29c73" />

**Nombre vacío o configuración inválida** — La API responde `400` con un mensaje claro y no se crea nada.

![Configuración inválida]<img width="779" height="345" alt="image10" src="https://github.com/user-attachments/assets/039ae59b-54eb-47f2-9edb-d721f24b950b" />

