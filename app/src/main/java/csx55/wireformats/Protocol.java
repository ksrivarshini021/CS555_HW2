package csx55.wireformats;
import java.io.*;

public interface Protocol{
    int REGISTER_REQUEST = 1; 
    int REGISTER_RESPONSE = 2;
    int DEREGISTER_REQUEST = 3;
    int DEREGISTER_RESPONSE = 4;
    int INITIATE_OVERLAY_SETUP = 5; //overlay setup
    int MESSAGING_NODES_LIST = 6; //list of messaging nodes and their connections
    int LINK_WEIGHTS = 7; //registry assignes weights to the conenctions ; assignOverlayLinkWeights
    int TASK_INITIATE = 8; // intiate sending messages
    int TASK_COMPLETE = 9; //notify when task is completed Messaging Node Status
    int PULL_TRAFFIC_SUMMARY = 10; // retirve trafiic summary
    int TRAFFIC_SUMMARY = 11; //send Traffic Summaries From Nodes To Registry
    final byte SUCCESS = (byte) 200;
    final byte FAILURE = (byte) 500;
    int MESSAGE = 12;

    public static Event getEventType(int messageType, byte[] marshalledBytes) throws IOException{
        switch (messageType) {
            case REGISTER_REQUEST:
                return new Register(marshalledBytes);
            case REGISTER_RESPONSE:
                return new RegisterResponse(marshalledBytes);
            case DEREGISTER_REQUEST:
                return new Register(marshalledBytes);
            case DEREGISTER_RESPONSE:
                return new RegisterResponse(marshalledBytes);
            // case INITIATE_OVERLAY_SETUP:
            //     return new OverlaySetup(marshalledBytes);
            case MESSAGING_NODES_LIST:
                return new MessagingNodesList(marshalledBytes);
            case LINK_WEIGHTS:
                return new LinkWeights(marshalledBytes);
            case TASK_INITIATE:
                return new TaskInitiate(marshalledBytes);
            case MESSAGE:
                return new Message(marshalledBytes);
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