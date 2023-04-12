package com.example.hundirlaflota.net;

import com.example.hundirlaflota.model.Llista;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SrvTcpLlista {
/* Servidor TCP que genera un número perquè ClientTcpAdivina.java jugui a encertar-lo 
 * i on la comunicació dels diferents jugador passa per el Thread : ThreadServidorAdivina.java
 * */
	
	int port;
	Llista llista;
	public SrvTcpLlista(int port ) {
		this.port = port;
	}
	
	public void listen() {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		
		try {
			serverSocket = new ServerSocket(port);
			while(true) { //esperar connexió del client i llançar thread
				clientSocket = serverSocket.accept();
				//Llançar Thread per establir la comunicació
				ThreadSevidorLlista FilServidor = new ThreadSevidorLlista(clientSocket);
				Thread client = new Thread(FilServidor);
				client.start();
			}
		} catch (IOException ex) {
			Logger.getLogger(SrvTcpLlista.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) {
		/*if (args.length != 1) {
            System.err.println("Usage: java SrvTcpAdivina <port number>");
            System.exit(1);
        }*/
 
        
        //int port = Integer.parseInt(args[0]);
        SrvTcpLlista srv = new SrvTcpLlista(5558);
        srv.listen();

	}

}
