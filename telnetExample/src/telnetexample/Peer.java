/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import static telnetexample.MyValues.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;


/*
FALTA:

IMPLEMENTAR COLA DE PRIORIDAD EN LA CLASE PEER.
IMPLEMENTAR LA DIFERENCIA EN EL PEER Y SACARLA DEL TELNETSERVER.
IMPLEMENTAR EL MATCH DE MENSAJE Y SUS RESPECTIVAS FUNCIONES DEPENDE EL MATCH.


*/
public class Peer {
    static InetAddress[] ips = new InetAddress[1];
    private Vehicle vehicle;
    LinkedList<QueueObject> queue;
    
    public Peer() throws UnknownHostException {
        vehicle=new Vehicle();
        ips[0] = InetAddress.getByName(IPJOAKO);
        queue = new LinkedList();
    }
    

    public static void main(String args[]) throws Exception {
        Peer peer = new Peer();
        
        new UDPPeerServer().start(); //empiezo a escuchar en UDP puerto 9876
        InetAddress IPAddress = InetAddress.getByName(IPJOAKO);
      
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
