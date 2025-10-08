package csx55.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class OverlayCreator {
    // private final List<String> messagingNodeList;

    // neighbours per each npde
    private final Map<String, Set<String>> adjList = new HashMap<>();

    // list for all
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

        // take note of the adjecent nodes
        for (String node : nodes) {
            adjList.put(node, new HashSet<>());
        }

        // then connect linerly to form a ring
        for (int i = 0; i < numNodes; i++) {
            String nodeA = nodes.get(i);
            String pred = nodes.get((i - 1 + numNodes) % numNodes);
            String succ = nodes.get((i + 1) % numNodes);
            addEdge(nodeA, pred);
            addEdge(nodeA, succ);
        }
        return true;
    }

    /**
     * to check if the edge has already been built
     * 
     * @param a
     * @param b
     */
    private void addEdge(String a, String b) {
        adjList.get(a).add(b);
        adjList.get(b).add(a);
    }


    public Map<String, Set<String>> getAdjList() {
        return adjList;
    }

}
