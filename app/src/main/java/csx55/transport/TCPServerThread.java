package csx55.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import csx55.threads.Node;

public class TCPServerThread implements Runnable {
    private Node node;
    private ServerSocket ss;
    private boolean running = true;

    public TCPServerThread(Node node, ServerSocket ss) {
        this.node = node;
        this.ss = ss;

    }

    public void run() {
        System.out.println("TCP server thread listening to port" + ss);

        while (running) {
            try {
                Socket messagingSocket = ss.accept();
                TCPConnection connection = new TCPConnection(messagingSocket, node);
                connection.start();
                node.addRegister(connection);
            } catch (IOException ioe) {

            }

        }
    }

}
