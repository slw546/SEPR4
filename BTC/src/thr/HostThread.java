package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import scn.MultiplayerSetUp;

public class HostThread extends Thread {
	/**
	 * Thread which hosts the multiplayer service on a TCP socket.
	 * Communication with client is not delegated to separate threads
	 * As a result only one client may be connected at a time.
	 */
	
	// The socket to which clients will attach.
	// Clients may latch to a ServerSocket to recieve the service
	// In this case, the service will be the hosting work for the multiplayer mode.
	// A server socket only needs a port. Clients may access the socket given the IP of the server AND the port used.
	private ServerSocket socket = null;
	
	// A socket through which communications can take place
	// communication is bidirectional.
	// available when a client attempting to connect to the ServerSocket is accepted.
	private Socket clientSocket = null;
	
	// The port to open the socket on.
	private int portNumber = 4444;
	// Flag to raise when we are hosting the service.
	private boolean hosting = false;
	//flag to raise when we are playing the game.
	private boolean playing = false;

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
	
	public HostThread(int portNumber) {
		
		this.portNumber = portNumber;

	}
	
	public void run(){
		setUp();
		while (hosting){
			
			textOutputWriter.write("PING!");
			try {
				sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while(hosting && playing){
			// listen to synchronisation from client
			// send changes to client e.g. aircraft changing airspace
			// update the local airspace
		}
		// alert host that thread is exiting
		System.out.println("HostThread exiting");
	}
	
	private void setUp(){
		// Try to initialise the ServerSocket
		// If socket initialised, wait for a client to arrive at it.
		try (ServerSocket socket = new ServerSocket(portNumber)) {
			this.socket = socket;
			System.out.println("Host waiting on connection to socket, port number: " + portNumber);
			System.out.println("Host waiting on connection to socket, address: null");	
		} catch (IOException e){
			e.printStackTrace();
			System.err.println("Could not host on port " + portNumber);
			System.err.println("Ensure no other service is using this port");
		}

		// Wait for, and try to accept, communication through the socket
		// If client accepted, try to set up IO streams.
		try (Socket clientSocket = socket.accept()) {
			this.clientSocket = clientSocket;
			System.out.println("Accepted communication through socket");

			/*try {
				// set up input streams
				textInputStream = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
				objInputStream = new ObjectInputStream(clientSocket.getInputStream());	
			} catch (IOException e){
				e.printStackTrace();
				System.err.println("Could not set up input streams");
			}
			try {
				// set up output streams
				textOutputWriter = new PrintWriter(clientSocket.getOutputStream());
				objOutputWriter = new ObjectOutputStream(clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not set up output streams");
			}
*/
			hosting = true;
		} catch (IOException e){
			System.err.println("Error accepting connection on ServerSocket");
			e.printStackTrace();
		}
	}
}
