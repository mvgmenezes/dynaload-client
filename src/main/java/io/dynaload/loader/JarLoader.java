package io.dynaload.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader {

    private final URLClassLoader loader;

    public JarLoader(File jarFile) throws Exception {
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("[Dynaload] Jar file not found: " + jarFile.getAbsolutePath());
        }

        this.loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()},
                Thread.currentThread().getContextClassLoader());
    }

    public Class<?> load(String className) throws ClassNotFoundException {
        return loader.loadClass(className);
    }
}
