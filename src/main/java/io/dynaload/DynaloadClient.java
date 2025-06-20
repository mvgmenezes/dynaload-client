package io.dynaload;

import io.dynaload.service.DynaloadService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynaloadClient {
    public static void main(String[] args) throws Exception {
        connect("localhost", 9999);
    }

    public static void connect(String host, int port) throws Exception {
        DynaloadService service = new DynaloadService();
        if (!pingServer(host, port, service)) return;
        List<String> classes = listRemoteClasses(host, port, service);
        if (classes.isEmpty()) {
            System.err.println("[Dynaload] No registered classes found.");
            return;
        }

        Map<String, String> keyToClassName = fetchAndSaveAllClasses(host, port, classes, service);

        exportAndValidateClasses(classes, keyToClassName, service);
    }

    private static boolean pingServer(String host, int port, DynaloadService service) {
        try (Socket socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            boolean ok = service.ping(in, out);
            System.out.println(ok ? "[Dynaload] Server is reachable" : "[Dynaload] No response from server.");
            return ok;
        } catch (Exception e) {
            System.err.println("[Dynaload] Error during PING: " + e.getMessage());
            return false;
        }
    }

    private static List<String> listRemoteClasses(String host, int port, DynaloadService service) throws Exception {
        try (Socket socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            List<String> classes = service.listRemoteClasses(in, out);
            classes.forEach(c -> System.out.println("[Dynaload] Registered: " + c));
            return classes;
        }
    }

    private static Map<String, String> fetchAndSaveAllClasses(String host, int port, List<String> classes, DynaloadService service) throws Exception {
        Map<String, String> keyToClassName = new HashMap<>();
        try (Socket socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            for (String path : classes) {
                String className = service.fetchAndSaveClass(path, in, out);
                if (className != null) {
                    keyToClassName.put(path, className);
                    System.out.println("[Dynaload] Loaded class: " + className);
                }
            }

            service.closeConection(in, out);
        }
        return keyToClassName;
    }

    private static void exportAndValidateClasses(List<String> classes, Map<String, String> keyToClassName, DynaloadService service) {

        service.exportJarToLibs();

        for (String path : classes) {
            String className = keyToClassName.get(path);
            if (className != null) {
                try {
                    Class<?> clazz = service.loadClassFromJar(className);
                    if (clazz.isInterface() || clazz.isAnnotation() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum()) {
                        continue;
                    }
                    DynaloadService.validateClass(clazz);
                } catch (Exception e) {
                    System.err.println("[Dynaload] Validation failed: " + className);
                    e.printStackTrace();
                }
            }
        }
    }
}