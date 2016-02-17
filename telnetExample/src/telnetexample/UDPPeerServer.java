/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nico
 */
public class UDPPeerServer extends Thread {

    private DatagramSocket serverSocket;
    private byte[] receiveData;
    private byte[] sendData;

    public UDPPeerServer() throws SocketException {
        serverSocket = new DatagramSocket(9876);
        receiveData = new byte[1024];
        sendData = new byte[1024];
        
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println("RECEIVED: " + sentence);
                sendData = "ACK".getBytes();
                DatagramPacket sendPacket= new DatagramPacket(sendData, sendData.length,receivePacket.getSocketAddress());
                serverSocket.send(sendPacket);
            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
}
