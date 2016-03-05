/*
 * Class that holds the IP address and ports utilized by a peer.
 */
package proyecto_redes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IPPorts {

    private InetAddress ip; //Peer IP address
    private int portUDP; //UDP port where the peer listens
    private int portTelnet; //Telnet port where the peer listens

    /**
     * Constructor for IPPorts class
     * Requires a specified IP address, UDP Port and Telnet Port
     * @param ip IP address of the peer
     * @param portUDP UDP port of the peer
     * @param portTelnet Telnet port of the peer
     */
    public IPPorts(String ip, int portUDP, int portTelnet) {
        try {
            this.ip = InetAddress.getByName(ip);
            this.portUDP = portUDP;
            this.portTelnet = portTelnet;
        } catch (UnknownHostException ex) {
            Logger.getLogger(IPPorts.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *Getter for the IP address of the peer
     * @return the IP address of the peer
     */
    public InetAddress getIp() {
        return ip;
    }

    /**
     * Setter for the IP of the peer
     * @param ip new peer IP address
     */
    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    /**
     * Getter for the UDP port of the peer
     * @return the UDP port of the peer
     */
    public int getPortUDP() {
        return portUDP;
    }

    /**
     * Setter for the UDP port of the peer
     * @param portUDP new peer UDP port
     */
    public void setPortUDP(int portUDP) {
        this.portUDP = portUDP;
    }

    /**
     * Getter for the Telnet port of the peer
     * @return the telnet port of the peer
     */
    public int getPortTelnet() {
        return portTelnet;
    }

    /**
     * Setter for the Telnet port of the peer
     * @param portTelnet new port of the peer
     */
    public void setPortTelnet(int portTelnet) {
        this.portTelnet = portTelnet;
    }

    @Override
    public String toString() {
        return "ip:" + getIp().getHostAddress() + " UDP port:" + getPortUDP() + " TELNET port:" + getPortTelnet();
    }

} //end of IPPorts class.
