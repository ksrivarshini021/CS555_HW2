package csx55.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender {

    private Socket socket;
    private DataOutputStream dataout;

    public TCPSender(Socket socket) throws IOException{
        this.socket = socket;
        dataout = new DataOutputStream(socket.getOutputStream());

    }

    public synchronized void sendData(byte[] dataToSend) throws IOException{
        int dataLength = dataToSend.length;
        dataout.writeInt(dataLength);
        dataout.write(dataToSend, 0, dataLength);
    dataout.flush();
    }

}
