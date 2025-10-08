package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RingMessage implements Event {
    private final int type = Protocol.RING_MESSAGE;
    private List<String> ringNodes;
    private String taskNode;
    // private final Map<String,Integer> loadRoute = new HashMap<>();
    private List<Integer> taskCounts;

    public RingMessage(List<String> ringNodes, List<Integer> taskCounts, String taskNode){
        this.ringNodes = new ArrayList<>(ringNodes);
        this.taskCounts = new ArrayList<>(taskCounts);
        this.taskNode = taskNode;
    }

    public String nextNode(String currentNode){
        int index = ringNodes.indexOf(currentNode);
        if(index != -1){
            return ringNodes.get((index + 1) % ringNodes.size());
        } else{
            System.out.println("Node position ndoe found" + currentNode);
            return null;
        }
    }
    public List<Integer> getTaskCounts(){
        return taskCounts;
    }

    public List<String> getRingNodes(){
        return ringNodes;
    }

    public String getTaskNode(){
        return taskNode;
    }

    public void setTaskCount(int index, int count){
        if(index >= 0 && index < taskCounts.size()){
            taskCounts.set(index, count);
        }
    }

    RingMessage (byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        int type = datain.readInt();

        /**
         * read nodes in ring
         */
        int ringSize = datain.readInt();
        ringNodes = new ArrayList<>();
        taskCounts = new ArrayList<>();
        for( int i = 0 ; i < ringSize ; i++){
            int nodeLength = datain.readInt();
            byte[] nodeBytes = new byte[nodeLength];
            datain.readFully(nodeBytes);
            ringNodes.add(new String(nodeBytes));
            int count = datain.readInt();
            taskCounts.add(count);
        }

        /**
         * read taskNode
         */
        int taskNodeLength = datain.readInt();
        byte[] taskNodeBytes = new byte[taskNodeLength];
        datain.readFully(taskNodeBytes);
        taskNode = new String(taskNodeBytes);

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

        /**
         * write node ring & task counts
         */
        dataout.writeInt(type);
        dataout.writeInt(ringNodes.size());

        for(int i = 0; i < ringNodes.size(); i++){
            byte[] nodeBytes = ringNodes.get(i).getBytes();
            dataout.writeInt(nodeBytes.length);
            dataout.write(nodeBytes);
            dataout.writeInt(taskCounts.get(i));
        }

        // for(String node : ringNodes){
        //     byte[] nodeBytes = node.getBytes();
        //     dataout.writeInt(nodeBytes.length);
        //     dataout.write(nodeBytes);
        // }

        /**
         * write task to node ring
         */
        byte[] taskBytes = taskNode.getBytes();
        dataout.writeInt(taskBytes.length);
        dataout.write(taskBytes);
        dataout.flush();
        byte[] marshalledBytes = byteout.toByteArray();
        dataout.close();
        return marshalledBytes;

    }

    public String toString(){
        return "nodes in ring" + String.join(",", ringNodes) + "TaskCounts" + taskCounts.toString() + "TaskNode" + taskNode;
    }

}
