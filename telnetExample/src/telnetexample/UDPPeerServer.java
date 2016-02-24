/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;
import static telnetexample.MyValues.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UDPPeerServer extends Thread {

    private DatagramSocket serverSocket;
    private int stamp;
    private static byte[] sendData;
    private static byte[] receiveData;
    private int receivedAck;
    
    public UDPPeerServer() throws SocketException {
        serverSocket = new DatagramSocket(9876);
        stamp = 0;
        receiveData = new byte[20];
    }

    public static void broadcast(int action) throws UnknownHostException, SocketException, IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        java.util.Date date = new java.util.Date();
        String sentence = action + " " +  String.valueOf(date.getTime() - Peer.diference) + " " + Peer.pid;
        sendData = sentence.getBytes();
        for (InetAddress ip : Peer.ips) {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 9876);
            clientSocket.send(sendPacket);
        }
        clientSocket.close();

    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                DatagramSocket clientSocket = new DatagramSocket();
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                byte[] data = receivePacket.getData();

                System.out.println("RECEIVED in thread udppeerserver: " + sentence);

                int size = 0;
                while (size < data.length) {
                    if (data[size] == 0) {
                        break;
                    }
                    size++;
                }
                
                // Specify the appropriate encoding as the last argument
                String str = new String(data, 0, size, "UTF-8");
                
                
               int msg = Integer.valueOf(str.split(" ")[0]);
               QueueObject qb = new QueueObject(Long.valueOf(str.split(" ")[1]),Long.valueOf(str.split(" ")[2]));
               
               if (msg != MSGACK) {
                    //SI NO RESIVO UN ACK, PUEDO RECIVIR UN RELEASE O UN ENTER.
                     if (msg == MSGRELEASE) {
                        Peer.queue.remove(0);
                        if ((Peer.queue.get(0).getPid()) == Peer.pid ){
                            //ES MI TURNO TENGO QUE EJECUTAR LA ACCION (falta) Y MANDO BRODCAST
                            
                            broadcast(MSGRELEASE);
                        } 
                    }
                    if (msg == MSGENTER) {
                        //ENCOLO Y MANDO ACK
                        Peer.queue.remove(0);
                        Peer.enqueue(qb);
                        java.util.Date date = new java.util.Date();
                        String ds = MSGACK + " " +  String.valueOf(date.getTime() - Peer.diference) + " " + Peer.pid;
                        sendData = ds.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), 9876);
                        clientSocket.send(sendPacket);
                    } 
                } else {
                    System.out.println("DIERON IGUALES!");
                    receivedAck++;
                    if (receivedAck == Peer.ips.length) {
                         //ES MI TURNO TENGO QUE EJECUTAR LA ACCION (falta) Y MANDO BRODCAST
                            receivedAck = 0;
                            broadcast(MSGRELEASE);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
