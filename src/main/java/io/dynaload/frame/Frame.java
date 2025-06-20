package io.dynaload.frame;

public class Frame {
    public final int requestId;
    public final byte opCode;
    public final byte[] payload;

    public Frame(int requestId, byte opCode, byte[] payload) {
        this.requestId = requestId;
        this.opCode = opCode;
        this.payload = payload;
    }
}
