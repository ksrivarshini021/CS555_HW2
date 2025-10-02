package csx55.wireformats;

import java.io.*;

public class EventFactory {
    private static final EventFactory instance = new EventFactory();

    private EventFactory() {
    }

    /**
     * returns shared instance
     */
    public static EventFactory getInstance() {
        return instance;
    }

    /**
     * 
     */
    private int getType(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));
        int type = datain.readInt(); // reads bytes for type of message
        datain.close();
        bytein.close();
        return type; // int
    }

    public Event createEvent(byte[] marshalledBytes) throws IOException {
        int eventType = getType(marshalledBytes);
        return Protocol.getEventType(eventType, marshalledBytes);
    }

}