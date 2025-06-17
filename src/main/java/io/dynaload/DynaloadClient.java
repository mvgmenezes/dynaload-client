package io.dynaload;

import io.dynaload.service.DynaloadService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynaloadClient {
    public static void main(String[] args) throws Exception {
        connect("localhost", 9999);
    }

    public static void connect(String host, int port) throws Exception {
        // Cria um socket TCP para se conectar ao servidor Dynaload rodando localmente na porta 9999
        try (Socket socket = new Socket(host, port)) {

            // Cria um canal para ENVIAR dados para o servidor (output)
            DataOutputStream sendServer = new DataOutputStream(socket.getOutputStream());

            // Cria um canal para RECEBER dados do servidor (input)
            DataInputStream receivedServer = new DataInputStream(socket.getInputStream());

            DynaloadService service = new DynaloadService();

            // 1. Verifica se o servidor está respondendo
            if (service.ping(receivedServer, sendServer)) {
                System.out.println("[Dynaload] Server is reachable");
            } else {
                System.err.println("[Dynaload] No response from server.");
                return;
            }

            // 2. Lista as classes disponíveis
            List<String> classes = service.listRemoteClasses(receivedServer, sendServer);
            System.out.println("[Dynaload] Registered classes:");
            for (String c : classes) {
                System.out.println(" - " + c);
            }

//            // 3. Carrega uma classe específica
//            if (!classes.isEmpty()) {
//                String key = classes.get(0); // aqui você pode trocar por qualquer path da lista
//                Class<?> clazz = service.fetchAndLoadClass(key, receivedServer, sendServer);
//                Object instance = clazz.getDeclaredConstructor().newInstance();
//                System.out.println("[Client] Loaded class: " + clazz.getName());
//                System.out.println("[Client] Created instance: " + instance);
//            }

//            service.fetchAndSaveClass("v1/account", receivedServer, sendServer);
//            service.fetchAndSaveClass("v1/user", receivedServer, sendServer);
//            service.fetchAndSaveClass("v1/timeutils", receivedServer, sendServer);

            Map<String, String> keyToClassName = new HashMap<>();

            for (String path : classes) {
                String className = service.fetchAndSaveClass(path, receivedServer, sendServer);
                if (className != null) {
                    keyToClassName.put(path, className);
                    System.out.println("[Dynaload] Loaded class: " + className);
                }
                //service.fetchAndSaveClass(path, receivedServer, sendServer);
                //System.out.println("[Dynaload] Loaded class: " + path);
            }




            service.exportJarToLibs();
            // 2. Valida todas as classes carregadas (já no mesmo loader)
            for (String path : classes) {
                String className = keyToClassName.get(path);
                if (className != null) {
                    Class<?> clazz = service.loadClassFromJar(className);
                    DynaloadService.validateClass(clazz);
                }
            }
            service.closeConection(receivedServer, sendServer);
        }
    }

//    // Envia um comando PING e imprime a resposta
//    private static void sendPing(DataOutputStream out, DataInputStream in) throws IOException {
//        out.writeUTF("PING");
//        String response = in.readUTF();
//        System.out.println("[Dynaload Client] Ping response: " + response);
//    }
//
//    // Envia um comando LIST_CLASSES e imprime os caminhos registrados no servidor
//    private static void listAvailableClasses(DataOutputStream out, DataInputStream in) throws IOException {
//        out.writeUTF("LIST_CLASSES");
//        int count = in.readInt();
//        System.out.println("[Dynaload Client] Registered classes: " + count);
//        for (int i = 0; i < count; i++) {
//            String path = in.readUTF();
//            System.out.println(" - " + path);
//        }
//    }
//
//    // Envia um comando GET_CLASS com o path, recebe o bytecode e instancia a classe
//    private static void loadRemoteClass(DataOutputStream out, DataInputStream in, String classPath) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        // Envia o comando para o servidor indicar que deseja carregar uma classe
//        out.writeUTF("GET_CLASS");
//        // Envia o identificador da classe desejada para o servidor
//        out.writeUTF(classPath);
//
//        // Lê o nome qualificado da classe (ex: io.dynaload.model.Account)
//        String className = in.readUTF();
//        if (className.isBlank()) {
//            System.out.println("[Dynaload Client] Error: Empty class name");
//            return;
//        }
//
//        // Lê o tamanho do bytecode
//        int size = in.readInt();
//        if (size < 0) {
//            System.out.println("[Dynaload Client] Error: invalid bytecode size");
//            return;
//        }
//
//        // Lê o bytecode da classe
//        byte[] bytecode = new byte[size];
//        in.readFully(bytecode);
//
//        // Usa um class loader customizado para carregar a classe em runtime a partir do bytecode
//        DynamicClassLoader loader = new DynamicClassLoader();
//        Class<?> clazz = loader.defineClassFromBytes(className, bytecode);
//
//        // Cria uma instância da classe usando seu construtor padrão (sem parâmetros)
//        Object instance = clazz.getDeclaredConstructor().newInstance();
//
//        // Exibe informações sobre a classe e a instância criada
//        System.out.println("[Dynaload Client] Loaded class: " + clazz.getName());
//        System.out.println("[Dynaload Client] Created instance: " + instance);
//    }
}