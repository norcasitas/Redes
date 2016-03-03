/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nico
 */
public class TelnetPeerServer extends Thread {

    private Socket clientSocket;
    private String loginName;
    private String password;
    private String command;
    private int parameter;
    private Peer peer;
    private BufferedReader bufferIn;
    DataOutputStream bufferOut;

    TelnetPeerServer(Socket CSoc, Peer peer) throws Exception {
        clientSocket = CSoc; //creo el socket para el cliente
        this.peer = peer;
        System.out.println("Client Connected ...");
        System.out.println("Waiting for UserName And Password");
        bufferIn=new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
        bufferOut = new DataOutputStream(clientSocket.getOutputStream()); //preparo el socket para la salida
        bufferOut.writeChars("Ingrese user\nCentral Promt> ");
        loginName = bufferIn.readLine();
        bufferOut.writeChars("Ingrese pass\nCentral Promt> ");
        password = bufferIn.readLine();
        start(); //inicio el thread para leer los comandos de este cliente
    }

    public void run() {
        try {
            BufferedReader brFin = new BufferedReader(new FileReader("Passwords.txt"));
            String LoginInfo = "";
            boolean allow = false;
            while ((LoginInfo = brFin.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(LoginInfo);
                if (loginName.equals(st.nextToken()) && password.equals(st.nextToken())) {
                    bufferOut.writeChars("ALLOWED\nCentral Promt> ");
                    allow = true;
                    break;
                }
            }
            brFin.close();
            if (allow == false) {
                bufferOut.writeChars("NOT_ALLOWED\nCentral Promt> ");
            }
            while (allow) {
                command = bufferIn.readLine().toLowerCase();
                switch (command) {
                    case "reserve":
                        String s= bufferIn.readLine();
                        parameter = Integer.valueOf(s);
                        Boolean result = peer.reserve(parameter);
                        bufferOut.writeChars(result.toString() +"\nCentral Prompt> ");
                        break;
                    case "available":
                        Integer available = peer.available();
                        bufferOut.writeChars(available.toString()+"\nCentral Prompt> ");
                        break;
                    case "cancel":
                        parameter = Integer.valueOf(bufferIn.readLine());
                        boolean cancel = peer.cancel(parameter);
                        if (cancel) {
                            bufferOut.writeChars("Cancelaste exitosamente " + parameter + " pasajes \nCentral Prompt> ");
                        } else {
                            bufferOut.writeChars("Error: La cantidad de asientos a cancelar tiene que ser menor a la de asientos reservados \nCentral Prompt> ");
                        }
                        break;
                    case "quit":
                        allow = false;
                        clientSocket.close();
                        break;
                    default:
                        bufferOut.writeChars("Comando incorrecto\nCentral Prompt>");
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TelnetPeerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
