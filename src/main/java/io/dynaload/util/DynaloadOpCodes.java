package io.dynaload.util;

public class DynaloadOpCodes {
    public static final byte GET_CLASS = 0x01;
    public static final byte GET_CLASS_RESPONSE = 0x11;
    public static final byte INVOKE = 0x02;
    public static final byte INVOKE_RESPONSE = 0x12;
    public static final byte LIST_CLASSES = 0x03;
    public static final byte LIST_CLASSES_RESPONSE = 0x13;
    public static final byte PING = 0x04;
    public static final byte PONG = 0x14;
    public static final byte CLOSE = 0x05;
    public static final byte CLOSED_RESPONSE = 0x15;
    public static final byte ERROR = 0x7F;
}
