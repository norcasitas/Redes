#!/bin/bash
# -*- ENCODING: UTF-8 -*-
echo "COMPILANDO PROYECTO DE REDES DE HEREDIA - JAIMEZ - PEREYRA ORCASITAS"
mkdir class
echo "Manifest-Version: 1.0
Main-Class: proyecto_redes/Peer" > class/manifest.mf 
javac -d class src/proyecto_redes/*
cd class
jar cmf manifest.mf ../proyecto_redes.jar proyecto_redes/*.class
cd ..
rm -R class
echo "192.168.0.111 9876 5217" > ips.txt
echo "user pass" > Passwords.txt
echo "COMPILACION TERMINADA, CORRA: java -jar proyecto_redes.jar UDP_PORT TELNET_PORT"     
exit