package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import scn.MultiplayerGame;
import scn.MultiplayerSetUp;

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
	
	//SCENES
	private MultiplayerSetUp lobby;
	private MultiplayerGame game;
	
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
	
	
	public ClientThread(String hostAddress, int portNumber, MultiplayerSetUp lobby) {
		super();
		this.portNumber = portNumber;
		this.hostAddress = hostAddress;
		this.lobby = lobby;
		System.out.println("Constructed a client");
	}
	
	@Override
	public void run(){
		System.out.println("Run started");
		try {
			setUp();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Set up finished");
		listening = true;
		lobby.setConnection_established(true);
		while (listening) {
			
			String str = "failed";
			try {
				sleep(500);
				String recv = null;
				System.out.println("checking textInputStream");
				str = textInputStream.readLine();
				System.out.println("Client read: " + str);
				System.out.println("end textInputStream check");
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
	
	private void setUp() throws UnknownHostException, IOException{
		System.out.println("Set up 1");
		Socket socket = new Socket(hostAddress, portNumber);
		this.socket = socket;
		System.out.println("Set up 2");
		textOutputWriter = new PrintWriter(this.socket.getOutputStream());
		textOutputWriter.flush();
		System.out.println("Set up 3");
		objOutputWriter = new ObjectOutputStream(this.socket.getOutputStream());
		objOutputWriter.flush();
		System.out.println("Set up 4");
		textInputStream = new BufferedReader( new InputStreamReader(this.socket.getInputStream()));
		objInputStream = new ObjectInputStream(this.socket.getInputStream());
		System.out.println("Set up 5");
	}
	
	//Getters and Setters
	public void setGameScene(MultiplayerGame game){
		this.game = game;
	}

}
