package csx55.wireformats;

import java.io.IOException;

public class TaskComplete implements Event {
    private final int type = Protocol.TASK_COMPLETE;

    public TaskComplete(byte[] marshalledBytes) {
        
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte[] getBytes() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBytes'");
    }

    

}
