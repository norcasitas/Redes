/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    public static void broadcast() throws UnknownHostException, SocketException, IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        String sentence = "Quiero usar el server" + InetAddress.getByName("localhost").toString();
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

                if (!str.equals("ACK")) {
                    String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
                    java.util.Date date = new java.util.Date();
                    long t = date.getTime();
                    String ds = String.valueOf(t) + " " + processName.split("@")[0];
                    sendData = ds.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), 9876);
                    clientSocket.send(sendPacket);
                } else {
                    System.out.println("DIERON IGUALES!");
                    receivedAck++;
                    if (receivedAck == Peer.ips.length) {

                    }
                    //ACA TENDIRA QUE CONTAR TODOS LOS ACK.
                    // CUANDO ESTEN TODOS ENCOLARIA LA TAREA.
                }
                if (!str.equals("RELEASE")) {
                    //ACA CONTARIA TODOS LOS RELEASE
                    // CUANDO ESTEN TODOS, DESENCOLA.
                }
            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
