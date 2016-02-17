/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author nico
 */
public class TelnetClient {

    public static void main(String args[]) throws Exception {
        Socket soc = new Socket("localhost", 5217);
        String loginName;
        String password;
        String command;

        DataInputStream din = new DataInputStream(soc.getInputStream());     //preparo el socket para la entrada   
        DataOutputStream dout = new DataOutputStream(soc.getOutputStream()); //preparo el socket para la salida
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //preparo el buffer para leer desde la terminal

        System.out.println("Welcome to Telnet Client");
        System.out.println("Your Credential Please...");
        System.out.print("Login Name :");

        loginName = br.readLine(); //leo el usuario desde la terminal

        System.out.print("password :");
        password = br.readLine(); //leo la pass desde la terminal

        dout.writeUTF(loginName); //escribo en el socket el usuario
        dout.writeUTF(password); //escribo en el socket la pass

        if (din.readUTF().equals("ALLOWED")) //espero la respuesta si está aceptado
        {
            do {
                System.out.print("< Telnet Prompt command> : ");
                command = br.readLine(); //leo el comando desde la terminal
                dout.writeUTF(command); //escribo el comando en el socket
                if (command.toLowerCase().equals("reserve") || command.toLowerCase().equals("cancel")) {
                    System.out.print("< Telnet Prompt amount> : ");
                    command = br.readLine(); //leo la cantidad de asientos
                    dout.writeUTF(command); //escribo la cantidad de asientos
                }
                if (!command.equals("quit")) { //si el comando es distinto de quit, espero la respuesta y la imprimo
                    System.out.println(din.readUTF());
                }
            } while (!command.equals("quit"));
        }
        soc.close();
    }
}
