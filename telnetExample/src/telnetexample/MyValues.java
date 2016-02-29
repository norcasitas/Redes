/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;


public final class MyValues {
    public static final IPports MYIP = new IPports("localhost", 9876,5217); //mantengo ip, puerto udp, puerto telnet
    public static final IPports IPCENTRAL1 = new IPports("192.168.0.16", 9876,5217); //mantengo ip, puerto udp, puerto telnet
    public static final int MSGAVAILABLE = 1;
    public static final int MSGRESERVE = 2;
    public static final int MSGCANCEL = 3;
    public static final int MSGACK = 4;
    public static final int MSGENTER = 5;
    public static final int MSGRELEASE = 6;
}
