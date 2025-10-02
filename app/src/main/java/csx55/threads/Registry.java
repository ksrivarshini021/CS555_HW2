package csx55.threads;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import csx55.transport.TCPConnection;
import csx55.transport.TCPServerThread;
import csx55.util.OverlayCreator;
import csx55.wireformats.*;

/**
 * To accept and manage registered nodes ; contains a list of connected nodes
 */
public class Registry implements Node {
    // private Registry(){}
    // private ConcurrentHashMap<String, Socket> registeredMessagingNodes = new
    // ConcurrentHashMap<>();
    private LinkWeights weights = null;
    private final Map<String, TCPConnection> connections = new ConcurrentHashMap<>();
    private final AtomicInteger completedTasks = new AtomicInteger(0);

    private int rounds = 0;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            String port = null;
            System.err.println("Using port" + port);
            System.exit(1);

        }
        /**
         * port number ; create registry ; starting to lsiten to any connections
         */
        int port = Integer.parseInt(args[0]);
        Registry registry = new Registry();
        // registry.launchServerSocket(port);
        // registry.processArgs();

        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Registry lsitning on port" + port);

            new Thread(new TCPServerThread(registry, ss)).start();
            registry.processArgs();

        } catch (IOException e) {
            System.err.println("Error ooning registry" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void processArgs() throws IOException {
        Scanner cmd = new Scanner(System.in);
        System.out.println("Registry ready for for commands.");

        while (true) {
            String line = cmd.nextLine();
            if (line == null || line.trim().isEmpty())
                continue;

            String[] input = line.trim().split("\\s+");
            String command = input[0].toLowerCase();

            // String command = input[0];
            switch (command) {
                case "list-messaging-nodes":
                    printingMessagingNodesList();
                    break;

                case "setup-overlay":
                    int numconnections = 2;
                    if (input.length > 1) {
                        try {
                            numconnections = Integer.parseInt(input[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("invalid number for connections");
                        }
                    }
                    setupOverlay(numconnections);
                    break;

                case "start":
                    TaskInitiate(input);
                    break;

                default:
                    System.out.println("unknown command");
                    break;
            }
        }

    }

    @Override
    public void onEvent(Event event, TCPConnection connection) {
        int type = event.getType();
        switch (type) {
            case Protocol.REGISTER_REQUEST:
                handleRegisteration((Register) event, connection, true);
                // System.out.println("Registry received event type: " + type);

                break;

            case Protocol.DEREGISTER_REQUEST:
                handleRegisteration((Register) event, connection, false);
                break;
            case Protocol.TASK_COMPLETE:
                completedTask();
                break;

            default:
                System.err.println("unkown type" + type);

        }

    }

    /**
     * To register or de-register messaging node
     * 
     * @param event
     * @param connection
     * @param b
     */
    private void handleRegisteration(Register event, TCPConnection connection, boolean register) {
        String nodeValue = event.ipAddress() + ":" + event.getPort();
        String responseString;
        byte status;

        if (register) {
            if (connections.containsKey(nodeValue)) {
                responseString = "Node already registered" + nodeValue;
                status = Protocol.FAILURE;
            } else {
                connections.put(nodeValue, connection);
                responseString = nodeValue;
                status = Protocol.SUCCESS;
                // System.out.println("Received request from " + event.ipAddress()
                // + ":" + event.getPort());

            }
        }
        // to deregister
        else {
            if (!connections.containsKey(nodeValue)) {
                responseString = "Node not registered" + nodeValue;
                status = Protocol.FAILURE;
            } else {
                connections.remove(nodeValue);
                responseString = "node de-registered from" + connections.size();
                status = Protocol.SUCCESS;
                System.out.println(responseString);
            }

        }

        try {
            RegisterResponse response = new RegisterResponse(status, responseString);
            connection.getTCPSenderThread().sendData(response.getBytes());

        } catch (IOException e) {
            System.err.println("Failed to connect to" + nodeValue + e.getMessage());

        }
        System.out.println(responseString);

    }

    private synchronized void completedTask() {
        int completedTask = completedTasks.incrementAndGet();
        /**
         * shoudl wait for all nodes first
         */
        if(completedTask != connections.size()){
            return;
        }
        completedTasks.set(0);
        if (rounds > 0) {
            System.out.println(rounds + " rounds completed");
        } else {
            System.out.println("success");
        }

        try{
            Thread.sleep(15000);
        } catch (InterruptedException ie) {}

        for (String node : connections.keySet()) {
            TCPConnection connection = connections.get(node);
            try {
                TaskSummaryRequest request = new TaskSummaryRequest();
                connection.getTCPSenderThread().sendData(request.getBytes());
            } catch (IOException e) {
                System.err.println("Failed to fetch summary" + node + e.getMessage());
            }
        }
    }


    private void printingMessagingNodesList() {
        if (connections.isEmpty()) {
            System.out.println("No registered messaging nodes");
            return;
        }

        System.out.println("Connected nodes" + connections.size());
        for (String node : connections.keySet()) {
            System.out.println(node);
        }
    }

    /**
     * display colelcted sumamry of traffic from all nodes
     * 
     * @param event
     * @throws IOException
     */
    // private void TrafficSummary(TaskSummaryResponse event) {
    // summary.add(event);
    // if(summary.size() == connections.size()){
    // System.out.println("summary collected form all nodes");
    // summary.forEach(System.out::println);
    // summary.clear();
    // }
    // }

    private void displayLinkWeights() throws IOException {
        if (weights == null) {
            if (connections.size() < 2) {
                System.out.println("need atleast 2 nodes for connections");
                return;
            }
        }
        for (String link : weights.getLinkStrings()) {
            System.out.println(link);
        }

    }

    private void setupOverlay(int connectionsPerNode) throws IOException {
        if (connections.size() < 2) {
            System.out.println("atleast 2 nodes required to create an overlay");
            return;
        }

        if (connectionsPerNode >= connections.size()) {
            System.out.println("Available messages are less than connections required");
            return;
        }

        List<String> nodes = new ArrayList<>(connections.keySet());
        OverlayCreator overlay = new OverlayCreator(nodes, connectionsPerNode);
        boolean buildOverlay = overlay.buildOverlay();

        if (!buildOverlay) {
            System.out.println("Failed to create overlay");
            return;
        }

        /* To Store weights in registry */
        weights = new LinkWeights(overlay.getEdgeWeights());

        for (String node : nodes) {
            Set<String> adj = overlay.getAdjList().get(node);
            MessagingNodesList message = new MessagingNodesList(new ArrayList<>(adj));
            connections.get(node).getTCPSenderThread().sendData(message.getBytes());
        }
        System.out.println("setup completed with " + weights.getLinkStrings().size() + " connections");

    }

    private void sendLinkWeights() {
        if (weights == null) {
            System.out.println("needed to create overlay first");
            return;
        }
        for (TCPConnection connection : connections.values()) {
            try {
                connection.getTCPSenderThread().sendData(weights.getBytes());
            } catch (IOException e) {
                System.err.println("cannot assign weights to ndoe" + e.getMessage());
            }
        }
        System.out.println("link weights assigned");
    }

    private void TaskInitiate(String[] input) {
        if (connections.size() < 2) {
            System.out.println("need to messaging ndeos to initialte task");
            return;
        }
        int rounds = 1;
        if (input.length > 1) {
            try {
                rounds = Integer.parseInt(input[1]);
            } catch (NumberFormatException ne) {
                System.out.println("Invalid input of number of rounds so setting to Default - 1 ");
            }
        }

        this.rounds = rounds;

        TaskInitiate task = new TaskInitiate(rounds);

        for (String node : connections.keySet()) {

            try {
                TCPConnection connection = connections.get(node);
                connection.getTCPSenderThread().sendData(task.getBytes());
            } catch (IOException e) {
                System.err.println("Failed to communicate with node" + node + e.getMessage());

            }

        }
        // System.out.println(rounds + " " + " rounds completed");

    }

    @Override
    public void addRegister(TCPConnection connection) throws IOException {
        System.out.println("new connection added" + connection.getSocket().getInetAddress());
    }

    @Override
    public void onSendError(TCPConnection connection) {
        System.err.println("Error sending connection to" + connection.getSocket().getInetAddress());
        String keyRemove = null;
        for (String node : connections.keySet()) {
            TCPConnection tcpConnection = connections.get(node);
            if (tcpConnection.equals(connection)) {
                keyRemove = node;
                break;
            }
        }
        if (keyRemove != null) {
            connections.remove(keyRemove);
            System.out.println("failed to remove" + keyRemove);
        }

    }

    /**
     * if conencton fails and the node has to be removed
     */
    @Override
    public void onReceiverError(TCPConnection connection) {
        System.err.println("error receiving connecting" + connection.getSocket().getInetAddress());
    }

    @Override
    public void launchServerSocket(int portNum) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'launchServerSocket'");
    }

}
