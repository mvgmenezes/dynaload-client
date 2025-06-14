package io.dynaload.service;

import io.dynaload.loader.DynamicClassLoader;
import io.dynaload.loader.JarLoader;
import io.dynaload.loader.JarPackager;
import io.dynaload.util.ClassWriterUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DynaloadService {

    private final static String BASE_DIR = "build/dynaload";

    public void fetchAndSaveClass(String key, DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        sendServer.writeUTF("GET_CLASS");
        sendServer.writeUTF(key);

        String className = receivedServer.readUTF();
        int size = receivedServer.readInt();

        if (size < 0) {
            throw new IllegalStateException("[Dynaload] Server returned invalid class size for " + key);
        }

        byte[] bytecode = new byte[size];
        receivedServer.readFully(bytecode);

        // Salva em disco para build/debug
        if(ClassWriterUtil.saveClassToFile(className, bytecode, BASE_DIR)){
            JarPackager.generatePackage(BASE_DIR);
            File jarFile = new File("build/dynaload-models.jar");
            JarLoader jarLoader = new JarLoader(jarFile);
        }

    }

    /**
     * Solicita ao servidor Dynaload o bytecode de uma classe específica e a carrega dinamicamente.
     * Também salva o bytecode em disco.
     */
    public Class<?> fetchAndLoadClass(String key, DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        sendServer.writeUTF("GET_CLASS");
        sendServer.writeUTF(key);

        String className = receivedServer.readUTF();
        int size = receivedServer.readInt();

        if (size < 0) {
            throw new IllegalStateException("[Dynaload] Server returned invalid class size for " + key);
        }

        byte[] bytecode = new byte[size];
        receivedServer.readFully(bytecode);

        // Salva em disco para build/debug
        ClassWriterUtil.saveClassToFile(className, bytecode, BASE_DIR);

        // Carrega via class loader customizado
        DynamicClassLoader loader = new DynamicClassLoader();
        return loader.defineClassFromBytes(className, bytecode);
    }

    /**
     * Solicita ao servidor Dynaload a lista de classes registradas.
     */
    public List<String> listRemoteClasses(DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        sendServer.writeUTF("LIST_CLASSES");

        int count = receivedServer.readInt();
        List<String> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            results.add(receivedServer.readUTF());
        }

        return results;
    }

    /**
     * Envia um PING e espera um PONG como resposta.
     */
    public boolean ping(DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        sendServer.writeUTF("PING");
        String response = receivedServer.readUTF();
        return "PONG".equals(response);
    }

    /**
     * Envia um PING e espera um PONG como resposta.
     */
    public boolean closeConection(DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        sendServer.writeUTF("CLOSE");
        String response = receivedServer.readUTF();
        if ("CLOSED".equals(response)) {
            System.out.println("[Client] Session closed by client");
        }
        return true;
    }
}