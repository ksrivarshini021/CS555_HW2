package csx55.wireformats;
import java.io.*;
import java.util.*;

public class MessagingNodesList implements Event{
    private List<String> peerConnections;
    private int messageType;

    public MessagingNodesList(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        messageType = datain.readInt();
        if (messageType != Protocol.MESSAGING_NODES_LIST){
            throw new IOException("Invalid message for messaging node list");
        }

        int count = datain.readInt();
        peerConnections = new ArrayList<>(count);

        for (int i = 0; i < count; i++){
            int length = datain.readInt();
            byte[] addBytes = new byte[length];
            datain.readFully(addBytes);
            peerConnections.add(new String(addBytes));

        }
    }

    public MessagingNodesList(List<String> peers){
        this.messageType = Protocol.MESSAGING_NODES_LIST;
        this.peerConnections = new ArrayList<>(peers);
    } 

    public int getType(){
        return Protocol.MESSAGING_NODES_LIST;
    }

    public byte[] getBytes() throws IOException{
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(getType());
        dataout.writeInt(peerConnections.size());

        for (String peer: peerConnections){
            byte[] addressBytes = peer.getBytes();
            dataout.writeInt(addressBytes.length);
            dataout.write(addressBytes);
        }

        dataout.flush();
        byte[] bytes = byteout.toByteArray();
        dataout.close();
        return bytes;
    }

    public List<String> getPeerConnections(){
        return new ArrayList<>(peerConnections);
    }

    public String toString(){
        return "MessagingNodesList" + peerConnections;
    }
}
