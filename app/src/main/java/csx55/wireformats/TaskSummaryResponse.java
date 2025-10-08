package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSummaryResponse implements Event {
    private final int type = Protocol.TRAFFIC_SUMMARY;
    private String nodeHost;
    private int nodePort;
    private int generatedTotalTasks;
    private int pulledTotalTasks;
    private int pushedTotalTasks;
    private int completedTotalTasks;

    public TaskSummaryResponse(String host, int port, UpdateStats stats) {
        this.nodeHost = host;
        this.nodePort = port;
        this.generatedTotalTasks = stats.getGeneratedTasks();
        this.pulledTotalTasks = stats.getPulledTasks();
        this.pushedTotalTasks = stats.getPushedTasks();
        this.completedTotalTasks = stats.getCompletedTasks();

    }

    public TaskSummaryResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        datain.readInt();
        int length = datain.readInt();
        byte[] hostBytes = new byte[length];
        datain.readFully(hostBytes);
        
        this.nodeHost = new String(hostBytes);
        this.nodePort = datain.readInt();
        this.generatedTotalTasks = datain.readInt();
        this.pulledTotalTasks = datain.readInt();
        this.pushedTotalTasks = datain.readInt();
        this.completedTotalTasks = datain.readInt();

        datain.close();
        
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
        dataout.writeInt(generatedTotalTasks);
        dataout.writeInt(pulledTotalTasks);
        dataout.writeInt(pushedTotalTasks);
        dataout.writeInt(pushedTotalTasks);
        dataout.writeInt(completedTotalTasks);

        dataout.flush();
        byte[] marshalledBytes = byteout.toByteArray();

        dataout.close();

        return marshalledBytes;
    }

    /***
     * unmarshalling summary stats
     * 
     * @return
     */
    public TaskSummaryResponse unmarshal(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        datain.readInt();
        int length = datain.readInt();
        byte[] hostBytes = new byte[length];
        datain.readFully(hostBytes);

        this.nodeHost = new String(hostBytes);
        this.nodePort = datain.readInt();
        this.generatedTotalTasks = datain.readInt();
        this.pulledTotalTasks = datain.readInt();
        this.pushedTotalTasks = datain.readInt();
        this.completedTotalTasks = datain.readInt();

        datain.close();
        return this;
    }

    public String getHost() {
        return nodeHost;
    }

    public int getPort() {
        return nodePort;
    }

    @Override
    public int getType() {
        return type;
    }

    public int getGeneratedTasks(){
        return generatedTotalTasks;
    }

    public int getPulledTasks(){
        return pulledTotalTasks;
    }

    public int getPushedTasks(){
        return pushedTotalTasks;
    }

    public int getCompletedTasks(){
        return completedTotalTasks;
    }

    public String toString() {
        return String.format("%1$-20s %2$-25s %3$-25s %4$-25s %5$-25s %6$-25s",
                nodeHost + ":" + nodePort, generatedTotalTasks, pulledTotalTasks, pushedTotalTasks, completedTotalTasks);
    }

}
