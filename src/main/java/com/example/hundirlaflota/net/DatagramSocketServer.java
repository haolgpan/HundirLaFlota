package com.example.hundirlaflota.net;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

public class DatagramSocketServer {
    private DatagramSocket socket;
    private int fi,turno;
    private  byte [] sendingDataEnemy ;
    private String nom=null, nom2=null, ganador="";
    private boolean acabat, encert = false, gameOver; //Variables para Controlar ganador
    private ArrayList <String> arrayTirades= new ArrayList<>();
    private String posicionesJug1, posicionesJug2;
    private ArrayList <String> posicionBarcosJugador1= new ArrayList<>();
    private ArrayList <String> posicionBarcosJugador2= new ArrayList<>();

    private boolean jugada=false;
    private String jugadaAnterior ="";
    //Instàciar el socket
    public void init(int port) throws SocketException {
        socket = new DatagramSocket(port);
        acabat = false;
        fi=-1;
        System.out.printf("Servidor obert pel port %d%n",port);
    }
    public void runServer() throws IOException {
        byte[] receivingData = new byte[1024];
        String[] nomSplit = new String[0];
        InetAddress clientIP;
        int clientPort;
        while (!acabat) {
            DatagramPacket packet = new DatagramPacket(receivingData, 1024);
            socket.receive(packet);
            if (sendingDataEnemy == null) {
                sendingDataEnemy = "Esperando respuesta , turno par".getBytes();
            }
            // Llegim el port i l'adreça del client per on se li ha d'enviar la resposta
            clientIP = packet.getAddress();
            clientPort = packet.getPort();
            // Si el nom es null perque no hi han jugadors possem el nom del primer jugador a la variable nom
            if (nom == null) {
                String packet1 = new String(processData(packet.getData(), packet.getLength()), 0, packet.getLength());
                nomSplit = packet1.split(" ");
                nom = nomSplit[0];
                posicionesJug1 = nomSplit[1];
                String[] posJug1Array = posicionesJug1.split(",");
                Collections.addAll(posicionBarcosJugador1, posJug1Array);
            }
            // Si el nom NO es null i ens arriba una consulta del torn enviem el torn al jugador que el demana
            else if (nom != null) {
                String packet1 = new String(processData(packet.getData(), packet.getLength()), 0, packet.getLength());
                nomSplit = packet1.split(" ");
                if (nomSplit[0].equals("consultaTurno")) {

                   // if(jugada){
                   //     packet = new DatagramPacket(arrayTirades.get(arrayTirades.size() - 1).getBytes(), arrayTirades.get(arrayTirades.size() - 1).getBytes().length, clientIP, clientPort);
                   //     socket.send(packet);
                   //     jugada=false;
                   // }


                    if(gameOver){
                        String blanco = ganador+ " gameover";
                        packet = new DatagramPacket(blanco.getBytes(), blanco.getBytes().length, clientIP, clientPort);
                        socket.send(packet);
                    }
                    if (encert) {
                        encert = false;
                        String blanco = "blanco";
                        packet = new DatagramPacket(blanco.getBytes(), blanco.getBytes().length, clientIP, clientPort);
                        socket.send(packet);
                    } else {
                        packet = new DatagramPacket(String.valueOf(turno).getBytes(), String.valueOf(turno).getBytes().length, clientIP, clientPort);
                        socket.send(packet);
                    }
                }
                else if (nomSplit[1].equals("ganador")) {
                    gameOver=true;
                    ganador=nomSplit[0];

                }
                // Si el nom NO es null perque ja hi ha un jugador possem el nom del segon jugador a la variable nom2
                else if (!nom.equals(nomSplit[0])) {
                    nom2 = nomSplit[0];
                    posicionesJug2 = nomSplit[1];
                    String[] posJug2Array = posicionesJug2.split(",");
                    Collections.addAll(posicionBarcosJugador2, posJug2Array);
                }
            }
            //Continua el fil
            if (arrayTirades.size() > 1 && !nomSplit[0].equals("consultaTurno")) {
                sendingDataEnemy = arrayTirades.get(arrayTirades.size() - 2).getBytes();
                // Comprobacion de disparo certero
                String jugadaAnterior = arrayTirades.get(arrayTirades.size() - 1).split(" ")[1].replace("boton", "botonplayer");
                ArrayList<String> posicionBarcosJugador;
                if (nom.equals(nomSplit[0])) {
                    posicionBarcosJugador = posicionBarcosJugador2;
                } else if (nom2.equals(nomSplit[0])) {
                    posicionBarcosJugador = posicionBarcosJugador1;
                } else {
                    posicionBarcosJugador = new ArrayList<>();
                }
                if (posicionBarcosJugador.contains(jugadaAnterior)) {
                    encert = true;
                }
                packet = new DatagramPacket(sendingDataEnemy, sendingDataEnemy.length, clientIP, clientPort);
                socket.send(packet);
                turno++;
               // jugada=true;
            } else if (!nomSplit[0].equals("consultaTurno") && !nomSplit[1].contains(",")) {
                packet = new DatagramPacket(sendingDataEnemy, sendingDataEnemy.length, clientIP, clientPort);
                socket.send(packet);
                turno++;
            }
        }
    }
    //El server retorna al client la jugada de l'anterior jugador
    public byte[] processData(byte[] data, int lenght) {
        String jugada = new String(data,0,lenght);
        if (!jugada.equals("consultaTurno")&&!jugada.contains(",")){
            arrayTirades.add(jugada);
        }
        return data;
    }
}
