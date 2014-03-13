package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {
	
	private String hostAddress;
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
	
	public Client(Socket socket) throws IOException{
		super();
		this.hostAddress = hostAddress;
		this.portNumber = portNumber;
		textInputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		objInputStream = new ObjectInputStream(socket.getInputStream());
		textOutputWriter = new PrintWriter(socket.getOutputStream());
		objOutputWriter = new ObjectOutputStream(socket.getOutputStream());
	}

	@Override
	public void run(){
		String str = null;
		try {
			str = textInputStream.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Client: " + str);
		
	}
	
	

}
