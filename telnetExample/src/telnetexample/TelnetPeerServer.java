/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import static telnetexample.MyValues.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nico
 */
public class TelnetPeerServer extends Thread {

    private Socket ClientSocket;
    private DataInputStream din;
    private DataOutputStream dout;
    private String LoginName;
    private String Password;
    private String command;
    private int parameter;

    TelnetPeerServer(Socket CSoc) throws Exception {
        ClientSocket = CSoc; //creo el socket para el cliente
        System.out.println("Client Connected ...");
        DataInputStream din = new DataInputStream(ClientSocket.getInputStream()); //preparo el socket para la entrada
        DataOutputStream dout = new DataOutputStream(ClientSocket.getOutputStream()); //preparo el socket para la salida
        System.out.println("Waiting for UserName And Password");
        LoginName = din.readUTF(); //leo desde el socket el usuario
        Password = din.readUTF(); //leo desde el socket la pass
        start(); //inicio el thread para leer los comandos de este cliente
    }

    public void run() {
        try {
            DataInputStream din = new DataInputStream(ClientSocket.getInputStream()); //preparo el socket para la entrada
            DataOutputStream dout = new DataOutputStream(ClientSocket.getOutputStream()); //preparo el socket para la salida

            BufferedReader brFin = new BufferedReader(new FileReader("Passwords.txt"));

            String LoginInfo = new String("");
            boolean allow = false;

            while ((LoginInfo = brFin.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(LoginInfo);
                if (LoginName.equals(st.nextToken()) && Password.equals(st.nextToken())) {
                    dout.writeUTF("ALLOWED");
                    allow = true;
                    break;
                }
            }

            brFin.close();

            if (allow == false) {
                dout.writeUTF("NOT_ALLOWED");
            }

            while (allow) {
                command = din.readUTF().toLowerCase();

                switch (command) {
                    case "reserve":
                        parameter = Integer.valueOf(din.readUTF());
                        Boolean result = Peer.reserve(parameter);
                        dout.writeUTF(result.toString());
                        break;
                    case "available":
                        Integer available = Peer.available();
                        dout.writeUTF(available.toString());
                        break;
                    case "cancel":
                        parameter = Integer.valueOf(din.readUTF());
                        boolean cancel = Peer.cancel(parameter);
                        if (cancel) {
                            dout.writeUTF("Cancelaste exitosamente " + parameter + " pasajes");
                        } else {
                            dout.writeUTF("Error: La cantidad de asientos a cancelar tiene que ser menor a la de asientos reservados");
                        }
                        break;
                    case "quit":
                        allow = false;
                        ClientSocket.close();
                        break;
                    default: 
                        dout.writeUTF("Comando incorrecto");
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TelnetPeerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
