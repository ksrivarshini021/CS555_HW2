package csx55.wireformats;
import java.io.*;

public class RegisterResponse implements Event {
    private final byte status;
    private final String responseString;
    private final int type = Protocol.REGISTER_RESPONSE; 

    public RegisterResponse(byte status, String responseString) {
        this.status = status;
        this.responseString = responseString;
        
    }

    public RegisterResponse(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        int messageType = datain.readInt();
        if(messageType != Protocol.REGISTER_RESPONSE){
            datain.close();
            throw new IOException("invalid message ofr register response" + messageType);

        }
        this.status = datain.readByte();

        int length = datain.readInt();
        byte[] messageBytes = new byte[length];
        datain.readFully(messageBytes);
        this.responseString = new String(messageBytes);
        datain.close();
    }

    public byte[] getBytes() throws IOException{
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(type);
        dataout.writeByte(status);
        

        byte[] messageBytes = responseString.getBytes();
        dataout.writeInt(messageBytes.length);
        dataout.write(messageBytes);
        dataout.flush();

        byte[] marshalledBytes = byteout.toByteArray();
        dataout.close();
        byteout.close();
        return marshalledBytes;

    }

    public byte getStatus(){
        return status;
    }

    public String getMessage(){
        return responseString;
    }

    public int getType(){
        return type;
    }

    public String toString(){
        return "Register response, status" + status + "with message" + responseString;
    }

}
