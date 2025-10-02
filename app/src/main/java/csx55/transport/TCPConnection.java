package csx55.transport;

import java.io.IOException;
import java.net.Socket;
import csx55.threads.Node;

public class TCPConnection {
    private Socket socket;
    private TCPSender tcpSend;
    private TCPReceiverThread tcpReceive;
    // private TCPSenderThread tcpsend;
    private Thread receiverThread;
    private Node node;

    public TCPConnection(Socket socket, Node node){
        this.socket = socket;
        this.node = node;
        try {
            this.tcpSend = new TCPSender(socket);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            this.tcpReceive = new TCPReceiverThread(socket, node, this);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        this.receiverThread = new Thread(tcpReceive);
    }

    /**
     * start receiver thread for conenction
     */
    public void start(){
        receiverThread.start();
        
        
    }

    /**
     * get the sender to send messages
     */
    public TCPSender getTCPSenderThread(){
        return tcpSend;
    }
    public void close(){
        try{
            socket.close();

        } catch (IOException e){
            System.err.println("Error occured while closing scoket" + e.getMessage());
        }
    }

    public Socket getSocket(){
        return socket;
    }



}
