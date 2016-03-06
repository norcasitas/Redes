/*
 * Class that represents a thread that listens for the messages passed through the
 * UDP port of the peer.
 */
package proyecto_redes;

import static proyecto_redes.Messages.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPPeerServer extends Thread {

    private DatagramSocket serverSocket;
    private static byte[] sendData;
    private static byte[] receiveData;
    private int receivedAck;
    private static int actionFromClient;
    private static int amount;
    private static Object result;
    private Peer peer;
    private static final Object LOCK = new Object();

    public UDPPeerServer(Peer peer) throws SocketException {
        serverSocket = new DatagramSocket(Peer.getMYIP().getPortUDP());
        receiveData = new byte[512];
        this.peer = peer;
    }

    /**
     * Sendas a broadcast to all peers connected with the action that this peer
     * wants to do.
     *
     * @param action The action that this peer wants to do.
     * @param time The time of this peer.
     * @param pid The process id of this peer.
     * @throws UnknownHostException
     * @throws SocketException
     * @throws IOException
     */
    public void broadcast(int action, int time, int pid) throws UnknownHostException, SocketException, IOException {
        String sentence = action + " " + String.valueOf(time) + " " + pid; //Creates a sentence with the action, the time of the peer and the peer id.
        if (MSGRELEASE == action) {
            sentence += " " + peer.getReservedSeats(); //If the peer wants to release the resource, concatenate the current state of it.
        }
        sentence += "//";
        sendData = sentence.getBytes();
        //Sends it to every connected peer, does not wait for a synchronic response
        for (IPPorts ip : peer.getIps()) {
            DatagramSocket clientSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getIp(), peer.getPortByIP(1, ip.getIp()));
            clientSocket.send(sendPacket);
            clientSocket.close();
        }

    }

    /**
     * Sends a message with the state of the priority queue of tasks and the
     * shared resource to a specific peer.
     *
     * @param action The action that this peer wants to do.
     * @param time The time of this peer.
     * @param pid The process id of this peer.
     * @param ipDestiny The destination peer IP.
     * @throws SocketException
     * @throws IOException
     */
    public void sendMessageWithBusState(int action, int time, int pid, IPPorts ipDestiny) throws SocketException, IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        String sentence = action + " " + String.valueOf(time) + " " + pid;
        String busState = peer.getVehicle().getReservedSeats() + " " + peer.getQueue().size(); //Creates a string with the current state of the vehicle and the size of the priority queue.
        sentence = sentence + " " + busState +"//";
        sendData = sentence.getBytes();
        //Sends it to every connected peer, does not wait for a synchronic response
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipDestiny.getIp(), peer.getPortByIP(1, ipDestiny.getIp()));
        clientSocket.send(sendPacket);
        clientSocket.close();
    }

    @Override
    public void run() {
        while (true) {
            try {
                //Listens to the messages that comes through the UDP port.
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                DatagramSocket clientSocket = new DatagramSocket();
                serverSocket.receive(receivePacket);
                byte[] data = receivePacket.getData();
                String aux =new String("+++"+data);
                System.out.println(aux);
                aux =  aux.split("//")[0];
                System.out.println("..."+aux);
                // Specify the appropriate encoding as the last argument
                // Split the received message to obtain all the relevant data.
                String str[] = aux.split(" ");
                int action = Integer.valueOf(str[0]);
                int time = Integer.valueOf(str[1]);
                int pid = Integer.valueOf(str[2]);
                // Create a QueueObject that represents a new task, it contains the time and the process id of the peer.
                QueueObject qb = new QueueObject(time, pid);
                peer.updateTime(time); //Updates the time.
                switch (action) {
                    case MSGRELEASE:
                        peer.dequeue();
                        peer.setReservedSeats(Integer.valueOf(str[3]));
                        if (peer.getFirstPid() == peer.getPid()) {
                            myTurn();
                        }
                        break;
                    case MSGENTER:
                        peer.enqueue(qb);
                        //Synchronizes if necessary the time.
                        String ds = MSGACK + " " + String.valueOf(peer.getTime()) + " " + peer.getPid()+"//";
                        sendData = ds.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), peer.getPortByIP(1, receivePacket.getAddress()));
                        clientSocket.send(sendPacket); //Send a response with an ACK.
                        break;
                    case MSGACK:
                        receivedAck++;
                        if (receivedAck == peer.getIps().size()) {//This peer received all the acks
                            if (peer.getFirstPid() == peer.getPid()) {
                                //This peer is the next one on the queue.
                                receivedAck = 0;
                                myTurn();
                            }
                        }
                        break;
                    case MSGNEWCONECTION: //Add a new IP and send an ACK with the shared resource state and the task queue.
                        peer.addIP(peer.getIPPortsByIP(receivePacket.getAddress()));
                        sendMessageWithBusState(MSGACKNEWCONECTION, peer.getTime(), peer.getPid(), peer.getIPPortsByIP(receivePacket.getAddress()));
                        break;
                    case MSGACKNEWCONECTION:
                        /*When other peer acknowledges this peer connection, add it to the connected IPs, set the current status
                         * of the shared resources and fill the queue with trash tasks.
                         */
                        peer.addIP(peer.getIPPortsByIP(receivePacket.getAddress()));
                        peer.getVehicle().setReservedSeats(Integer.valueOf(str[3]));
                        peer.setQueueWithTrash(Integer.valueOf(str[4]));
                        break;
                }

            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Reserves the specified amount of seats.
     *
     * @param amount The amount of seats to reserve.
     * @return True if the seats were reserved, false otherwise.
     * @throws SocketException
     * @throws IOException
     */
    public boolean reserve(int amount) throws SocketException, IOException {
        actionFromClient = Messages.MSGRESERVE;
        UDPPeerServer.amount = amount;
        int time = peer.getTime();
        QueueObject qb = new QueueObject(time, peer.getPid());
        peer.enqueue(qb);
        //If there are no other peers connected, don't wait for acknowledge of other peers and don't send a broadcast.
        if (peer.getIps().isEmpty()) {
            myTurn();
        } else {
            //Notify other connected peers that this peer wants to use the shared resource.
            broadcast(MSGENTER, time, peer.getPid());
            lock();
        }
        boolean ret = (boolean) result;
        result = null;
        return ret;
    }

    /**
     * Cancel the specified amount of reserved seats.
     *
     * @param amount The amount of reserved seats to cancel.
     * @return True if the reserves were cancelled, false otherwise.
     * @throws SocketException
     * @throws IOException
     */
    public boolean cancel(int amount) throws SocketException, IOException {
        actionFromClient = Messages.MSGCANCEL;
        UDPPeerServer.amount = amount;
        int time = peer.getTime();
        QueueObject qb = new QueueObject(time, peer.getPid());
        peer.enqueue(qb);
        //If there are no other peers connected, don't wait for acknowledge of other peers and don't send a broadcast.
        if (peer.getIps().isEmpty()) {
            myTurn();
        } else {
            //Notify other connected peers that this peer wants to use the shared resource.
            broadcast(MSGENTER, time, peer.getPid());
            lock();
        }
        boolean ret = (boolean) result;
        result = null;
        return ret;
    }

    /**
     * Blocks the current thread if result equals null
     */
    private void lock() {
        synchronized (LOCK) {
            while (result == null) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    // Treat interrupt as exit request
                    break;
                }
            }
        }
    }

    /**
     * Method that is executed when this peer is the first on the task queue.
     * This means that this peer can access the shared resource and execute an
     * action.
     */
    private void myTurn() throws SocketException, IOException {
        //Execute the action and send broadcast.
        switch (actionFromClient) {
            case Messages.MSGRESERVE:
                result = peer.getVehicle().reserve(amount);
                break;
            case Messages.MSGCANCEL:
                result = peer.getVehicle().cancel(amount);
                break;
        }
        synchronized (LOCK) {
            //Notify all the locks.
            LOCK.notifyAll();
        }
        //Once the action is finished, dequeue the task.
        peer.dequeue();
        //Send a broadcast notifying all the other connected peers that this peer no longer needs access to the shared resource.
        broadcast(MSGRELEASE, peer.getTime(), peer.getPid());
    }

} //End of UDPPeerServer class
