package csx55.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BalancingLoad implements Event {
    private final int type = Protocol.BALANCING_LOAD;
    private List<String> loadPernode;

    public BalancingLoad(){
        loadPernode = new ArrayList<>();
    }

    public BalancingLoad(List<String> loadPernode){
        this.loadPernode = new ArrayList<>();
        if(loadPernode != null){
            for(String n : loadPernode){
                this.loadPernode.add(n);
            }
        }
    }

    public void addLoad(String node, int load){
        String entry = node + ":" + load;
        boolean found = false;
        for (int i =0 ; i < loadPernode.size() ; i++){
            if(loadPernode.get(i).startsWith(node + ":")){
                loadPernode.set(i, entry);
                found = true;
                break;
            }
            
        }
        if(!found){
            loadPernode.add(entry);
        }
    }

    public BalancingLoad(byte[] marshalledBytes) throws IOException{
        loadPernode = new ArrayList<>();
        ByteArrayInputStream bytein = new ByteArrayInputStream(marshalledBytes);
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));
        datain.readInt();

        int count = datain.readInt();
        for(int i = 0; i < count; i++){
            int length = datain.readInt();
            byte[] bytes = new byte[length];
            datain.readFully(bytes);
            loadPernode.add(new String(bytes));
        }

        datain.close();
        bytein.close();
        
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));
        dataout.writeInt(type);

        dataout.writeInt(loadPernode.size());
        for(String string : loadPernode){
            byte[] bytes = string.getBytes();
            dataout.writeInt(bytes.length);
            dataout.write(bytes);
        }

        dataout.flush();
        byte[] results = byteout.toByteArray();
        dataout.close();
        byteout.close();
        return results;
    }

    @Override
    public String toString(){
        return "Balancing load" + loadPernode;
    }

    public List<String> getLoadList() {
        return loadPernode;
    }

}
