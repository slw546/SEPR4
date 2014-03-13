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
		System.out.println("Constructed a client");
	}
	
	@Override
	public void run(){
		System.out.println("Run started");
		try {
			setUp2();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Set up finished");
		listening = true;
		while (listening) {
			String str = "failed";
			try {
				sleep(500);
				String recv = null;
				System.out.println("checking textInputStream");
				while ((recv = textInputStream.readLine()) != null){
					recv = recv + textInputStream.readLine();
					System.out.println("got: " + recv);
				}
				System.out.println("end textInputStream check");
				str = textInputStream.readLine();
				System.out.println("Client read: " + str);
			} catch (IOException e) {
				System.err.println("Failed to read textInputStream");
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//listening = false;
		}
		while (listening && playing) {
			// listen to synchronisation from host
			// send required synchronisations back, e.g. aircraft enters opposing player's airspace.
			// update local airspace
		}
		// alert client that thread is exiting
		System.out.println("ClientThread exiting");
	}
	
	private void setUp2() throws UnknownHostException, IOException{
		System.out.println("Set up 1");
		socket = new Socket(hostAddress, portNumber);
		System.out.println("Set up 2");
		textOutputWriter = new PrintWriter(socket.getOutputStream());
		textOutputWriter.flush();
		System.out.println("Set up 3");
		objOutputWriter = new ObjectOutputStream(socket.getOutputStream());
		objOutputWriter.flush();
		System.out.println("Set up 4");
		textInputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		objInputStream = new ObjectInputStream(socket.getInputStream());
		System.out.println("Set up 5");
		
	}
	
	private void setUp(){
		// Try to create the socket by latching to the Host ServerSocket.
		// If socket created, try to create IO streams
		System.out.println("Setting up");
		try (
				Socket socket = new Socket(hostAddress, portNumber);
				){
			this.socket = socket;
			System.out.println("Setting up2");
			getIO(socket);
			listening = true;
			System.out.println("Setting up3");
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
		System.out.println("Getting IO");
		try {
			// set up output streams
			System.out.println("Getting IO2");
			textOutputWriter = new PrintWriter(socket.getOutputStream());
			textOutputWriter.flush();
			objOutputWriter = new ObjectOutputStream(socket.getOutputStream());
			objOutputWriter.flush();
			System.out.println("Getting IO3");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not set up output streams");
		}
		
		try {
			// set up input streams
			System.out.println("Getting IO4");
			textInputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
			objInputStream = new ObjectInputStream(socket.getInputStream());
			System.out.println("Getting IO5");
		} catch (IOException e){
			e.printStackTrace();
			System.err.println("Could not set up input streams");
		}
	}
}
