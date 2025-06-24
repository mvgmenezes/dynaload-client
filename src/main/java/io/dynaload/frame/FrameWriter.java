package io.dynaload.frame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static io.dynaload.util.DynaloadCodes.MAGIC_HEADER;

public class FrameWriter {

    public static void writeFrame(OutputStream out, Frame frame) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeShort(MAGIC_HEADER);
        dataOut.writeInt(frame.requestId);
        dataOut.writeByte(frame.opCode);
        dataOut.writeInt(frame.payload.length);
        dataOut.write(frame.payload);
        dataOut.flush();
    }
}