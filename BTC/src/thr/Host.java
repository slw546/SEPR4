package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Host extends Thread {
	
	private int portNumber;
	
	private static PrintWriter out;
	private static BufferedReader in;
	
	public Host(int portNumber){
		super();
		this.portNumber = portNumber;
	}
	
	@Override
	public void start(){
		try (
				ServerSocket listener = new ServerSocket(portNumber);
				Socket clientSocket = listener.accept();
				PrintWriter out =
						new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
			){
			this.out = out;
			this.in= in;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		run();
		
	}

	@Override
	public void run(){
		System.out.println("Hello, Host Thread");
		out.println("Ping");
		System.out.println("Bye");
	}
	
	
}
