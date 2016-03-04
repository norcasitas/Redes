/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import static telnetexample.MyValues.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Peer {

    private LinkedList<IPports> ips = new LinkedList<>(); //almacena las ips de las distintas centrales
    private Vehicle vehicle; //representación del colectivo
    private LinkedList<QueueObject> queue;//cola de tareas
    private int time; //utilizado para llevar la diferencia con el reloj propio
    private long pid; //id del proceso peer
    private UDPPeerServer udpPeerServer; // manejador de los comandos de udp
    private LinkedList<IPports> allIps = new LinkedList<>();

    public Peer() throws SocketException, IOException {
        vehicle = new Vehicle();
        readIpsFromFile();
        queue = new LinkedList();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //preparo el buffer para leer desde la terminal
        MYIP.setPortUDP(Integer.valueOf(br.readLine()));
        MYIP.setPortTelnet(Integer.valueOf(br.readLine()));
        pid = Long.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        time = 0;
        udpPeerServer = new UDPPeerServer(this); //empiezo a escuchar en UDP puerto 9876
        notifyConection();
    }

    private void readIpsFromFile() throws FileNotFoundException, IOException {
        BufferedReader brFin = new BufferedReader(new FileReader("ips.txt"));
        String ipWithPorts = "";
        while ((ipWithPorts = brFin.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(ipWithPorts);
            allIps.add(new IPports(st.nextToken(), Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken())));
        }
    }

    public void init() throws Exception {
        udpPeerServer.start();
        runTelnetServer();
    }

    private void notifyConection() throws SocketException, IOException {
        //preparo un string que es por ejemplo 1 12386123 pid donde representa la 
        //accion, su tiempo, y el pid del proceso
        String sentence = MSGNEWCONECTION + " " + String.valueOf(time) + " " + pid;
        byte[] sendData = sentence.getBytes();
        //lo envio a cada proceso, no espero respuesta sincronica
        for (IPports ip : allIps) {
            DatagramSocket clientSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getIp(), ip.getPortUDP());
            clientSocket.send(sendPacket);
            clientSocket.close();
        }

    }

    /**
     * type es el tipo de puerto que desea retornar, 1 para udp, 2 para telnet
     * si no encuentra la ip, retorna -1
     *
     * @param type
     * @param ip
     * @return numero de puerto
     */
    public int getPortByIP(int type, InetAddress ip) {
        for (IPports ipPort : ips) {
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

    public IPports getIPPortsByIP(InetAddress ip) {
        for (IPports ipPort : allIps) {
            if (ip.getHostAddress().equals(ipPort.getIp().getHostAddress())) {
                return ipPort;
            }
        }
        return null;
    }

    /**
     * Encola la terea de un peer en la posición que le corresponde
     *
     * @param qb
     */
    public void enqueue(QueueObject qb) {
        int i = 0;
        if (queue.size() > 0) {
            while (i < queue.size() && (queue.get(i).getTime() < qb.getTime())) {
                i++;
            }
            //si son el mismo tiempo, y además el pid que estaba en la cola es menor, 
            //encolo despues de este
            while (i < queue.size() && queue.get(i).getTime() == qb.getTime() && queue.get(i).getPid() < qb.getPid()) {
                i++;
            }
        }
        queue.add(i, qb);
    }

    /**
     * obtengo el pid del primero en la cola, en caso de ser vacio, retorno -1
     *
     * @return
     */
    public long getFirstPid() {
        return queue.size() > 0 ? queue.getFirst().getPid() : -1;
    }

    /**
     * Desencolo el primero en la cola
     *
     * @return
     */
    public QueueObject dequeue() {

        return queue.isEmpty() ? null : queue.removeFirst();
    }

    /**
     * Retorna el identificador del proceso
     *
     * @return
     */
    public long getPid() {
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
     * actualizo el reloj siempre y cuando el parametro de entrada sea mayor a
     * mi tiempo
     *
     * @param millis
     */
    public void updateTime(int newTime) {
        //si el tiempo que transcurrió desde la ultima sincro es menor al tiempo de entrada,
        //me sincronizo
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
        time++; //hace falta o no acá?
        return vehicle.available();
    }

    public boolean cancel(int amount) throws IOException {
        time++;
        return udpPeerServer.cancel(amount);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int getSeats() {
        return vehicle.getReserved();
    }

    public void setSeats(int seats) {
        vehicle.setSeats(seats);
    }

    /**
     * Retorno el tiempo de mi reloj en milisegundos
     *
     * @return
     */
    public int getTime() {
        return time;
    }

    public LinkedList<IPports> getIps() {
        return ips;
    }

    public void addIP(IPports ip) {
        ips.add(ip);
    }

    public LinkedList<QueueObject> getQueue() {
        return queue;
    }

    /**
     * Llena de basura la cola para simular que tiene objetos antes que el y no
     * son de el
     *
     * @param size
     */
    public void setQueueWithTrash(int size) {
        //System.out.println("viendo cola antes");
        for (QueueObject q : queue) {
            System.out.println(q.toString());
        }
        queue.clear();
        for (int i = 0; i < size; i++) {
            queue.add(new QueueObject(-1, -1));
        }
        //System.out.println("viendo cola despues");
        for (QueueObject q : queue) {
            System.out.println(q.toString());
        }
    }

    public static void main(String args[]) throws Exception {
        new Peer().init();
    }

}
