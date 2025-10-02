package csx55.wireformats;
import java.io.*;


/**
 * captures the type of message event from protocol 
 * each message type must implement this interface
 */
public interface Event{
    int getType();
    /**
     * need to convert into byte array for TCP connection to represent event in byte array
     */
    byte[] getBytes() throws IOException; 
}
