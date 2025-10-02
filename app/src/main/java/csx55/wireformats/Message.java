package csx55.wireformats;

import java.io.*;
import java.util.*;

public class Message implements Event {
    private int type;
    private int dataMessage; // data that passes through nodes
    private int index; // position in that routing path
    public String[] routingPath; // list of nodes

    /**
     * created each time a new essage is created in a routing path
     * 
     * @param dataMessage
     * @param index
     * @param routingPath
     */
    public Message(int dataMessage, int index, String[] routingPath) {
        this.type = Protocol.MESSAGE;
        this.dataMessage = dataMessage;
        this.index = index;
        this.routingPath = routingPath;
    }

    /**
     * message form a byte array
     * @param data
     * @throws IOException
     */
    public Message(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        this.type = datain.readInt();
        this.dataMessage = datain.readInt();
        this.index = datain.readInt();

        int routingPathLength = datain.readInt();
        this.routingPath = new String[routingPathLength];

        for(int i = 0; i < routingPathLength; i++){
            int length = datain.readInt();
            byte [] bytes = new byte[length];
            datain.readFully(bytes);
            this.routingPath[i] = new String(bytes);
        }

    }

    public void moveAdj() {
        this.index++;
    }

    public int getDataMessage() {
        return dataMessage;
    }

    public int index() {
        return index;
    }

    @Override
    public int getType() {
        return type;
    }

    /**
     * conveting into btyes
     */
    @Override
    public byte[] getBytes() throws IOException {
        try(ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout))){

        dataout.writeInt(type);
        dataout.writeInt(dataMessage);
        dataout.writeInt(index);
        dataout.writeInt(routingPath.length);

        for(String node : routingPath){
            byte[] bytes = node.getBytes();
            dataout.writeInt(bytes.length);
            dataout.write(bytes);
        }

        dataout.flush();
        return byteout.toByteArray();
    }
    
    }
    

    @Override
    public String toString() {
        return "Message type" + type + ", dataMessage" + dataMessage + ", index" + index + ", routhing path" + Arrays.toString(routingPath);
    }

}
