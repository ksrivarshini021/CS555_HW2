package csx55.wireformats;

import java.io.*;
import java.util.*;

import csx55.util.OverlayCreator;

public class LinkWeights implements Event {
    private int type;
    private final List<OverlayCreator.Link> links;

    /**
     * to save individual link information ; inner link weights
     */
    // private static class LinkInfo {
    // private final String connection;
    // private final int weight;

    // public LinkInfo(String connection, int weight) {
    // this.connection = connection;
    // this.weight = weight;
    // }

    // public String getConnection() {
    // return connection;
    // }

    // public int getWeight() {
    // return weight;
    // }

    // @Override
    // public String toString() {
    // return connection + weight;
    // }
    // }

    public LinkWeights(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bytein = new ByteArrayInputStream((marshalledBytes));
        DataInputStream datain = new DataInputStream(new BufferedInputStream(bytein));

        this.type = datain.readInt();
        if (this.type != Protocol.LINK_WEIGHTS) {
            // datain.close();
            // bytein.close();
            throw new IOException("Invalid message for link weights" + type);
        }

        int numLinks = datain.readInt();
        this.links = new ArrayList<>(numLinks);

        for (int i = 0; i < numLinks; i++) {
            int nodeALength = datain.readInt();
            byte[] nodeABytes = new byte[nodeALength];
            datain.readFully(nodeABytes);
            String nodeA = new String(nodeABytes);

            int nodeBLength = datain.readInt();
            byte[] nodeBBytes = new byte[nodeBLength];
            datain.readFully(nodeBBytes);
            String nodeB = new String(nodeBBytes);

            int weight = datain.readInt();
            this.links.add(new OverlayCreator.Link(nodeA, nodeB, weight));

        }

        // datain.close();
        // bytein.close();
    }

    /**
     * creating constuctor from a list of OverlayCreator
     * 
     * @param links
     */
    public LinkWeights(List<OverlayCreator.Link> links) {
        this.type = Protocol.LINK_WEIGHTS;
        this.links = new ArrayList<>(links);
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(byteout));

        dataout.writeInt(type);
        // dataout.writeInt(numLinks);
        dataout.writeInt(links.size());

        for (OverlayCreator.Link link : links) {
            byte[] nodeABytes = link.getNodeA().getBytes();
            dataout.writeInt(nodeABytes.length);
            dataout.write(nodeABytes);

            byte[] nodeBBytes = link.getNodeB().getBytes();
            dataout.writeInt(nodeBBytes.length);
            dataout.write(nodeBBytes);

            dataout.writeInt(link.getWeight());
        }

        dataout.flush();
        byte[] marshalledBytes = byteout.toByteArray();

        // dataout.close();
        // byteout.close();
        return marshalledBytes;

    }

    public List<OverlayCreator.Link> getLinks() {
        return new ArrayList<>(links);
    }

    // public void printWeights(){
    // if (links.isEmpty()){
    // System.out.println("No links available");
    // return;
    // } else{
    // System.out.println("setup completed with" + links.size()+ "connections");
    // for(OverlayCreator.Link link : links){
    // System.out.println(link.getNodeA() + "," + link.getNodeB() + "," +
    // link.getWeight());
    // }
    // }

    // }

    @Override
    public int getType() {
        return type;
    }

    public List<String> getLinkStrings() {
        List<String> result = new ArrayList<>();
        for (OverlayCreator.Link link : links) {
            result.add(link.getNodeA() + "," + link.getNodeB() + "," + link.getWeight());
        }
        return result;
    }

}
