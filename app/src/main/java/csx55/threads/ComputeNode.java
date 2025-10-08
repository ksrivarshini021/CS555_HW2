package csx55.threads;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import csx55.hashing.Task;
import csx55.transport.TCPConnection;
import csx55.transport.TCPServerThread;
import csx55.wireformats.BalancingLoad;
import csx55.wireformats.Event;
import csx55.wireformats.OverlaySetup;
import csx55.wireformats.Protocol;
import csx55.wireformats.Register;
import csx55.wireformats.RingMessage;
import csx55.wireformats.TaskComplete;
import csx55.wireformats.TaskInitiate;
import csx55.wireformats.TaskList;
import csx55.wireformats.TaskSummaryResponse;
import csx55.wireformats.UpdateStats;

public class ComputeNode implements Node {
    private String Host;
    private int Port;
    private ThreadPool pool;
    private int ThreadCount;
    private int rounds;

    private List<String> neighbors = new ArrayList<>();
    // private List<String> ring = new ArrayList<>();
    private List<Task> currentTasks = new ArrayList<>();
    private Map<String, TCPConnection> peerConnection = new ConcurrentHashMap<>();
    private Map<String, Integer> route = new ConcurrentHashMap<>();
    // private List<String> balaceNode = new ArrayList<>();

    private UpdateStats stats = new UpdateStats();
    private TCPConnection connectToRegistry;
    boolean taskInProgress = false;
    boolean sendSummary = false;

    public ComputeNode(String host, int port) {
        this.Host = host;
        this.Port = port;
        this.ThreadCount = 0;
        this.pool = null;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Using port <registry-host> <registry-port>");
            System.exit(1);
        }

        String registryHost = args[0];
        int registryPort = Integer.parseInt(args[1]);

        try (ServerSocket ss = new ServerSocket(0)) {
            int nodePort = ss.getLocalPort();
            String nodeHost = InetAddress.getLocalHost().getHostAddress();

            ComputeNode node = new ComputeNode(nodeHost, nodePort);

            System.out.println(" Node Host" + Inet4Address.getLocalHost());
            System.out.println(" Node IP" + nodeHost);
            System.out.println("TCP Server Listening on port:" + nodePort);

            /**
             * start to listen to connections after scoket cration
             */
            Thread serverThread = new Thread(new TCPServerThread(node, ss));
            serverThread.start();
            /**
             * connect to registry
             */
            node.connectToRegistry(registryHost, registryPort);
            node.processArgs();
        } catch (IOException e) {
            System.err.println("Cannot start messagin node:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Conencting to current messaging node and register this with the registry
     * 
     * @param registryHost
     * @param registryPort
     */
    private void connectToRegistry(String registryHost, int registryPort) {
        try {
            Socket rs = new Socket(registryHost, registryPort);
            /**
             * create registry scoket with the message instance
             */
            TCPConnection connection = new TCPConnection(rs, this);
            connection.start();
            this.connectToRegistry = connection;

            /**
             * Create event registry
             */
            Register register = new Register(Protocol.REGISTER_REQUEST, Host, Port);

            /*
             * sending message through sender thread
             */
            connection.getTCPSenderThread().sendData(register.getBytes());
            System.out.println("Registerted with registry through" + Host + "port:" + Port);

        } catch (Exception e) {
            System.err.println("Failed to connect to registry" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event event, TCPConnection connection) throws Exception {
        int type = event.getType();
        switch (type) {
            case Protocol.INITIATE_OVERLAY_SETUP:
                handleOverlaysetUp((OverlaySetup) event);
                // OverlaySetup setup = (OverlaySetup) event;
                // this.ThreadCount = setup.getThreadCount();
                // this.neighbors = setup.getNeighbours();

                // setUpConnection();

                // pool = new ThreadPool(ThreadCount, 1000, 0);

                break;

            case Protocol.TASK_INITIATE:
                handleTaskInitiate((TaskInitiate) event);

                break;

            case Protocol.TASK_LIST:
                handleTaskList((TaskList) event);

                break;
            case Protocol.RING_MESSAGE:
                handleRingMessage((RingMessage) event);

                break;
            case Protocol.BALANCING_LOAD:
                handleLoadBalancing((BalancingLoad) event);

            case Protocol.PULL_TRAFFIC_SUMMARY:
                handleTrafficSummaryRequest(connection);

                break;
            default:
                break;
        }
    }

    private void handleTrafficSummaryRequest(TCPConnection connection) throws IOException {
        if (!sendSummary) {
            TaskSummaryResponse response = new TaskSummaryResponse(Host, Port, stats);
            connection.getTCPSenderThread().sendData(response.getBytes());
            sendSummary = true;
            System.out.println("send summary to registry");

        }

    }

    private void handleOverlaysetUp(OverlaySetup event) {
        this.ThreadCount = event.getThreadCount();
        this.neighbors = event.getNeighbours();
        handleSetUpConnection();
        this.pool = new ThreadPool(ThreadCount, 1000, 0);
        System.out.println("overlay ste up, thread count is" + ThreadCount);

    }

    private void handleTaskList(TaskList tasklist) {
        currentTasks.addAll(tasklist.getTasks());
        System.out.println("received task" + tasklist.getTasks().size());
    }

    private void handleSetUpConnection() {
        for (String neighbour : neighbors) {
            if (!peerConnection.containsKey(neighbour)) {
                String[] parts = neighbour.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                try {
                    Socket socket = new Socket(host, port);
                    TCPConnection connection = new TCPConnection(socket, this);
                    connection.start();
                    peerConnection.put(neighbour, connection);
                } catch (IOException e) {
                    System.err.println("cannot connect to neighbour");
                }
            }
        }
        System.out.println("Overlay setup doone");
        System.out.println("Thread count" + this.ThreadCount);
        System.out.println("neighbours" + this.neighbors);
    }

    private void handleTaskInitiate(TaskInitiate event) throws InterruptedException, IOException {
        sendSummary = false;
        this.rounds = event.getNumRounds();
        for (int i = 0; i < rounds; i++) {
            System.out.println("Starting rounds" + (i + 1));
            generateTask();
            executeTasks();
            sendLoadMessage();
            System.out.println("finishes rounds" + (i + 1));
        }

        if (connectToRegistry != null) {
            TaskComplete complete = new TaskComplete(Host, Port);
            connectToRegistry.getTCPSenderThread().sendData(complete.getBytes());
            System.out.println("task completed" + Host + ":" + Port);
        }
        // taskInProgress = false;
    }

    private void generateTask() {
        Random rand = new Random();
        // int minTask = 1;
        // int maxTask = 1000;
        int numTask = rand.nextInt(1000) + 1;

        currentTasks.clear();
        stats.updateGeneratedCount(numTask);
        for (int i = 0; i < numTask; i++) {
            Task task = new Task(Host, Port, 1, rand.nextInt(10000));
            currentTasks.add(task);
        }

        System.out.println("Generated tasks" + numTask);

    }

    private void executeTasks() throws InterruptedException {
        List<Thread> workers = new ArrayList<>();

        /** add tasks for the pool */
        for (Task task : currentTasks) {
            pool.submit(task);
        }

        for (int i = 0; i < ThreadCount; i++) {
            Thread thread = new Thread() {
                public void run() {
                    while (true) {
                        Task task = pool.getTasks();
                        if (task == null) {
                            break;
                        }
                        task.setThreadId();
                        task.setTimestamp();
                        int nonce = 0;
                        int payload = task.getPayload();
                        for (int j = 0; j < 1000; j++) {
                            nonce += (payload * j + 1) % 13;
                        }
                        task.setNonce(nonce);
                        stats.updateCompletedCount();
                    }
                }
            };
            workers.add(thread);
            thread.start();
        }
        /**
         * stop adding tasks
         */
        pool.stopAdding();

        /**
         * waiting for all thread to finish
         */
        for (Thread thread : workers) {
            thread.join();
        }

        System.out.println("all tasks balanced for this round");
    }

    private void sendLoadMessage() throws IOException {
        List<String> totalNodes = new ArrayList<>(neighbors);
        totalNodes.add(Host + ":" + Port);
        List<Integer> taskCount = new ArrayList<>(Collections.nCopies(totalNodes.size(), 0));

        int currentIndex = totalNodes.indexOf(Host + ":" + Port);
        if (currentIndex >= 0) {
            taskCount.set(currentIndex, currentTasks.size());
        }

        RingMessage message = new RingMessage(totalNodes, taskCount, Host + ":" + Port);
        String nextNode = message.nextNode(Host + ":" + Port);

        TCPConnection connection = peerConnection.get(nextNode);
        if (connection != null) {
            connection.getTCPSenderThread().sendData(message.getBytes());
            System.out.println(" ring messaging started from" + Host + ":" + Port);

        }
    }

    private void handleRingMessage(RingMessage message) throws IOException {
        List<String> ringNodes = message.getRingNodes();
        List<Integer> counts = message.getTaskCounts();

        int currentIndex = ringNodes.indexOf(Host + ":" + Port);
        if (currentIndex != -1) {
            counts.set(currentIndex, currentTasks.size());
        }

        if (message.getTaskNode().equals(Host + ":" + Port)) {
            System.out.println("circled tasks and returned to origin, starting oad balancing");
            executeLoadBalancing(ringNodes, counts);
        } else {
            String nextNode = message.nextNode(Host + ":" + Port);
            TCPConnection connection = peerConnection.get(nextNode);
            if (connection != null) {
                connection.getTCPSenderThread().sendData(message.getBytes());
                System.out.println("Forwarded message to" + nextNode);
            }
        }
    }

    private void executeLoadBalancing(List<String> ringNodes, List<Integer> counts) throws IOException {
        int totalTasks = 0;
        for (int i : counts) {
            totalTasks += i;
        }
        int idealLoad = totalTasks / ringNodes.size();
        int currentLoad = currentTasks.size();
        System.out.println("Total" + totalTasks + "but ideal load" + idealLoad);

        for (int i = 0; i < ringNodes.size(); i++) {
            String node = ringNodes.get(i);
            int load = counts.get(i);
            if (node.equals(Host + ":" + Port))
                continue;

            if (currentLoad > idealLoad + 10 && load < idealLoad - 10) {
                pushTasks(node, Math.min(10, currentLoad - idealLoad));
            } else if (currentLoad < idealLoad - 10 && load > idealLoad + 10) {
                pullTasks(node, Math.min(10, idealLoad - currentLoad));

            }
        }
    }

    private void pullTasks(String sourceNode, int count) throws IOException {
        TaskList requested = new TaskList(count, Host + ":" + Port);
        TCPConnection connection = peerConnection.get(sourceNode);
        if (connection != null) {
            connection.getTCPSenderThread().sendData(requested.getBytes());
        }
        stats.updatePullCount();
        System.out.println("Requested taskes" + count + "from" + sourceNode);

    }

    private void pushTasks(String targetNode, int count) throws IOException {
        List<Task> send = new ArrayList<>();
        for (int i = 0; i < count && i < currentTasks.size(); i++) {
            send.add(currentTasks.remove(0));

        }
        TaskList list = new TaskList(send);
        TCPConnection connection = peerConnection.get(targetNode);
        if (connection != null) {
            connection.getTCPSenderThread().sendData(list.getBytes());
            stats.updatePushCount();
            System.out.println("Pushed" + send.size() + "tasks to node" + targetNode);
        }

    }

    private void handleLoadBalancing(BalancingLoad load) throws IOException {
        List<String> loadList = load.getLoadList();
        int currentLoad = currentTasks.size();

        for (String string : loadList) {
            String[] parts = string.split(":");
            if (parts.length < 3)
                continue;

            String node = parts[0] + ":" + parts[1];
            int otherLoad = Integer.parseInt(parts[2]);

            if (node.equals(Host + ":" + Port)) {
                continue;
            }

            if (currentLoad > otherLoad + 10) {
                pushTasks(node, Math.min(10, currentLoad - otherLoad));
            } else if (currentLoad < otherLoad - 10) {
                pullTasks(node, Math.min(10, otherLoad - currentLoad));
            }
        }

    }

    @Override
    public void processArgs() throws Exception {

    }

    @Override
    public void launchServerSocket(int portNum) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'launchServerSocket'");
    }

    @Override
    public void onReceiverError(TCPConnection connection) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onReceiverError'");
    }

    @Override
    public void onSendError(TCPConnection connection) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onSendError'");
    }

    @Override
    public void addRegister(TCPConnection connection) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addRegister'");
    }

}
