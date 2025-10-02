package csx55.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import csx55.threads.Node;
import csx55.wireformats.Event;
import csx55.wireformats.EventFactory;

public class TCPReceiverThread implements Runnable {
    private final TCPConnection connection;
    private final DataInputStream datain;
    private final Node node;

    public TCPReceiverThread(Socket socket, Node node, TCPConnection connection) throws IOException {
        this.connection = connection;
        this.datain = new DataInputStream(socket.getInputStream());
        this.node = node;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int dataLength = datain.readInt();
                byte[] data = new byte[dataLength];
                datain.readFully(data, 0, dataLength);

                Event event = EventFactory.getInstance().createEvent(data);
                node.onEvent(event, connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
