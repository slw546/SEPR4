package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Host extends Thread {
	
	private int portNumber;
	
	// INPUT
	// A buffered input stream to read text incoming from the client socket
	private BufferedReader textInputStream;
	// An input stream to read objects incoming from the client socket.
	private ObjectInputStream objInputStream;

	//OUTPUT
	// A print writer to send text through the client socket.
	private PrintWriter textOutputWriter;
	// An output stream to send objects through the client socket.
	private ObjectOutputStream objOutputWriter; 
	
	public Host(Socket clientSocket) throws IOException{
		super();
		this.portNumber = portNumber;
		textInputStream = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
		objInputStream = new ObjectInputStream(clientSocket.getInputStream());
		textOutputWriter = new PrintWriter(clientSocket.getOutputStream());
		objOutputWriter = new ObjectOutputStream(clientSocket.getOutputStream());
	}

	@Override
	public void run(){
		System.out.println("Hello, Host Thread");
		textOutputWriter.println("Ping");
		System.out.println("Bye");
	}

}
