package io.dynaload.proxy;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DynaloadSocketClient implements Closeable {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public DynaloadSocketClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public DataInputStream getIn() { return in; }
    public DataOutputStream getOut() { return out; }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
