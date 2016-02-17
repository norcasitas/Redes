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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 * @author nico
 */
public class TelnetServer {

    public static void main(String args[]) throws Exception {
        ServerSocket Soc = new ServerSocket(5217);
        while (true) { // en este while voy recibiendo los clientes
            Socket CSoc = Soc.accept();
            AcceptTelnetClient1 ob = new AcceptTelnetClient1(CSoc);
        }
    }

}

class AcceptTelnetClient1 extends Thread {

    Socket ClientSocket;
    DataInputStream din;
    DataOutputStream dout;
    String LoginName;
    String Password;

    AcceptTelnetClient1(Socket CSoc) throws Exception {
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
                String strCommand;
                strCommand = din.readUTF();
                if (strCommand.equals("quit")) {
                    allow = false;
                } else {
                    Runtime rt = Runtime.getRuntime();

                    Process p = rt.exec("TelnetServer.bat " + strCommand);

                    String stdout = new String("");
                    String st;
                    DataInputStream dstdin = new DataInputStream(p.getInputStream());
                    while ((st = dstdin.readLine()) != null) {
                        stdout = stdout + st + "\n";
                    }
                    dstdin.close();
                    dout.writeUTF(stdout);
                }
            }
            ClientSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
