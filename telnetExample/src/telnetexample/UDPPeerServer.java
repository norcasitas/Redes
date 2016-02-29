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
    public void broadcast(int action, long time, long pid) throws UnknownHostException, SocketException, IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        java.util.Date date = new java.util.Date();
        //preparo un string que es por ejemplo 1 12386123 pid donde representa la 
        //accion, su tiempo, y el pid del proceso
        String sentence = action + " " + String.valueOf(time) + " " + pid;
        if (MSGRELEASE == action) {
            sentence += " " + peer.getSeats();
        }
        sendData = sentence.getBytes();
        //lo envio a cada proceso, no espero respuesta sincronica
        for (IPports ip : peer.getIps()) {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getIp(), peer.getPortByIP(1, ip.getIp()));
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
                peer.updateTime(time); //sincronizo el reloj
                switch (action) {
                    case MSGRELEASE:
                        peer.dequeue();
                        peer.setSeats(Integer.valueOf(str.split(" ")[3]));
                        if (peer.getFirstPid() == peer.getPid()) {
                            myTurn();
                        }
                        break;
                    case MSGENTER:
                        //ENCOLO Y MANDO ACK
                        peer.enqueue(qb);
                        //con esto logro sincronizar, si fuera necesario, el tiempo
                        String ds = MSGACK + " " + String.valueOf(peer.getMyTimeInMillis()) + " " + peer.getPid();
                        sendData = ds.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), peer.getPortByIP(1, receivePacket.getAddress()));
                        clientSocket.send(sendPacket); // le respondo que está todo bien
                        break;
                    case MSGACK:
                        receivedAck++;
                        if (receivedAck == peer.getIps().size()) {//recibi todos los ack
                            if (peer.getFirstPid() == peer.getPid()) {
                                //y soy yo el que sigue en la cola, entonces es mi turno
                                receivedAck = 0;
                                myTurn();
                            }
                        }
                        break;
                    case MSGNEWCONECTION:
                        peer.addIP(peer.getIPPortsByIP(receivePacket.getAddress()));
                        broadcast(MSGACKNEWCONECTION, peer.getMyTimeInMillis(), peer.getPid());
                        //aca faltaría notificar como está el colectivo y como está la cola
                        break;
                    case MSGACKNEWCONECTION:
                        peer.addIP(peer.getIPPortsByIP(receivePacket.getAddress()));
                        //falta obtener como está el colectivo y la cola, para restaurar
                        break;
                }

            } catch (IOException ex) {
                Logger.getLogger(UDPPeerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean reserve(int amount) throws SocketException, IOException {
        actionFromClient = MyValues.MSGRESERVE;
        UDPPeerServer.amount = amount;
        long time = peer.getMyTimeInMillis();
        QueueObject qb = new QueueObject(time, peer.getPid());
        peer.enqueue(qb);
        //notifico a todos que quiero usar el recurso compartido
        broadcast(MSGENTER, time, peer.getPid());
        lock();
        boolean ret = (boolean) result;
        result = null;
        return ret;
    }

    public int available() throws SocketException, IOException {
        actionFromClient = MyValues.MSGAVAILABLE;
        long time = peer.getMyTimeInMillis();
        QueueObject qb = new QueueObject(time, peer.getPid());
        peer.enqueue(qb);
        broadcast(MSGENTER, time, peer.getPid());
        System.out.println("avaialable");
        lock();
        System.out.println("avaialable 2");
        int ret = (int) result;
        result = null;
        return ret;
    }

    public boolean cancel(int amount) throws SocketException, IOException {
        actionFromClient = MyValues.MSGCANCEL;
        UDPPeerServer.amount = amount;
        long time = peer.getMyTimeInMillis();
        QueueObject qb = new QueueObject(time, peer.getPid());
        peer.enqueue(qb);
        broadcast(MSGENTER, time, peer.getPid());
        lock();
        boolean ret = (boolean) result;
        result = null;
        return ret;
    }

    /**
     * Bloquea el thread corriente si result es null
     */
    private void lock() {
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
    }

    /**
     * Metodo que se ejecuta cuando me toca a mi utilizar el area critica
     */
    private void myTurn() throws SocketException, IOException {
        //ES MI TURNO TENGO QUE EJECUTAR LA ACCION (falta) Y MANDO BRODCAST
        System.out.println("my turn");
        switch (actionFromClient) {
            case MyValues.MSGRESERVE:
                //tengo que realizar una reserva
                result = peer.getVehicle().reserve(amount);
                break;
            case MyValues.MSGAVAILABLE:
                //Verifico asientos disponibles
                result = peer.getVehicle().available();
                break;
            case MyValues.MSGCANCEL:
                result = peer.getVehicle().cancel(amount);
                break;
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        peer.dequeue();
        broadcast(MSGRELEASE, peer.getMyTimeInMillis(), peer.getPid());
    }

}
