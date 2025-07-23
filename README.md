# Dynaload Server

**Dynaload Server** is a lightweight, framework-independent runtime designed to dynamically expose Java classes, interfaces, and methods over sockets. It supports remote bytecode export, method invocation, and discovery via a custom binary protocol.

---

## How It Works

Dynaload Server scans the classpath for the following annotations:

* `@DynaloadExport`: Exports a class for remote bytecode retrieval
* `@DynaloadService` + `@DynaloadCallable`: Exposes methods for remote invocation

The server initializes a custom TCP socket and handles framed binary commands (e.g., `GET_CLASS`, `INVOKE`, `LIST_CLASSES`) using a defined opCode protocol.

---

## Getting Started

### Manual Initialization

```java
public class Main {
    public static void main(String[] args) {
        Dynaload.start(9999, "com.example.package");
    }
}
```

### Automatic Initialization via Annotation

```java
@DynaloadStart(basePackage = "com.example.package", port = 9999)
public class Application {
    public static void main(String[] args) {
        DynaloadAutoBootstrap.init();
    }
}
```

> Note: the server runs in a separate thread named `Dynaload-Server-Thread`.

---

## Supported Annotations

### `@DynaloadExport`

```java
@DynaloadExport(value = "v1/account", includeDependencies = { Address.class })
public class Account {
    ...
}
```

Exports the class for remote retrieval via `GET_CLASS`. Dependencies listed in `includeDependencies` will also be exported.

### `@DynaloadService` + `@DynaloadCallable`

```java
@DynaloadService
public class UserService {

    @DynaloadCallable
    public List<User> getAllUsers() {
        return repository.findAll();
    }
}
```

Exposes the method for remote execution via the `INVOKE` command.

### `@DynaloadStart`

```java
@DynaloadStart(port = 9999, basePackage = "com.myapp")
```

Optional. Used for auto-bootstrapping via `DynaloadAutoBootstrap.init()`.

---

## Frame Structure

| Field        | Type     | Description                        |
| ------------ | -------- | ---------------------------------- |
| Header       | `short`  | Always `0xCAFE`                    |
| Request ID   | `int`    | Identifies the request             |
| OpCode       | `byte`   | Operation type (e.g., `GET_CLASS`) |
| Payload Size | `int`    | Size of the binary payload         |
| Payload      | `byte[]` | Serialized data content            |

### Defined OpCodes

```java
DynaloadOpCodes.GET_CLASS    = 0x01
DynaloadOpCodes.INVOKE       = 0x02
DynaloadOpCodes.LIST_CLASSES = 0x03
DynaloadOpCodes.PING         = 0x04
DynaloadOpCodes.CLOSE        = 0x05
DynaloadOpCodes.ERROR        = 0x7F
```

---

## Internal Components

### `StubInterfaceGenerator`

Automatically generates `@RemoteService` interfaces for any class annotated with `@DynaloadService` and `@DynaloadCallable` methods. Uses ByteBuddy to export them under the `io.dynaload.remote.service` package.

### `ClassExportScanner`

Scans and registers all classes marked with `@DynaloadExport`. Each class is associated with a custom key (e.g., `v1/account`) for later retrieval.

### `CallableScanner`

Discovers all `@DynaloadCallable` methods inside `@DynaloadService` classes and registers them in the `CallableRegistry`.

### `SocketServer`

Multiplexed TCP server that supports multiple operations per socket. Commands like `INVOKE` use persistent connections; others like `GET_CLASS` close the connection after response.

---

## Example: Exporting with Dependencies

```java
@DynaloadExport(value = "v1/user", includeDependencies = { Address.class, Role.class })
public class User implements Serializable {
    ...
}
```

---

## Current Limitations

* No authentication/authorization (future support for API Key)
* Only supports native Java serialization (ObjectInputStream)
* All exported methods must be `public` and `Serializable`
* `INVOKE` and `GET_CLASS` require all dependencies to be exportable or already visible

---

## Debug Tip

Use the `LIST_CLASSES` command to verify if the server has correctly registered the exported classes.

---

## Caution

Avoid calling `Dynaload.start(...)` on the main thread of a Spring Boot application. Use `new Thread(...).start()` or `@PostConstruct` with `@Async` if embedding in a framework.

---

## Recommended Project Structure

```
project-root/
├── src/
│   └── main/java/
│       ├── com/example/model/User.java
│       ├── com/example/service/UserService.java
│       └── com/example/Main.java
├── build/dynaload/
│   └── io/dynaload/remote/service/UserService.class
```

---

## Related Modules

* [Dynaload Client](../dynaload-client)
* [Dynaload Spring Starter](../dynaload-spring-starter) (optional)
* [Dynaload Protocol Spec](../protocol.md)

