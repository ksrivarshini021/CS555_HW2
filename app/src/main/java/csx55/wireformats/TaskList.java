package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import csx55.hashing.Task;

public class TaskList implements Event {
    private final int type = Protocol.TASK_LIST;
    private List<Task> tasks;
    private boolean isPull;
    private int pulledCount;
    private String requestingNode;

    public TaskList(List<Task> tasks){
        this.tasks = new ArrayList<>(tasks);
        this.isPull = false;
        this.pulledCount = 0;
        this.requestingNode = null;

    }

    public TaskList (int pulledCount, String requestingNode){
        this.tasks = new ArrayList<>();
        this.isPull = true;
        this.pulledCount = pulledCount;
        this.requestingNode = requestingNode;

    }


    public TaskList(byte[] marshalledBytes) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        datain.readInt();
        this.isPull = datain.readBoolean();
        this.pulledCount = datain.readInt();

        /**
         * read requesting node fully
         */
        int nodeLength = datain.readInt();
        byte[] nodeLengthBytes = new byte[nodeLength];
        datain.readFully(nodeLengthBytes);

        this.requestingNode = new String(nodeLengthBytes);

        /**
         * for readinf task list
         */
        int taskNo = datain.readInt();
        this.tasks = new ArrayList<>();
        for(int i = 0 ; i < taskNo; i++){
            int taskLength = datain.readInt();
            byte[] taskBytes = new byte[taskLength];
            datain.readFully(taskBytes);

            String taskString = new String(taskBytes);
            String[] parts = taskString.split(":");
            if(parts.length < 4) continue;

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            int roundNumber = Integer.parseInt(parts[2]);
            int load = Integer.parseInt(parts[3]);
           
            Task task = new Task(host, port, roundNumber, load);
            // task.unmarshal(taskBytes);
            this.tasks.add(task);

        }
        datain.close();
        bytein.close();
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
        dataout.writeBoolean(isPull);
        dataout.writeInt(pulledCount);

        byte[] requestingNodeBytes = requestingNode.getBytes();
        dataout.writeInt(requestingNodeBytes.length);
        dataout.write(requestingNodeBytes);
        dataout.writeInt(tasks.size());

        for(Task task : tasks){
            byte[] taskBytes = task.toBytes();
            dataout.writeInt(taskBytes.length);
            dataout.write(taskBytes);
        }

        dataout.flush();
        byte[] marshalledBytes = byteout.toByteArray();

        dataout.close();
        byteout.close();
        return marshalledBytes;
    }

    public List<Task> getTasks(){
        return tasks;
    }

    public boolean isPull(){
        return isPull;
    }

    public int getPulledCount(){
        return pulledCount;
    }

    public String getRequestingNode(){
        return requestingNode;
    }

    @Override
    public String toString(){
        return "tasks" + tasks.size() + "pulled?" + isPull + "pilled count:" + pulledCount + "from requesting node" + requestingNode;
    }

}
