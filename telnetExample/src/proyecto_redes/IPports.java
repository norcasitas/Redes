/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_redes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nico
 */
public class IPports {

    private InetAddress ip; //ip del peer
    private int portUDP; //puerto udp donde escucha el peer
    private int portTelnet; //puerto telnet donde escucha el peer

    public IPports(String ip, int portUDP, int portTelnet) {
        try {
            this.ip = InetAddress.getByName(ip);
            this.portUDP = portUDP;
            this.portTelnet = portTelnet;
        } catch (UnknownHostException ex) {
            Logger.getLogger(IPports.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPortUDP() {
        return portUDP;
    }

    public void setPortUDP(int portUDP) {
        this.portUDP = portUDP;
    }

    public int getPortTelnet() {
        return portTelnet;
    }

    public void setPortTelnet(int portTelnet) {
        this.portTelnet = portTelnet;
    }

    public String toString() {
        return "ip:" + ip.getHostAddress() + " UDP port:" + portUDP + " TELNET port:" + portTelnet;
    }

}
