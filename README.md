# Dynaload Server

O **Dynaload Server** é um runtime leve e independente de framework para expor dinamicamente classes, interfaces e métodos via socket, com suporte a exportação de bytecode, invocação remota e discovery.

---

## 🔧 Como funciona?

O Dynaload Server escaneia seu classpath em busca de:

* **@DynaloadExport**: exporta a classe para download remoto de bytecode
* **@DynaloadService + @DynaloadCallable**: exporta métodos para invocação remota

O servidor cria um socket TCP customizado, com protocolo binário baseado em `Frame`, onde cada comando (GET\_CLASS, INVOKE, LIST\_CLASSES, etc) possui um opCode definido.

---

## 🚀 Iniciando

### Inicialização manual

```java
public class Main {
    public static void main(String[] args) {
        Dynaload.start(9999, "com.exemplo.meupacote");
    }
}
```

### Inicialização automática com anotacão

```java
@DynaloadStart(basePackage = "com.exemplo.meupacote", port = 9999)
public class Application {
    public static void main(String[] args) {
        DynaloadAutoBootstrap.init();
    }
}
```

> Obs: o servidor será executado em uma thread separada chamada `Dynaload-Server-Thread`.

---

## ✨ Anotações Suportadas

### @DynaloadExport

```java
@DynaloadExport(value = "v1/account", includeDependencies = {Endereco.class})
public class Account {
  ...
}
```

Exporta a classe para download remoto via `GET_CLASS`. Também permite declarar dependências que serão exportadas junto.

### @DynaloadService + @DynaloadCallable

```java
@DynaloadService
public class UserService {

    @DynaloadCallable
    public List<User> getAllUsers() {
        return repository.findAll();
    }
}
```

Expõe o método para invocação remota via `INVOKE`.

### @DynaloadStart

```java
@DynaloadStart(port = 9999, basePackage = "com.myapp")
```

Opcional. Usado para inicialização automática via `DynaloadAutoBootstrap.init()`.

---

## 📂 Estrutura dos Frames

| Campo        | Tipo    | Descrição                         |
| ------------ | ------- | --------------------------------- |
| Header       | short   | Sempre `0xCAFE`                   |
| Request ID   | int     | ID da requisição                  |
| OpCode       | byte    | Tipo da operação (ex: GET\_CLASS) |
| Payload Size | int     | Tamanho do payload                |
| Payload      | byte\[] | Dados binários                    |

### OpCodes

```java
DynaloadOpCodes.GET_CLASS          = 0x01
DynaloadOpCodes.INVOKE             = 0x02
DynaloadOpCodes.LIST_CLASSES       = 0x03
DynaloadOpCodes.PING               = 0x04
DynaloadOpCodes.CLOSE              = 0x05
DynaloadOpCodes.ERROR              = 0x7F
```

---

## 🔍 Funcionalidades internas

### StubInterfaceGenerator

Gera interfaces `@RemoteService` automaticamente para qualquer classe com `@DynaloadService` e métodos `@DynaloadCallable` usando ByteBuddy, no pacote `io.dynaload.remote.service`, e exporta via `ClassExportScanner`.

### ClassExportScanner

Escaneia e registra todas as classes anotadas com `@DynaloadExport`. Cada classe pode ser recuperada via chave customizada, ex: `v1/account`.

### CallableScanner

Escaneia métodos `@DynaloadCallable` dentro de `@DynaloadService` e registra na `CallableRegistry` para invocação.

### SocketServer

Servidor TCP com protocolo binário multiplexado. Cada cliente pode realizar múltiplas operações por socket. Comandos como `INVOKE` usam conexão persistente, outros como `GET_CLASS` fecham após resposta.

---

## 🛡️ Exemplo de exportação com dependências

```java
@DynaloadExport(value = "v1/user", includeDependencies = { Address.class, Role.class })
public class User implements Serializable {
  ...
}
```

---

## 🚫 Limitações atuais

* Não há autenticação/autorizacao (futuro: API Key).
* Apenas serialização Java nativa (ObjectInputStream).
* Métodos devem ser públicos e serializáveis.
* Requisições de `INVOKE` e `GET_CLASS` precisam que todas as dependências estejam visíveis ou exportadas.

---

## ⚡ Dica para Debug

Use o comando `LIST_CLASSES` para checar se o servidor registrou corretamente as classes exportadas.

---

## 🚫 Não use

Evite rodar `Dynaload.start(...)` na thread principal de um servidor Spring Boot. Use `new Thread(...).start()` ou `@PostConstruct` com `@Async` se estiver embutido.

---

## 📃 Exemplo de estrutura do projeto

```
project-root/
├── src/
│   └── main/java/
│       ├── com/exemplo/model/User.java
│       ├── com/exemplo/service/UserService.java
│       └── com/exemplo/Main.java
├── build/dynaload/
│   └── io/dynaload/remote/service/UserService.class
```

---

## 🚜 Veja também:

* [Dynaload Client](../dynaload-client)
* [Dynaload Spring Starter](../dynaload-spring-starter) (opcional)
* [Dynaload Protocol Spec](../protocol.md)
