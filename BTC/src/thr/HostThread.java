package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import cls.Aircraft;

import scn.MultiplayerGame;
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
	private int portNumber = 4445;
	// Flag to raise when we are hosting the service.
	private boolean hosting = false;
	//flag to raise when we are playing the game.
	private boolean playing = false;
	
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
	
	//Constructor
	public HostThread(int portNumber, MultiplayerSetUp lobby) {
		this.portNumber = portNumber;
		this.lobby = lobby;
	}
	
	@Override
	public void run(){
		try {
			setUp();
		} catch (IOException e) {
			System.err.println("Error setting up connection or IO with client");
			e.printStackTrace();
		}
		while (hosting){
			String outLine = "ping!";
			//System.out.println(outLine);
			textOutputWriter.println(outLine);
			
			//If lobby has started the game, send game to client and leave this loop.
			if (playing){
				try {
					startGame();
				} catch (IOException e) {
					System.err.println("IO failed trying to start game");
					e.printStackTrace();
				}
				break;
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
	
	private void setUp() throws IOException {
		System.out.println("Set up 1");
		//Init the ServerSocket and await a connection
		ServerSocket socket = new ServerSocket(portNumber);
		//accept an incoming connection request.
		Socket clientSocket = socket.accept();
		this.socket = socket;
		this.clientSocket = clientSocket;
		System.out.println("Set up 2");
		//set up IO
		//Outputs
		textOutputWriter = new PrintWriter(clientSocket.getOutputStream());
		textOutputWriter.flush();
		System.out.println("Set up 3");
		objOutputWriter = new ObjectOutputStream(clientSocket.getOutputStream());
		objOutputWriter.flush();
		System.out.println("Set up 4");
		//Inputs
		textInputStream = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
		objInputStream = new ObjectInputStream(clientSocket.getInputStream());
		System.out.println("Set up 5");
		//Raise hosting flag
		hosting = true;
		lobby.setConnection_established(true);
	}
	
	public void startGame() throws IOException{
		objOutputWriter.writeObject(game);
	}
	
	//Getters and Setters
	public void setGameScene(MultiplayerGame game){
		this.game = game;
	}
	
	public void setPlaying(boolean playing){
		this.playing = playing;
	}
	
}
