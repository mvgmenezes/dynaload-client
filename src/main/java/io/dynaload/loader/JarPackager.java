package io.dynaload.loader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.*;

public class JarPackager {

    public static void generatePackage(String baseDir) throws IOException {
        List<File> classes = listAllClassFilesRecursive(new File(baseDir));
        createJar(classes.toArray(new File[0]), baseDir, new File(baseDir +"-models.jar"));
    }

    private static void createJar(File[] classFiles, String baseDirPath, File outputJar) throws IOException {
        try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputJar))) {
            for (File classFile : classFiles) {

                String entryName = classFile.getCanonicalPath()
                        .replace(new File(baseDirPath).getCanonicalPath() + File.separator, "")
                        .replace(File.separatorChar, '/');

                jarOut.putNextEntry(new JarEntry(entryName));
                try (FileInputStream in = new FileInputStream(classFile)) {
                    in.transferTo(jarOut);
                }
                jarOut.closeEntry();
            }
        }
    }

    private File[] listAllClassFiles(File dir) {
        return dir.exists() ? dir
                .toPath()
                .toFile()
                .listFiles(f -> f.isFile() && f.getName().endsWith(".class")) : new File[0];
    }

    private static List<File> listAllClassFilesRecursive(File dir) {
        List<File> result = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        result.addAll(listAllClassFilesRecursive(f));
                    } else if (f.getName().endsWith(".class")) {
                        result.add(f);
                    }
                }
            }
        }
        return result;
    }
}
