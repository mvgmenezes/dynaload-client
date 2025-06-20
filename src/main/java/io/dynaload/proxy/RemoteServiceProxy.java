package io.dynaload.proxy;

import io.dynaload.frame.Frame;
import io.dynaload.frame.FrameReader;
import io.dynaload.frame.FrameWriter;
import io.dynaload.util.DynaloadOpCodes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RemoteServiceProxy {

    @SuppressWarnings("unchecked")
    public static <T> T createRemoteService(Class<T> serviceInterface, DynaloadSocketClient connection) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                (proxy, method, args) -> {
                    String methodId = generateMethodId(serviceInterface, method); // ex: "userService.getUserName"

                    ByteArrayOutputStream payloadBuffer = new ByteArrayOutputStream();
                    try (ObjectOutputStream out = new ObjectOutputStream(payloadBuffer)) {
                        out.writeUTF(methodId);
                        out.writeInt(args == null ? 0 : args.length);
                        if (args != null) {
                            for (Object arg : args) {
                                out.writeObject(arg);
                            }
                        }
                    }

                    Frame frame = new Frame(1, DynaloadOpCodes.INVOKE, payloadBuffer.toByteArray());
                    FrameWriter.writeFrame(connection.getOut(), frame);

                    Frame response = FrameReader.readFrame(connection.getIn());
                    if (response.opCode != DynaloadOpCodes.INVOKE_RESPONSE) {
                        throw new RuntimeException("Invalid response from Dynaload server.");
                    }

                    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(response.payload))) {
                        String status = in.readUTF();
                        if (!"SUCCESS".equals(status)) {
                            throw new RuntimeException("Invocation failed: " + in.readObject());
                        }
                        return in.readObject();
                    }
                }
        );
    }

    private static String generateMethodId(Class<?> clazz, Method method) {
        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1) + "::" + method.getName();
    }
}
