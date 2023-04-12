package com.example.hundirlaflota.net;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by jordi on 26/02/17.
 * Exemple Servidor UDP extret dels apunts IOC i ampliat
 * El seu CLient és DatagramSocketClient
 */
public class DatagramSocketServer {
    private DatagramSocket socket;
    private int fi;
    private int turno;
    private  byte [] sendingDataEnemy ;

    private String nom=null;
    private String nom2=null;
    private int comptadorClients;
    private boolean acabat;
    private boolean complert = false;
    private ArrayList <String> arrayTirades= new ArrayList<>();
    //Instàciar el socket
    public void init(int port) throws SocketException {
        socket = new DatagramSocket(port);
        acabat = false;
        fi=-1;
        System.out.printf("Servidor obert pel port %d%n",port);
    }

    public void runServer() throws IOException {
        byte [] receivingData = new byte[1024];
        byte [] sendingData;
        InetAddress clientIP;
        int clientPort;

        while(!acabat ) {
            DatagramPacket packet = new DatagramPacket(receivingData,1024);
            socket.receive(packet);
            sendingData = processData(packet.getData(),packet.getLength());
            if(sendingDataEnemy==null){
                sendingDataEnemy = new String("Esperando respuesta , turno par").getBytes();
                turno++;
            }
                //Llegim el port i l'adreça del client per on se li ha d'enviar la resposta
            clientIP = packet.getAddress();
            clientPort = packet.getPort();
            if(nom==null){
                String packet1= new String(processData(packet.getData(),packet.getLength()),0,packet.getLength());
                String[]nomSplit= packet1.split(" ");
                nom=nomSplit[0];
                comptadorClients++;
                System.out.println(nom);
                turno++;


            }
            else if (nom!=null){
                String packet1= new String(processData(packet.getData(),packet.getLength()),0,packet.getLength());
                String[]nomSplit= packet1.split(" ");
                if (nomSplit[0].equals("consultaTurno")){
                    packet = new DatagramPacket(String.valueOf(turno).getBytes(),String.valueOf(turno).getBytes().length,clientIP,clientPort);
                    socket.send(packet);
                }
                if(!nom.equals(nomSplit[0])) {
                    nom2 = nomSplit[0];
                    comptadorClients++;
                    System.out.println(nom2);
                    turno++;
                }
                else  System.out.println(nom);


            }
            System.out.println("sendEnemy "+ new String(sendingDataEnemy));
            if (arrayTirades.size()>3) {
                System.out.println("ArrayJugada rebuda correctament= "+ arrayTirades.get(arrayTirades.size()-3));
                sendingDataEnemy = arrayTirades.get(arrayTirades.size()-3).getBytes();
                packet = new DatagramPacket(sendingDataEnemy,sendingDataEnemy.length,clientIP,clientPort);
                socket.send(packet);
            }
            else {
                packet = new DatagramPacket(sendingDataEnemy, sendingDataEnemy.length, clientIP, clientPort);
                socket.send(packet);
            }
          //  sendingDataEnemy=sendingData;

        }
    }

    //El server retorna al client el mateix missatge que li arriba però en majúscules
    public byte[] processData(byte[] data, int lenght) {
        String nombre = new String(data,0,lenght);
        System.out.println("rebut->"+nombre);
        arrayTirades.add(nombre);
       // fi = ns.comprova(Integer.parseInt(nombre));
   //     if(fi==0) acabat=true;
       // byte[] resposta = ns.comprova(nombre).getBytes();
        return data;
    }




}
