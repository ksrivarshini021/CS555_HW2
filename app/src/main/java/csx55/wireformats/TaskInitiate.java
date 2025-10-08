package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Registry sends messagle to tell each node how many roudns od message sending
 * should be done
 */
public class TaskInitiate implements Event{
    private final int type;
    private int numRounds;

    public TaskInitiate(int rounds) {
        this.type = Protocol.TASK_INITIATE;
        this.numRounds = rounds;
    }

    public TaskInitiate(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        /**
         * reads first 4 bytes of message type
         * reads next 4 byte for number of rounds
         */
        this.type = datain.readInt();
        this.numRounds = datain.readInt();

        datain.close();
        bytein.close();
    }

    public int getNumRounds() {
        return numRounds;
    }

    public int getType() {
        return type;
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(type);
        dataout.writeInt(numRounds);
        dataout.flush();

        byte[] marshalledBytes = byteout.toByteArray();

        dataout.close();
        byteout.close();
        return marshalledBytes;
    }

    @Override
    public String toString() {
        return Integer.toString(type) + " " + Integer.toString(numRounds);
    }

}
