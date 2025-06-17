package io.dynaload.init;

import io.dynaload.DynaloadClient;
import io.dynaload.annotations.DynaloadConnect;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class DynaloadAutoBootstrap {

    public static void connect() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("") // escaneia tudo
                .scan()) {

            Class<?> startClass = scanResult.getClassesWithAnnotation("io.dynaload.annotations.DynaloadConnect")
                    .loadClasses()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (startClass != null) {
                DynaloadConnect annotation = startClass.getAnnotation(DynaloadConnect.class);
                String host = annotation.host();
                int port = annotation.port();
                System.out.println("[Dynaload] Starting Dynaload Server...");
                DynaloadClient.connect(host, port);
            }else{
                System.err.println("[Dynaload] Warning: No class annotated with @DynaloadConnect was found. Dynaload connection will not start.");
            }

        } catch (Exception e) {
            System.err.println("[Dynaload] Failed to initialize Dynaload automatically:");
            e.printStackTrace();
        }
    }
}
