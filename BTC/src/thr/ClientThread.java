package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
	/**
	 * Thread which latches to a ServerSocket and communicates with an instance of a HostThread
	 */
	
	// The port number which the host is hosting the game on
	private int portNumber;
	// the IP address of the host.
	private String hostAddress;
	
	// The socket through which communication will take place
	private Socket socket;
	
	// Flag to hold whether we are listening to a host or not.
	private boolean listening;
	// Flag to hold whether the game is in a play state or not.
	private boolean playing;
	
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
	
	
	public ClientThread(String hostAddress, int portNumber) {
		super();
		this.portNumber = portNumber;
		this.hostAddress = hostAddress;
	}
	
	@Override
	public void run(){
		try {
			setUp2();
			listening = true;
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (listening) {
			String str = "failed";
			try {
				str = textInputStream.readLine();
				System.out.println("Client read: " + str);
			} catch (IOException e) {
				System.err.println("Failed to read textInputStream");
				e.printStackTrace();
			}
			
			//listening = false;
		}
		while (listening && playing) {
			// listen to synchronisation from host
			// send required synchronisations back, e.g. aircraft enters opposing player's airspace.
			// update local airspace
		}
		// alter client that thread is exiting
		System.out.println("ClientThread exiting");
	}
	
	private void setUp2() throws UnknownHostException, IOException{
		socket = new Socket(hostAddress, portNumber);
		textInputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		objInputStream = new ObjectInputStream(socket.getInputStream());
		textOutputWriter = new PrintWriter(socket.getOutputStream());
		objOutputWriter = new ObjectOutputStream(socket.getOutputStream());
	}
	
	private void setUp(){
		// Try to create the socket by latching to the Host ServerSocket.
		// If socket created, try to create IO streams
		try (
				Socket socket = new Socket(hostAddress, portNumber);
				BufferedReader txtInStream =  new BufferedReader( new InputStreamReader(socket.getInputStream()));
				ObjectInputStream objInStream = new ObjectInputStream(socket.getInputStream());
				){
			this.socket = socket;
			this.textInputStream = txtInStream;
			this.objInputStream = objInStream;
			getIO(socket);

			/*try {
				// set up input streams
				textInputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
				objInputStream = new ObjectInputStream(socket.getInputStream());	
			} catch (IOException e){
				e.printStackTrace();
				System.err.println("Could not set up input streams");
			}

			try {
				// set up output streams
				textOutputWriter = new PrintWriter(socket.getOutputStream());
				objOutputWriter = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not set up output streams");
			}*/

			listening = true;

		} catch (UnknownHostException e){
			e.printStackTrace();
			System.err.println("Unknown Host. " + hostAddress  + " on port" + portNumber + ". Ensure the correct host address was used.");
			System.err.println("Ensure that the host has hosted the game");
		} catch (IOException e1) { //caused by automatic close invocation on socket
			System.err.println("Connection was refused - check host is hosting the game");
			e1.printStackTrace();
		}
	}
	
	public void getIO(Socket socket){
		//Set up IO
		//try {
			// set up input streams
			//textInputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
			//objInputStream = new ObjectInputStream(socket.getInputStream());	
		//} catch (IOException e){
		//	e.printStackTrace();
		//	System.err.println("Could not set up input streams");
		//}
		try {
			// set up output streams
			textOutputWriter = new PrintWriter(socket.getOutputStream());
			objOutputWriter = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not set up output streams");
		}
	}
}
