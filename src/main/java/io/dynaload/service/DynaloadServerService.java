package io.dynaload.service;

import io.dynaload.frame.Frame;
import io.dynaload.frame.FrameReader;
import io.dynaload.frame.FrameWriter;
import io.dynaload.loader.DynamicClassLoader;
import io.dynaload.loader.JarLoader;
import io.dynaload.loader.JarPackager;
import io.dynaload.util.ClassWriterUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static io.dynaload.util.DynaloadOpCodes.*;

public class DynaloadServerService {

    private final static String BASE_DIR = "build/dynaload";
    private JarLoader jarLoader = null;

    public String fetchAndSaveClass(String key, DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        // Prepara o payload com o path da classe
        ByteArrayOutputStream payloadBuffer = new ByteArrayOutputStream();
        try (DataOutputStream payloadOut = new DataOutputStream(payloadBuffer)) {
            payloadOut.writeUTF(key);
        }

        // Envia o frame com opCode GET_CLASS
        FrameWriter.writeFrame(sendServer, new Frame(0, GET_CLASS, payloadBuffer.toByteArray()));

        // Lê a resposta
        Frame response = FrameReader.readFrame(receivedServer);
        if (response == null || response.opCode != GET_CLASS_RESPONSE) {
            throw new IllegalStateException("Invalid GET_CLASS response");
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.payload));
        String className = in.readUTF();
        int size = in.readInt();

        if (size < 0) {
            throw new IllegalStateException("[Dynaload] Server returned invalid class size for " + key);
        }

        byte[] bytecode = new byte[size];
        in.readFully(bytecode);

        // Salva em disco
        if (ClassWriterUtil.saveClassToFile(className, bytecode, BASE_DIR)) {
            JarPackager.generatePackage(BASE_DIR);
            File jarFile = new File("build/dynaload-models.jar");
            jarLoader = new JarLoader(jarFile);
            return className;
        }
        return null;
    }

    public Class<?> loadClassFromJar(String className) throws Exception {
        //jarLoader = new JarLoader(new File("build/dynaload-models.jar"));
        return jarLoader.load(className);
    }

    public Class<?> loadAndValidate(byte[] bytecode, String className) {
        try {
            DynamicClassLoader loader = new DynamicClassLoader();
            Class<?> clazz = loader.defineClassFromBytes(className, bytecode);

            // Validação leve
            if (clazz.isInterface() || clazz.isAnnotation() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum()) {
                return clazz;
            }
            validateClass(clazz);

            return clazz;
        } catch (NoClassDefFoundError e) {
            System.err.println("[Dynaload Client] - Error to load class " + className + ": " + e.getMessage());
            return null;
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
        FrameWriter.writeFrame(sendServer, new Frame(0, LIST_CLASSES, new byte[0])); // opcode 0x01 = LIST_CLASSES
        Frame response = FrameReader.readFrame(receivedServer);
        if (response == null || response.opCode != LIST_CLASSES_RESPONSE) throw new IllegalStateException("Invalid LIST_CLASSES response");

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.payload));
        int count = in.readInt();
        List<String> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(in.readUTF());
        }
        return results;
    }

    /**
     * Envia um PING e espera um PONG como resposta.
     */
    public boolean ping(DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {
        FrameWriter.writeFrame(sendServer, new Frame(0, PING, new byte[0]));
        Frame response = FrameReader.readFrame(receivedServer);
        if (response == null || response.opCode != PONG) {
            System.err.println("[Dynaload] Invalid PING response");
            return false;
        }

        return true;
    }

    /**
     * Envia um CLOSE e espera um CLOSED_RESPONSE como resposta.
     */
    public boolean closeConection(DataInputStream receivedServer, DataOutputStream sendServer) throws Exception {

        FrameWriter.writeFrame(sendServer, new Frame(0, CLOSE, new byte[0])); // Ex: opcode 0x04 = CLOSE
        Frame response = FrameReader.readFrame(receivedServer);
        if (response == null || response.opCode != CLOSED_RESPONSE) {
            System.out.println("[Dynaload] Session closed by client");
        }
        return true;
    }

    public static void validateClass(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Method method : clazz.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())
                        && method.getParameterCount() == 0
                        && method.getReturnType() != void.class) {
                    method.setAccessible(true);
                    try {
                        method.invoke(instance);
                    } catch (Throwable e) {
                        System.err.println("[Dynaload] Validation Class error: " + method.getName());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[Dynaload] Validation Class error: " + clazz.getName());
            e.printStackTrace();
        }
    }

    public void exportJarToLibs() {
        try {
            Path buildDir = Paths.get("build/dynaload");
            Path outputJar = Paths.get("dynaload/libs/dynaload-models.jar");

            // Garante que o diretório de destino exista
            Files.createDirectories(outputJar.getParent());

            // Remove o JAR antigo, se existir
            Files.deleteIfExists(outputJar);

            try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputJar.toFile()))) {
                Files.walk(buildDir)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String entryName = buildDir.relativize(path).toString().replace("\\", "/");
                                JarEntry entry = new JarEntry(entryName);
                                jarOut.putNextEntry(entry);
                                Files.copy(path, jarOut);
                                jarOut.closeEntry();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }

            System.out.println("[Dynaload] Exported dynaload-models.jar to dynaload/libs/");
        } catch (IOException e) {
            System.err.println("[Dynaload] Failed to export dynaload-models.jar");
            e.printStackTrace();
        }
    }
}