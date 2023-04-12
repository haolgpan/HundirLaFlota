package com.example.hundirlaflota.net;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public abstract class DatagramSocketClient {
    InetAddress serverIP;
    int serverPort;
    DatagramSocket socket;
    Scanner sc;
    String nom;
    private Timer timer;
    private TimerTask timerTask;
    private int lastResponse;
    public boolean turno=true;
    public boolean turnoPar=true;
    public DatagramSocketClient() {
        sc = new Scanner(System.in);
    }

    public void init(String host, int port) throws SocketException, UnknownHostException {
        serverIP = InetAddress.getByName(host);
        serverPort = port;
        socket = new DatagramSocket();
    }
    public void runClientTorn() throws IOException {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] receivedData = new byte[1024];
                    byte[] sendingData;
                    String consultaT="consultaTurno";
                    sendingData = consultaT.getBytes();
                    DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, serverIP, serverPort);
                    socket.send(packet);
                    packet = new DatagramPacket(receivedData, 1024);
                    socket.receive(packet);
                    getResponse(packet.getData(), packet.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, 4000);
    }
    /*
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    runClientTorn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000); // Ejecuta la tarea cada segundo
    }

    public void runClientTorn() throws IOException {
        byte[] receivedData = new byte[1024];
        byte[] sendingData;
        String consultaTurno= "consultaTurno";
        sendingData = consultaTurno.getBytes();
        DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, serverIP, serverPort);
        socket.send(packet);
        packet = new DatagramPacket(receivedData, 1024);
        socket.receive(packet);
        int response = getResponse(packet.getData(), packet.getLength());
        if (response != lastResponse) {
            timerTask.cancel(); // Cancela la tarea del temporizador
        }
        lastResponse = response;
    }


     */
    public void runClient() throws IOException {
        byte [] receivedData = new byte[1024];
        byte [] sendingData;

       // sendingData = getRequest();
        DatagramPacket packet =
                //new DatagramPacket(sendingData,sendingData.length,serverIP,serverPort);
        //socket.send(packet);
        //packet =
                new DatagramPacket(receivedData,1024);
        socket.receive(packet);
        getResponse(packet.getData(), packet.getLength());


    }
//MetodeSend Personalitzat
    public void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, serverIP, serverPort);
        socket.send(packet);
        turno=false;
      //  startTimer();
        runClientTorn();

    }

    //Resta de conversa que se li envia al server
    public abstract int getResponse(byte[] data, int length);

    //primer missatge que se li envia al server
    public abstract byte[] getRequest();

    //Si se li diu adeu al server el client es desconnecta
    public abstract boolean mustContinue(byte [] data);

}
