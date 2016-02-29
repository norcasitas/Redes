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
import java.net.SocketException;
import java.util.LinkedList;


public class Peer {

    private  LinkedList<IPports> ips = new LinkedList<>(); //almacena las ips de las distintas centrales
    private  Vehicle vehicle; //representación del colectivo
    private  LinkedList<QueueObject> queue;//cola de tareas
    private  long timeSyncronized; //utilizado para llevar la diferencia con el reloj propio
    private  long pid; //id del proceso peer
    private UDPPeerServer udpPeerServer; // manejador de los comandos de udp
    
    public Peer() throws SocketException  {
        vehicle = new Vehicle();
        ips.add(MYIP);
        queue = new LinkedList();
        pid = Long.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        timeSyncronized = System.currentTimeMillis() ; //con esto, hago que el reloj simule empezar en 0
        udpPeerServer = new UDPPeerServer(this); //empiezo a escuchar en UDP puerto 9876
    }
    
    public void init() throws Exception{
        udpPeerServer.start();
        runTelnetServer();
    }
    
    private void notifyConection() throws SocketException, IOException{
        udpPeerServer.broadcast(MSGNEWCONECTION, getMyTimeInMillis(), pid);
    }
    /**
     * type es el tipo de puerto que desea retornar, 1 para udp, 2 para telnet
     * si no encuentra la ip, retorna -1
     * @param type
     * @param ip 
     * @return  numero de puerto
     */
    public int getPortByIP(int type,InetAddress ip){
        for (IPports ipPort: ips){
            if(ipPort.getIp().equals(ip)){
                if(type == 1)
                    return ipPort.getPortUDP();
                if(type == 2)
                    return ipPort.getPortTelnet();
            }
        }
        return -1;
    }

    public IPports getIPPortsByIP(InetAddress ip){
        if(ip.equals(IPCENTRAL1.getIp()))
            return IPCENTRAL1;
        else
            return MYIP;
    }
    /**
     * Encola la terea de un peer en la posición que le corresponde
     * @param qb 
     */
    public void enqueue(QueueObject qb) {
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
    public long getFirstPid(){
        return queue.size()>0? queue.getFirst().getPid(): -1;
    }
    
    /**
     * Desencolo el primero en la cola
     * @return 
     */
    public QueueObject dequeue(){
        return queue.removeFirst();
    }
    
    /**
     * Retorna el identificador del proceso
     * @return 
     */
    public long getPid(){
        return pid;
    }

    public void runTelnetServer() throws IOException, Exception {
        ServerSocket Soc = new ServerSocket(MYIP.getPortTelnet());
        while (true) { // en este while voy recibiendo los clientes
            Socket CSoc = Soc.accept();
            TelnetPeerServer ob = new TelnetPeerServer(CSoc, this);
        }
    }

    /**
     * actualizo el reloj siempre y cuando el parametro de entrada sea mayor a mi tiempo
     * @param millis 
     */
    public void updateTime(long millis){
        //si el tiempo que transcurrió desde la ultima sincro es menor al tiempo de entrada,
        //me sincronizo
        long e= System.currentTimeMillis() - timeSyncronized;
        if(System.currentTimeMillis() - timeSyncronized  < millis){
            timeSyncronized = System.currentTimeMillis() - millis;
        }
    }
    
    /**
     * Transfiero la data que entra por telnet a udp
     * @param amount
     * @return 
     * @throws java.io.IOException
     */
    public  boolean reserve(int amount) throws IOException{
        return udpPeerServer.reserve(amount);
    }

    public  int available() throws IOException {
        return udpPeerServer.available();
    }
    
    public boolean cancel(int amount) throws IOException{
        return udpPeerServer.cancel(amount);
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public int getSeats(){
        return vehicle.getReserved();
    }
    
    public void setSeats(int seats){
        vehicle.setSeats(seats);
    }
    
    /**
     * Retorno el tiempo de mi reloj en milisegundos
     * @return 
     */
    public long getMyTimeInMillis(){
        long ret=System.currentTimeMillis() - timeSyncronized; //tiempo que transcurrio
        return ret;
    }

    public LinkedList<IPports> getIps() {
        return ips;
    }
    
    public void addIP(IPports ip){
        ips.add(ip);
    }
    
    
    
    public static void main(String args[]) throws Exception {
        new Peer().init();
    }

}
