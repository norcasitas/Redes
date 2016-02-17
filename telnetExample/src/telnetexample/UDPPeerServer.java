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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nico
 */
public class UDPPeerServer extends Thread {

    private DatagramSocket serverSocket;
    private int stamp;
    private static byte[] sendData;
    private static byte[] receiveData;

    public UDPPeerServer() throws SocketException {
        serverSocket = new DatagramSocket(9876);
        stamp = 0;
        receiveData = new byte[20];
    }

    public static void broadcast() throws UnknownHostException, SocketException, IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        String sentence = "Quiero usar el server" + InetAddress.getByName("localhost").toString();
        sendData= sentence.getBytes();
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
                System.out.println("RECEIVED in thread udppeerserver: " + sentence);
                if(receivePacket.getData()!=("ACK".getBytes())){
                    sendData = "ACK".getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,receivePacket.getAddress(), 9876);
                    clientSocket.send(sendPacket);
            }
            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
