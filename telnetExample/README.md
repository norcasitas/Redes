To compile the project, run compile.sh.

A file named 'ips.txt' will autogenerate, this contains the IPs of the centrals (Peers), the format of the lines in this file must be: IP_CENTRAL UDP_PORT TELNET_PORT, for instance: 192.168.0.11 9000 5000. Each new line must be a different central (Peer).

To run the project:
Run in a terminal java -jar proyecto_redes.jar UDP_PORT TELNET_PORT
Where UDP_PORT and TELNET_PORT are the ports in which this peer wants to listen through UDP and TELNET respectively.

To conect to a central (Peer) as a client open a terminal and run: telnet IP_CENTRAL TELNET_PORT where IP_CENTRAL is the IP of the peer that the client wants to establish connection and TELNET_PORT is the port in which the peer listens to TELNET.