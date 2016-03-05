Para compilar el proyecto debe correrse compile.sh

Se autogenerará un archivo ips.txt el cual contiene las ips de las distintas centrales
el formato de la lineas en este archivo debe ser: IP_CENTRAL UDP_PORT TELNET_PORT
por ejemplo 192.168.0.11 9000 5000, en cada nueva linea debe ir una central distinta

para correr el proyecto debe hacerse java -jar proyecto_redes.jar UDP_PORT TELNET_PORT
en donde UDP_PORT y TELNET_PORT son los puertos en el cual se quiere escuchar mensajes a través de UDP 
y TELNET respectivamente.

Para conectarse a una central debe hacerse: telnet IP_CENTRAL TELNET_PORT en donde
IP_CENTRAL es la ip del peer al cual se desea establecer una conexión, y TELNET_PORT
es el puerto en el cual está oyendo ese peer