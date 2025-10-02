package csx55.threads;
import java.io.*;

import csx55.transport.*;
import csx55.wireformats.*;


/** interface is for the registery and the messaging nodes */
public interface Node {
    /**
     * event triggered when a new message is received form one node to another
     */
    public void onEvent(Event event, TCPConnection connection) throws Exception;
    /**
     * starts a server socket and stars listening on port
     */
    public void launchServerSocket(int portNum) throws IOException;
    /**
     * handling command line arguments
     */
    public void processArgs() throws Exception;
    /**
     * called when anew connection is made; REgistry keeps track of active connection
     */
    public void addRegister(TCPConnection connection) throws IOException;
    /**
     * called when error during rending/receiving
     */
    public void onReceiverError(TCPConnection connection);
    public void onSendError(TCPConnection connection);


}