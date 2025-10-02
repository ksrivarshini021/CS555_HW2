package csx55.wireformats;
import java.io.*;

public class TaskSummaryRequest  implements Event{
    
    private final int type;

    public TaskSummaryRequest(){
        this.type = Protocol.PULL_TRAFFIC_SUMMARY;
    }
    public TaskSummaryRequest(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        this.type = datain.readInt();
        // datain.close();
        // bytein.close();
    }

    @Override
    public byte[] getBytes() throws IOException{
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(type);
        dataout.flush();

        byte[] marshalledBytes = byteout.toByteArray();

        dataout.close();
        byteout.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString(){
        return "TaskSumamryType" + type;
    }
    
}
