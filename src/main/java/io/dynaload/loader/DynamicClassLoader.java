package io.dynaload.loader;

public class DynamicClassLoader extends ClassLoader {
    public Class<?> defineClassFromBytes(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}

