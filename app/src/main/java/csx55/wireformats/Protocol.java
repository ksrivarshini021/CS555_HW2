package csx55.wireformats;
import java.io.*;

public interface Protocol{
    int REGISTER_REQUEST = 1; 
    int REGISTER_RESPONSE = 2;
    int DEREGISTER_REQUEST = 3;
    int DEREGISTER_RESPONSE = 4;
    int INITIATE_OVERLAY_SETUP = 5; //overlay setup
    int TASK_INITIATE = 6; // intiate sending messages
    int RING_MESSAGE = 7; // to circulate tasks clockwise direction
    int TASK_LIST = 8; //for keeping a track of pushed and pulled tasks
    int BALANCING_LOAD = 9; //
    int TASK_COMPLETE = 10; //notify when task is completed Messaging Node Status
    int PULL_TRAFFIC_SUMMARY = 11; // retirve trafiic summary
    int TRAFFIC_SUMMARY = 12; //send Traffic Summaries From Nodes To Registry
    final byte SUCCESS = (byte) 200;
    final byte FAILURE = (byte) 500;
    

    public static Event getEventType(int messageType, byte[] marshalledBytes) throws IOException, ClassNotFoundException{
        switch (messageType) {
            case REGISTER_REQUEST:
                return new Register(marshalledBytes);
            case REGISTER_RESPONSE:
                return new RegisterResponse(marshalledBytes);
            case DEREGISTER_REQUEST:
                return new Register(marshalledBytes);
            case DEREGISTER_RESPONSE:
                return new RegisterResponse(marshalledBytes);
            case INITIATE_OVERLAY_SETUP:
                return new OverlaySetup(marshalledBytes);
            case TASK_INITIATE:
                return new TaskInitiate(marshalledBytes);
            case RING_MESSAGE:
                return new RingMessage(marshalledBytes);
            case TASK_LIST:
                return new TaskList(marshalledBytes);
            case BALANCING_LOAD:
                return new BalancingLoad(marshalledBytes);
            case TASK_COMPLETE:
                return new TaskComplete(marshalledBytes);
            case PULL_TRAFFIC_SUMMARY:
                return new TaskSummaryRequest(marshalledBytes);
            case TRAFFIC_SUMMARY:
                return new TaskSummaryResponse(marshalledBytes);
            

            default: 
                throw new IOException("Unknown message" + messageType);
            
        }
    }
}