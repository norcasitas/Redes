/*
 * Class that represents a peer in a network.
 */
package proyecto_redes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import static proyecto_redes.Messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Peer {
    private static final IPPorts MYIP = new IPPorts("127.0.0.1", 9876, 5217); //IPPorts object with the local IP and the UDP and Telnet ports 

    private LinkedList<IPPorts> connectedIPs = new LinkedList<>(); //Linked list that stores the IP addresses of other connected peers except this one
    private Vehicle vehicle; //Representation of the actual state of the shared resource
    private LinkedList<QueueObject> queue;//Priority queue with all the requests
    private int time; //Amount of time that the peer has been online
    private long pid; //Process id of the peer
    private UDPPeerServer udpPeerServer; //Class that manages the UDP commands
    private LinkedList<IPPorts> peersIPs = new LinkedList<>(); //Linked list that stores the IP addresses of all the peers except this one

    /**
     * Constructor: Creates a new Vehicle object to represent the shared resource, loads all the IP's of other peers
     * initializes the priority queue, sets the id of the peer, the time with 0, starts the UDP thread 
     * and notifies other peers about the connection.
     * @throws SocketException
     * @throws IOException
     */
    public Peer() throws SocketException, IOException {
        vehicle = new Vehicle();
        readIpsFromFile();
        queue = new LinkedList();
        pid = Long.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]); //Obtains the peer id through the process id
        time = 0;
        udpPeerServer = new UDPPeerServer(this); //Starts to listen through UDP 9876 port
        notifyConection();
    }

    /**
     * Reads the IPs of other peers from a text file
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void readIpsFromFile() throws FileNotFoundException, IOException {
        BufferedReader brFin = new BufferedReader(new FileReader("ips.txt"));
        String ipWithPorts;
        while ((ipWithPorts = brFin.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(ipWithPorts);
            peersIPs.add(new IPPorts(st.nextToken(), Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken())));
        }
    }

    /**
     * Starts the listening UDP server
     * @throws Exception
     */
    public void init() throws Exception {
        System.out.println("listening UDP in port " + getMYIP().getPortUDP());
        System.out.println("listening TELNET in port " + getMYIP().getPortTelnet());
        udpPeerServer.start();
        runTelnetServer();
    }

    /**
     * Notifies all the other IPs that this peer has connected.    
     *
     * @throws SocketException
     * @throws IOException
     */
    private void notifyConection() throws SocketException, IOException {
        String sentence = MSGNEWCONECTION + " " + String.valueOf(time) + " " + pid;
        byte[] sendData = sentence.getBytes();
        //Sends a message to every peer connected, notifying this new connection
        for (IPPorts ip : peersIPs) {
            DatagramSocket clientSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getIp(), ip.getPortUDP());
            clientSocket.send(sendPacket);
            clientSocket.close();
        }

    }

    /**
     * Returns a specific port of the given IP.
     * If the IP does not exist, this returns -1.
     *
     * @param type of the port to return. 1 for UDP, 2 for Telnet
     * @param ip of which the port must be taken
     * @return specific port number. If the IP does not exists, returns -1
     */
    public int getPortByIP(int type, InetAddress ip) {
        for (IPPorts ipPort : connectedIPs) {
            if (ipPort.getIp().equals(ip)) {
                if (type == 1) {
                    return ipPort.getPortUDP();
                }
                if (type == 2) {
                    return ipPort.getPortTelnet();
                }
            }
        }
        return -1;
    }

    /**
     * Given an IP, returns an IPPorts that contains the IP and respective ports.
     *
     * @param ip The IP that must be retrieved.
     * @return An IPPorts object with the IP and respective ports.
     */
    public IPPorts getIPPortsByIP(InetAddress ip) {
        for (IPPorts ipPort : peersIPs) {
            if (ip.getHostAddress().equals(ipPort.getIp().getHostAddress())) {
                return ipPort;
            }
        }
        return null;
    }

    /**
     * Enqueues the given task of a peer in the correct position of the priority queue of tasks.
     * The criteria for the queue is to set the tasks with lower time first, in case of a tie between two tasks,
     * the one with lower pid goes first.
     * @param qb QueueObject containing the task to be enqueued.
     */
    public void enqueue(QueueObject qb) {
        int i = 0;
        if (queue.size() > 0) {
            while (i < queue.size() && (queue.get(i).getTime() < qb.getTime())) {
                i++;
            }
            while (i < queue.size() && queue.get(i).getTime() == qb.getTime() && queue.get(i).getPid() < qb.getPid()) {
                i++;
            }
        }
        queue.add(i, qb);
    }

    /**
     * Returns the first pid in the task queue, in case of an empty queue, returns -1.
     * @return the pid of the first task in the queue if it is not empty. Otherwise, returns -1.
     */
    public long getFirstPid() {
        return queue.size() > 0 ? queue.getFirst().getPid() : -1;
    }

    /**
     * Dequeue the first task of the priority queue of tasks.
     *
     * @return the QueueObject representing the first task in the priority queue.
     */
    public QueueObject dequeue() {

        return queue.isEmpty() ? null : queue.removeFirst();
    }

    /**
     * Returns the process id of the peer.
     *
     * @return the process id of the peer.
     */
    public long getPid() {
        return pid;
    }

    /**
     * Starts the telnet server.
     *
     * @throws IOException
     * @throws Exception
     */
    public void runTelnetServer() throws IOException, Exception {
        ServerSocket Soc = new ServerSocket(getMYIP().getPortTelnet()); //Creates a ServerSocket that listens in the Telnet Port of this peer
        while (true) { //Accepts the clients that want to connect.
            Socket CSoc = Soc.accept();
            TelnetPeerServer ob = new TelnetPeerServer(CSoc, this);
        }
    }

    /**
     * Updates the peer time only if the new time is greater than the old one.
     *
     * @param newTime the new time for the peer.
     */
    public void updateTime(int newTime) {
        if (this.time < newTime) {
            this.time = newTime;
        }
    }

    /**
     * Transfiero la data que entra por telnet a udp
     *
     * @param amount
     * @return
     * @throws java.io.IOException
     */
    public boolean reserve(int amount) throws IOException {
        time++;
        return udpPeerServer.reserve(amount);
    }

    public int available() throws IOException {
        time++;
        return vehicle.available();
    }

    public boolean cancel(int amount) throws IOException {
        time++;
        return udpPeerServer.cancel(amount);
    }

    /**
     * Returns the current state of the shared resource, represented by a Vehicle object.
     * @return The Vehicle object that represents the current state of the shared resource.
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Return the amount of reserved seats in a given moment.
     *
     * @return The number of seats reserved.
     */
    public int getReservedSeats() {
        return vehicle.getReservedSeats();
    }

    /**
     * Sets the quantity of reserved seats.
     *
     * @param seats The amount of reserved seats.
     */
    public void setReservedSeats(int seats) {
        vehicle.setReservedSeats(seats);
    }

    /**
     * Returns the time of this peer.
     *
     * @return The time of this peer.
     */
    public int getTime() {
        return time;
    }

    /**
     * Returns the list of all the connected IPs at the moment
     * 
     * @return List of all the connected IPs
     */
    public LinkedList<IPPorts> getIps() {
        return connectedIPs;
    }

    /**
     * Adds the given IP to the connectedIPs list
     * @param ip The IP that connected
     */
    public void addIP(IPPorts ip) {
        connectedIPs.add(ip);
    }

    /**
     * Returns the priority queue of QueueObject representing the tasks at the moment.
     * @return The priority queue with the tasks.
     */
    public LinkedList<QueueObject> getQueue() {
        return queue;
    }

    /**
     * Set the queue with trash to simulate the existance of objects before this peer task.
     *
     * @param size The amount of trash objects to add to the queue.
     */
    public void setQueueWithTrash(int size) {
        queue.clear();
        for (int i = 0; i < size; i++) {
            queue.add(new QueueObject(-1, -1));
        }
    }

    /**
     * Init the peer with the given ports.
     * @param args first argument is the UDP port, the second the Telnet port
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.err.println("the first param is UDP port and the second param is TELNET port ");
            System.exit(1);
        }
        getMYIP().setPortUDP(Integer.valueOf(args[0]));
        getMYIP().setPortTelnet(Integer.valueOf(args[1]));
        new Peer().init();
    }

    /**
     * @return the IP and PORTS of the peer in a IPPort object 
     */
    public static IPPorts getMYIP() {
        return MYIP;
    }

} //End of Peer class.
