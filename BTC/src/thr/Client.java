package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {
	
	private String hostAddress;
	private int portNumber;
	
	private static PrintWriter out;
	private static BufferedReader in;
	
	public Client(String hostAddress, int portNumber){
		super();
		this.hostAddress = hostAddress;
		this.portNumber = portNumber;
	}
	
	@Override
	public void start(){
		try (
				Socket clientSocket = new Socket(hostAddress, portNumber);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				){
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		String str = null;
		try {
			str = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Client: " + str);
		
	}
	
	

}
