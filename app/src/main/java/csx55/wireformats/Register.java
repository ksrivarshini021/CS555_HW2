package csx55.wireformats;
import java.io.*;

public class Register implements Event{
    private final int type;
    private final String ipAddress;
    private final int port;

    /**
     * constructor called for every new register message
     */
    public Register(int type, String ipAddress, int port){
        this.type = type;
        this.ipAddress = ipAddress;
        this.port = port;
    }


    /**
     * constructor for unmarshalling from byte array
     */
    public Register(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        this.type = datain.readInt();

        int identifierLength = datain.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        datain.readFully(identifierBytes);
        // this.type = 0;
        
        this.ipAddress = new String(identifierBytes);
        this.port = datain.readInt();

        datain.close();
        bytein.close();
    }

    public byte[] getBytes() throws IOException{
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(type);

        byte[] identifierBytes = ipAddress.getBytes();
        dataout.writeInt(identifierBytes.length);
        dataout.write(identifierBytes);
        
        dataout.writeInt(port);
        dataout.flush();

        byte[] marshalledBytes = byteout.toByteArray();

        dataout.close();
        byteout.close();
        return marshalledBytes;
    }

    public int getType(){
        return type;
    }

    public String ipAddress(){
        return ipAddress;
    }

    public int getPort(){
        return port;
    }


}