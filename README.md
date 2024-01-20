# Magoya Programming Challenge
====================

TODO: Continuar y terminar documentando en el codigo mismo

Este proyecto fue proporcionado por Magoya como un challenge para continuar con la entrevista laboral. El PDF del challenge se encuentra adjuntado.

Utiliza:
- Spring Boot
- EventStore
- PostgreSQL

Se brinda:
- Visor de EventStore
- Swagger
- Script de Collections de Postman
- Smoketest con Apache JMeter

Si tenia tiempo disponible, iba a utilizar MongoDB. Debido a cuestiones personales que me demoraron, opte por PostgreSQL que ya lo tenia instalado en el sistema. Debido al mismo motivo, opte por un smoke test utilizando Apache JMeter sobre los unit tests

## Ejecucion
--------------------------------

### Paso 1

Instalar Docker o Docker Desktop si estas en Windows y dejarlo abierto de fondo. Luego, ejecutar en una consola `docker-compose up` desde la raiz de este proyecto para abrir los containers del servicio de eventos y postgreSQL.
Una vez que termine, deberia poder acceder http://localhost:2113/web/index.html#/dashboard que es el dashboard del servicio de eventos. Aca podras ver los streams de eventos existentes. Deberian haber un par ya desde el vamos que funcionan como la infraestructura para los futuros streams que Spring Boot va a crearle encima.

### Paso 2
En una segunda consola, ejecutar 
- Linux: `./gradlew bootRun`
- Windows: `gradlew bootRun`

Una vez ejecutado, deberia poder acceder el Swagger en http://localhost:8080/swagger-ui/index.html#/

## Explicacion

Se brindan tres endpoints de queries y dos de comandos. El sistema esta organizado como un sistema CQRS. Los comandos estan segregados de los queries, sin embargo, hay un comando que depende de un query. Detallare mas adelante.

Dashboard de Event Store:
![image](https://github.com/Zenyatta11/MagoyaChallenge/assets/94550664/0fdf4745-7221-48c8-9948-1a74068dc3e1)

Stream-viewer
![image](https://github.com/Zenyatta11/MagoyaChallenge/assets/94550664/d60da960-6288-412d-b236-a49249c3bffe)

Eventos de un stream particular (una cuenta bancaria)
![image](https://github.com/Zenyatta11/MagoyaChallenge/assets/94550664/e4e171fb-8f26-4ae4-b038-1015cc3fb019)

### Queries

Los queries se ejecutan en el momento y brindan la ultima version disponible de una entidad. El query de todas las cuentas brindara la ultima version de todas las cuentas que se encuentren disponibles en formato de paginas. El query de una cuenta especifica brindara la ultima version de esa cuenta particular.

Los streams estan organizados por `BankAccountId` o cuenta bancaria. El ID del cliente permite tener varias cuentas bancarias bajo el mismo cliente pero no tiene mas uso que eso. Para ver los streams, utilice el visor de EventStore y vaya a la opcion de Stream Browser para ver todos los streams disponibles. 

### Comandos

Los comandos tienen el siguiente flujo de vida:
- Se recibe un comando y se validan los datos. En el caso de una compra o extraccion, se valida ademas que haya suficiente saldo disponible.
- Si el comando tiene parametros validos, se almacena en el event bus suscrito (BankAccountService.java).

Asincronicamente, se van ejecutando los eventos del event bus suscrito y la verdadera magia ocurre en BankAccountDetailsProjection.java donde se tramitan los eventos en orden de llegada, aumentando el valor de la version.

No esta permitido crear un evento de escritura (comando) con version mas chica a la ultima registrada. Durante la validacion del comando, se recibe y reserva el ultimo puesto. 

## Postman

Se brinda una coleccion, separado con comandos por un lado y queries por el otro. Esta programado de tal manera que se almacenara la ultima cuenta creada y, si llegara a suceder que se manda un comando con versiones viejas, obteniendo el balance se almacenara la ultima version y podra asi continuar haciendo depositos/retiros.
![image](https://github.com/Zenyatta11/MagoyaChallenge/assets/94550664/4b4c9bf1-1044-4503-b82b-3e0be8286861)

## JMeter

Los tests de JMeter son en fila y deberian pasar siempre, independientemente de cuando se ejecutan.
![image](https://github.com/Zenyatta11/MagoyaChallenge/assets/94550664/a59a4cc3-1c63-4216-b9de-d9bbdf7abde2)
