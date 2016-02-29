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
    private static byte[] sendData;
    private static byte[] receiveData;
    private int receivedAck;
    private static int actionFromClient; //utilizado para saber si voy a reservar, cancelar, etc
    private static int amount;
    private static Object result; //utilizado para la sincro
    private Peer peer;
    private static Object LOCK = new Object(); // just something to lock on

    public UDPPeerServer(Peer peer) throws SocketException {
        serverSocket = new DatagramSocket(MYIP.getPortUDP());
        receiveData = new byte[20];
        this.peer = peer;
    }

    /**
     * Realiza un broadcast con la accion que desea realizar el peer
     *
     * @param action
     * @throws UnknownHostException
     * @throws SocketException
     * @throws IOException
     */
    public static void broadcast(int action, long time, long pid) throws UnknownHostException, SocketException, IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        java.util.Date date = new java.util.Date();
        //preparo un string que es por ejemplo 1 12386123 pid donde representa la 
        //accion, su tiempo, y el pid del proceso
        String sentence = action + " " + String.valueOf(time) + " " + pid;
        if (MSGRELEASE == action) {
            sentence+=" " + Peer.getSeats();
        }
        sendData = sentence.getBytes();
        //lo envio a cada proceso, no espero respuesta sincronica
        for (IPports ip : Peer.ips) {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getIp(), Peer.getPortByIP(1, ip.getIp()));
            clientSocket.send(sendPacket);
        }
        clientSocket.close();

    }

    @Override
    public void run() {
        while (true) {
            try {
                //empiezo a escuchar mensajes que entren por udp
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                DatagramSocket clientSocket = new DatagramSocket();
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                byte[] data = receivePacket.getData();
                System.out.println("RECEIVED in thread udppeerserver: " + sentence);
                int size = 0;
                while (size < data.length) {//obtengo el tamaño del mensaje, eliminando la basura
                    if (data[size] == 0) {
                        break;
                    }
                    size++;
                }
                // Specify the appropriate encoding as the last argument
                String str = new String(data, 0, size, "UTF-8");

                //hago un split para obtener la accion a realizar
                int action = Integer.valueOf(str.split(" ")[0]);
                //encolo en la cola, un objeto que contiene el timestamp y el pid del proceso del peer que lo envio
                long time = Long.valueOf(str.split(" ")[1]);
                long pid = Long.valueOf(str.split(" ")[2]);
                QueueObject qb = new QueueObject(time, pid);
                Peer.updateTime(time);
                if (action != MSGACK) {
                    //SI NO REcIbO UN ACK, PUEDO RECIbIR UN RELEASE O UN ENTER.
                    if (action == MSGRELEASE) {
                        Peer.dequeue();
                        peer.setSeats(Integer.valueOf(str.split(" ")[3]));
                        if (Peer.getFirstPid() == Peer.getPid()) {
                            //ES MI TURNO TENGO QUE EJECUTAR LA ACCION (falta) Y MANDO BRODCAST
                            switch (actionFromClient) {
                                case MyValues.MSGRESERVE:
                                    //tengo que realizar una reserva
                                    peer.getVehicle().reserve(amount);
                                    int i=0;
                                    while(i<10000){
                                        i++;
                                        System.out.print("ciclo");
                                    }
                                    synchronized (LOCK) {
                                        LOCK.notifyAll();
                                    }
                                    break;
                                case MyValues.MSGAVAILABLE:
                                    //Verifico asientos disponibles
                                    peer.getVehicle().available();
                                    synchronized (LOCK) {
                                        LOCK.notifyAll();
                                    }
                                    break;
                                case MyValues.MSGCANCEL:
                                    peer.getVehicle().cancel(amount);
                                    synchronized (LOCK) {
                                        LOCK.notifyAll();
                                    }
                                    break;
                            }
                            Peer.dequeue();
                            broadcast(MSGRELEASE, Peer.getMyTimeInMillis(), Peer.getPid());
                        }
                    }
                    if (action == MSGENTER) {
                        //ENCOLO Y MANDO ACK
                        Peer.enqueue(qb);
                        //con esto logro sincronizar, si fuera necesario, el tiempo
                        String ds = MSGACK + " " + String.valueOf(Peer.getMyTimeInMillis()) + " " + Peer.getPid();
                        sendData = ds.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), Peer.getPortByIP(1, receivePacket.getAddress()));
                        clientSocket.send(sendPacket); // le respondo que está todo bien
                    }
                } else {
                    System.out.println("DIERON IGUALES!");
                    receivedAck++;
                    if (receivedAck == Peer.ips.length) {//recibi todos los ack
                        if (Peer.getFirstPid() == Peer.getPid()) {
                            //y soy yo el que sigue en la cola, entonces es mi turno
                            receivedAck = 0;
                            //aca estoy en la zona critica, debería hacer lo que necesito
                            //ES MI TURNO TENGO QUE EJECUTAR LA ACCION (falta) Y MANDO BRODCAST
                            switch (actionFromClient) {
                                case MyValues.MSGRESERVE:
                                    //tengo que realizar una reserva
                                    result = peer.getVehicle().reserve(amount);
                                    int i=0;
                                    while(i<10000){
                                        i++;
                                        System.out.print("ciclo");
                                    }
                                    synchronized (LOCK) {
                                        LOCK.notifyAll();
                                    }
                                    break;
                                case MyValues.MSGAVAILABLE:
                                    //Tengo que verificar la cantidad de asientos disponibles.
                                    result = peer.getVehicle().available();
                                    synchronized (LOCK) {
                                        LOCK.notifyAll();
                                    }
                                    break;
                                case MyValues.MSGCANCEL:
                                    //Cancelo
                                    result = peer.getVehicle().cancel(amount);
                                    synchronized (LOCK) {
                                        LOCK.notifyAll();
                                    }
                                    break;
                            }
                            Peer.dequeue();
                            broadcast(MSGRELEASE, Peer.getMyTimeInMillis(), Peer.getPid());
                        }

                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static boolean reserve(int amount) throws SocketException, IOException {
        System.out.println("line 156 reserve" + amount);
        actionFromClient = MyValues.MSGRESERVE;
        UDPPeerServer.amount = amount;
        long time = Peer.getMyTimeInMillis();
        QueueObject qb = new QueueObject(time, Peer.getPid());
        Peer.enqueue(qb);
        //notifico a todos que quiero usar el recurso compartido
        broadcast(MSGENTER, time, Peer.getPid());
        synchronized (LOCK) {
            while (result == null) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    // treat interrupt as exit request
                    break;
                }
            }
        }
        boolean ret = (boolean) result;
        result = null;
        return ret;
    }

    public static int available() throws SocketException, IOException {
        actionFromClient = MyValues.MSGAVAILABLE;
        long time = Peer.getMyTimeInMillis();
        QueueObject qb = new QueueObject(time, Peer.getPid());
        Peer.enqueue(qb);
        broadcast(MSGENTER, time, Peer.getPid());
        synchronized (LOCK) {
            while (result == null) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    // treat interrupt as exit request
                    break;
                }
            }
        }
        int ret = (int) result;
        result = null;
        return ret;
    }

    public static boolean cancel(int amount) throws SocketException, IOException {
        actionFromClient = MyValues.MSGCANCEL;
        UDPPeerServer.amount = amount;
        long time = Peer.getMyTimeInMillis();
        QueueObject qb = new QueueObject(time, Peer.getPid());
        Peer.enqueue(qb);
        broadcast(MSGENTER, time, Peer.getPid());
        synchronized (LOCK) {
            while (result == null) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    // treat interrupt as exit request
                    break;
                }
            }
        }
        boolean ret = (boolean) result;
        result = null;
        return ret;
    }

}
