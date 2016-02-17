/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author nico
 */
public class Peer {
    
    private Vehicle vehicle;

    public Peer() {
        vehicle=new Vehicle();
    }

    /**
     * Envia un paquete a un server
     *
     * @param ip
     * @param port
     * @throws SocketException
     * @throws UnknownHostException
     * @throws IOException
     */
    public void sendDataUDP(InetAddress ip, int port) throws SocketException, UnknownHostException, IOException {
        BufferedReader inFromUser= new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        String sentence = inFromUser.readLine();
        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
        clientSocket.setSoTimeout(3000);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();
    }

    public static void main(String args[]) throws Exception {
        Peer peer = new Peer();
        
        new UDPPeerServer().start(); //empiezo a escuchar en UDP puerto 9876
        InetAddress IPAddress = InetAddress.getByName("localhost");
        peer.sendDataUDP(IPAddress, 9876);
        peer.runTelnetServer();

    }

    public void runTelnetServer() throws IOException, Exception {
        ServerSocket Soc = new ServerSocket(5217);
        while (true) { // en este while voy recibiendo los clientes
            Socket CSoc = Soc.accept();
            TelnetPeerServer ob = new TelnetPeerServer(CSoc,vehicle);
        }
    }
}
