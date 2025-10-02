package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event {
    private int messageType;
    private String messagingNodeHost;
    private int messagingNodePort;

    public TaskComplete(String host, int port) {
        this.messageType = Protocol.TASK_COMPLETE;
        this.messagingNodeHost = host;
        this.messagingNodePort = port;
    }

    public TaskComplete(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));
        this.messageType = datain.readInt();
        int messagingNodeHostLength = datain.readInt();
        byte[] hostBytes = new byte[messagingNodeHostLength];
        datain.readFully(hostBytes);
        this.messagingNodeHost = new String(hostBytes);
        this.messagingNodePort = datain.readInt();
    }

    @Override
    public int getType() {
        return messageType;
    }

    public String getHost() {
        return messagingNodeHost;
    }

    public int getPort() {
        return messagingNodePort;
    }

    @Override
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(messageType);
        byte[] hostBytes = messagingNodeHost.getBytes();
        dataout.writeInt(hostBytes.length);
        dataout.write(hostBytes);
        dataout.writeInt(messagingNodePort);

        dataout.flush();
        byte[] marshalledBytes = byteout.toByteArray();

        dataout.close();

        return marshalledBytes;
    }

    @Override
    public String toString() {
        return "TaskCompelted for" + messagingNodeHost + ":" + messagingNodePort;
    }

}
