package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event {
    private final int type = Protocol.TASK_COMPLETE;
    private String nodeHost;
    private int nodePort;

    public TaskComplete(String host, int port){
        this.nodeHost = host;
        this.nodePort = port;
    }

    public TaskComplete(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));
        
        datain.readInt();
        int hostLength = datain.readInt();
        byte[] hostBytes = new byte[hostLength];
        datain.readFully(hostBytes);
        this.nodeHost = new String(hostBytes);
        this.nodePort = datain.readInt();

        datain.close();

        
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(type);
        byte[] hostBytes = nodeHost.getBytes();
        dataout.writeInt(hostBytes.length);
        dataout.write(hostBytes);
        dataout.writeInt(nodePort);

        dataout.flush();
        byte[] marshalled = byteout.toByteArray();
        dataout.close();
        return marshalled;
    }

    public String getNodeHost(){
        return nodeHost;
    }

    public int getNodePort(){
        return nodePort;
    }

    public String toString() {
        return nodeHost + ":" + nodePort;
         
    }

    

}
