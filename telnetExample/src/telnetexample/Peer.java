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
import java.util.Calendar;
import java.util.LinkedList;


/*
 FALTA:

 IMPLEMENTAR LA DIFERENCIA EN EL PEER.
 IMPLEMENTAR FUNCIONES DEPENDE EL MATCH.


 */
public class Peer {

    static InetAddress[] ips = new InetAddress[1];
    private static Vehicle vehicle;
    private static LinkedList<QueueObject> queue;
    private static long timeSyncronized; //utilizado para llevar la diferencia con el reloj propio
    private static long pid;

    public Peer() throws UnknownHostException {
        vehicle = new Vehicle();
        ips[0] = InetAddress.getByName(IPJOAKO);
        queue = new LinkedList();
        pid = Long.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        timeSyncronized = System.currentTimeMillis(); //con esto, hago que el reloj simule empezar en 0
    }

    static public void enqueue(QueueObject qb) {
        int i = 0;
        while (i!=queue.size() && (queue.get(i).getTime() <= qb.getTime() || queue.size() < i)) {
            i++;
        }
        queue.add(i, qb);
    }
    
    /**
     * obtengo el pid del primero en la cola, en caso de ser vacio, retorno -1
     * @return 
     */
    static public long getFirstPid(){
        return queue.size()>0? queue.getFirst().getPid(): -1;
    }
    
    /**
     * Desencolo el primero en la cola
     * @return 
     */
    static public QueueObject dequeue(){
        return queue.removeFirst();
    }
    
    /**
     * Retorna el identificador del proceso
     * @return 
     */
    static public long getPid(){
        return pid;
    }

    public void runTelnetServer() throws IOException, Exception {
        ServerSocket Soc = new ServerSocket(5217);
        while (true) { // en este while voy recibiendo los clientes
            Socket CSoc = Soc.accept();
            TelnetPeerServer ob = new TelnetPeerServer(CSoc);
        }
    }

    /**
     * actualizo el reloj siempre y cuando el parametro de entrada sea mayor a mi tiempo
     * @param millis 
     */
    public static void updateTime(long millis){
        //si el tiempo que transcurrió desde la ultima sincro es menor al tiempo de entrada,
        //me sincronizo
        if(System.currentTimeMillis() - timeSyncronized  < millis){
            timeSyncronized = millis;
        }
    }
    
    /**
     * Transfiero la data que entra por telnet a udp
     */
    public static boolean reserve(int amount) throws IOException{
        return UDPPeerServer.reserve(amount);
    }

    public static int available() throws IOException {
        return UDPPeerServer.available();
    }
    
    public static boolean cancel(int amount) throws IOException{
        return UDPPeerServer.cancel(amount);
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    
    
    /**
     * Retorno el tiempo de mi reloj en milisegundos
     * @return 
     */
    public static long getMyTimeInMillis(){
        long ret=System.currentTimeMillis() - timeSyncronized; //tiempo que transcurrio
        System.out.println("time "+ret);
        return ret;
    }
    public static void main(String args[]) throws Exception {
        Peer peer = new Peer();
        new UDPPeerServer(peer).start(); //empiezo a escuchar en UDP puerto 9876
        InetAddress IPAddress = InetAddress.getByName(IPJOAKO);

      
        peer.runTelnetServer(); //pongo a correr el telnet en el puerto 5217

    }

}
