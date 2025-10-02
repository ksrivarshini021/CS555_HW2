package csx55.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import csx55.transport.TCPConnection;

public class OverlayCreator {
    // private final List<String> messagingNodeList;

    //neighbours per each npde
    private final Map<String, Set<String>> adjList = new HashMap<>(); 

    //store weights link
    private final List<Link> LinkWeights = new ArrayList<>(); 

    //list for all 
    private final List<String> nodes; 

    public OverlayCreator(List<String> nodes) {
        // this.messagingNodeList = null;
        this.nodes = new ArrayList<>(nodes); 
    }

    /**
     * 
     * @return
     */
    public boolean buildOverlay() {
        int numNodes = nodes.size();
        if (numNodes < 2) {
            System.out.println("Note enough to get a connection req");
            return false;
        }

        //take note of the adjecent nodes
        for (String node : nodes) {
            adjList.put(node, new HashSet<>());
        }

        //then connect linerly to form a ring
        for (int i = 0; i < numNodes; i++) {
            String nodeA = nodes.get(i);
            for(int j = 1; j <= connectionsPerNode / 2; j++){
                String nodeB = nodes.get((i+j) % numNodes);
                addEdge(nodeA, nodeB);

            }
        }
        return true;
    }

    /**
     * to check if the edge has already been built
     * @param a
     * @param b
     */
    private void addEdge(String a, String b) {
        adjList.get(a).add(b);
        adjList.get(b).add(a);

        int weight = 0;
        try {
            final Random random = new Random();
            weight = random.nextInt(10) + 1;
        } catch (Exception e) {
            System.out.println("Error assigning weights");
            e.printStackTrace();
        }
        LinkWeights.add(new Link(a, b, weight));
    }

    public List<Link> getEdgeWeights() {
        return new ArrayList<>(LinkWeights);
    }

    public List<String> getConnectionsPerNode(String node) {
        if (!adjList.containsKey(node)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(adjList.get(node));

    }

    public static class Link {
        public final String nodeA;
        public final String nodeB;
        public final int weight;

        public Link(String nodeA, String nodeB, int weight) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return nodeA + "," + nodeB + "," + (weight);
        }

        public String getNodeA() {
            return nodeA;
        }

        public String getNodeB() {
            return nodeB;
        }

        public int getWeight() {
            return weight;
        }
    }

    public Map<String, Set<String>> getAdjList() {
        return adjList;
    }

}
