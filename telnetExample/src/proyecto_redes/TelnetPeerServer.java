/*
 * Class that represents a thread that listens for the messages passed through the
 * Telnet port of the peer.
 */
package proyecto_redes;

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

public class TelnetPeerServer extends Thread {

    private Socket clientSocket;
    private String loginName;
    private String password;
    private String command;
    private int parameter;
    private Peer peer;
    private BufferedReader bufferIn;
    DataOutputStream bufferOut;

    /**
     * Constructor: Receives a socket and the Peer that creates the thread.
     *
     * @param CSoc The socket for the client.
     * @param peer The peer that creates the thread.
     * @throws Exception
     */
    public TelnetPeerServer(Socket CSoc, Peer peer) throws Exception {
        clientSocket = CSoc; //Creates the socket for the Telnet Client
        this.peer = peer;
        System.out.println("Client Connected ...");
        System.out.println("Waiting for UserName And Password");
        bufferIn = new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
        bufferOut = new DataOutputStream(clientSocket.getOutputStream()); //Prepares the socket for the Output
        bufferOut.writeChars("Username:\nCentral Prompt> ");
        loginName = bufferIn.readLine();
        bufferOut.writeChars("Password:\nCentral Prompt> ");
        password = bufferIn.readLine();
        start(); //Start the thread that reads the commands of the Telnet Client
    }

    @Override
    public void run() {
        try {
            BufferedReader brFin = new BufferedReader(new FileReader("Passwords.txt"));
            String LoginInfo;
            boolean allow = false;
            //Checks the username and password to see if they are allowed.
            while ((LoginInfo = brFin.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(LoginInfo);
                if (loginName.equals(st.nextToken()) && password.equals(st.nextToken())) {
                    bufferOut.writeChars("ALLOWED\nCentral Prompt> ");
                    allow = true;
                    break;
                }
            }
            brFin.close();
            if (allow == false) {
                bufferOut.writeChars("NOT_ALLOWED\nCentral Prompt> ");
            }
            while (allow) {
                //Read the commands through the client.
                command = bufferIn.readLine().toLowerCase();
                switch (command) {
                    case "reserve":
                        String s = bufferIn.readLine();
                        parameter = Integer.valueOf(s);
                        Boolean result = peer.reserve(parameter);
                        if (result) {
                            bufferOut.writeChars(parameter + "tickets reserved\nCentral Prompt> ");
                        } else {
                            bufferOut.writeChars("Error: bad parameter\nCentral Prompt> ");
                        }
                        break;
                    case "available":
                        Integer available = peer.available();
                        System.out.println(available);
                        bufferOut.writeChars("Available seats: " + available.toString() + "\nCentral Prompt> ");
                        break;
                    case "cancel":
                        parameter = Integer.valueOf(bufferIn.readLine());
                        boolean cancel = peer.cancel(parameter);
                        if (cancel) {
                            bufferOut.writeChars(parameter + " tickets cancelled\nCentral Prompt> ");
                        } else {
                            bufferOut.writeChars("Error: bad parameter\nCentral Prompt> ");
                        }
                        break;
                    case "quit":
                        allow = false;
                        clientSocket.close();
                        break;
                    default:
                        bufferOut.writeChars("No such command\nCentral Prompt> ");
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TelnetPeerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

} // End of TelnetPeerServer class.
