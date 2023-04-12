package com.example.hundirlaflota.net;

import com.example.hundirlaflota.model.Llista;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientTcpLlista extends Thread {
/* CLient TCP que ha endevinar un número pensat per SrvTcpAdivina.java */
	
	String hostname;
	int port;
	boolean continueConnected;
	int intents;
	Llista llista;
	Llista serverData;

	public ClientTcpLlista(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
		continueConnected = true;
		intents=0;
	}

	public void run() {
		Llista request;
		Socket socket;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		try {
			socket = new Socket(InetAddress.getByName(hostname), port);
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());

			//el client atén el port fins que decideix finalitzar
			while(continueConnected){

				serverData = (Llista) ois.readObject();

				System.out.println("Esperando");
				//processament de les dades rebudes i obtenció d'una nova petició
				System.out.println(serverData);
				request = getRequest(serverData);

				oos.writeObject(request);
				oos.flush();

				llista = (Llista) ois.readObject();
			}
			close(socket);
		} catch (UnknownHostException ex) {
			System.out.println("Error de connexió. No existeix el host: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("Error de connexió indefinit: " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.out.println("No se ha encontrado la clase Llista: " + ex.getMessage());
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (ois != null) {
					ois.close();
				}
			} catch (IOException ex) {
				System.out.println("Error al cerrar los streams: " + ex.getMessage());
			}
		}
	}

	public Llista getRequest(Llista serverData) {
		if (serverData!=null) System.out.println(serverData.getNumberList());
		Scanner in = new Scanner(System.in);
		System.out.print("Nombre de quien envia la lista? ");
		String nom = in.nextLine();
		System.out.print("Pon los numeros separados por espacios: ");
		List<Integer> numeros = new LinkedList<>();
		String llistaNum = in.nextLine();

// Separa el String en un arreglo de String usando split()
		String[] numStr = llistaNum.split(" ");

// Recorre el arreglo de String y convierte cada elemento a Integer
		for (String str : numStr) {
			numeros.add(Integer.parseInt(str));
		}

// Imprime la lista de numeros
		System.out.println(numeros);

		return new Llista(nom, numeros);
	}

	
	public boolean mustFinish(String dades) {
		if (dades.equals("exit")) return false;
		return true;
		
	}
	
	private void close(Socket socket){
		//si falla el tancament no podem fer gaire cosa, només enregistrar
		//el problema
		try {
			//tancament de tots els recursos
			if(socket!=null && !socket.isClosed()){
				if(!socket.isInputShutdown()){
					socket.shutdownInput();
				}
				if(!socket.isOutputShutdown()){
					socket.shutdownOutput();
				}
				socket.close();
			}
		} catch (IOException ex) {
			//enregistrem l'error amb un objecte Logger
			Logger.getLogger(ClientTcpLlista.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void main(String[] args) {
		/*if (args.length != 2) {
            System.err.println(
                "Usage: java ClientTcpAdivina <host name> <port number>");
            System.exit(1);
        }*/
 
       // String hostName = args[0];
       // int portNumber = Integer.parseInt(args[1]);
        ClientTcpLlista clientTcp = new ClientTcpLlista("localhost",5558);
        clientTcp.start();
	}
}
