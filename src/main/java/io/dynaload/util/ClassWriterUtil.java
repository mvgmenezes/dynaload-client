package io.dynaload.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClassWriterUtil {

    private ClassWriterUtil(){}

    /**
     * Salva um arquivo .class no caminho especificado com base no nome da classe.
     * Ex: className "io.dynaload.model.Account" => path build/dynaload/io/dynaload/model/Account.class
     */
    public static boolean saveClassToFile(String className, byte[] bytecode, String baseDir) {
        File outputFile = new File(baseDir + "/" + className.replace('.', '/') + ".class");
        outputFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(bytecode);
        } catch (IOException e) {
            throw new RuntimeException("[Dynaload] Failed to save .class file to disk", e);
        }
        return true;
    }
}

