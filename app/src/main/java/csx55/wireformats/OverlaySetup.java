package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OverlaySetup implements Event {
    private final int type = Protocol.INITIATE_OVERLAY_SETUP;
    private String nodeHost;
    private int nodePort;
    private List<String> neighbours;
    private int threadCount;

    public OverlaySetup(String host, int port, List<String> neighbours, int threadCount){
        this.nodeHost = host;
        this.nodePort = port;
        this.neighbours = new ArrayList<>(neighbours);
        this.threadCount = threadCount;
        
    }


    public OverlaySetup(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        datain.readInt();
        int length = datain.readInt();
        byte[] hostBytes = new byte[length];
        datain.readFully(hostBytes);
        this.nodeHost = new String(hostBytes);
        this.nodePort = datain.readInt();

        int numNeighbours = datain.readInt();
        this.neighbours = new ArrayList<>();
        for(int i = 0; i < numNeighbours; i++){
            int neighbourLength = datain.readInt();
            byte[] neighbourBytes = new byte[neighbourLength];
            datain.readFully(neighbourBytes);
            this.neighbours.add(new String(neighbourBytes));
        }

        this.threadCount = datain.readInt();

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
        dataout.writeInt(neighbours.size());
        if(neighbours != null){
            for(String neighbour : neighbours){
                byte[] neighbourBytes = neighbour.getBytes();
                dataout.writeInt(neighbourBytes.length);
                dataout.write(neighbourBytes);
            }

        }
        

        dataout.writeInt(threadCount);

        dataout.flush();
        byte[] marshalledBytes = byteout.toByteArray();
        dataout.close();
        return marshalledBytes;

    }

    public int getNodePort(){
        return nodePort;
    }

    public String getNodeHost(){
        return nodeHost;
    }
    public List<String> getNeighbours(){
        return neighbours;
    }
    public int getThreadCount(){
            return threadCount;
    }

    @Override
    public String toString(){
        return "OverlayRingNodes" + nodeHost + ":" + nodePort + "neighbours:" + neighbours + "Thread Count" + threadCount;
    }


}
