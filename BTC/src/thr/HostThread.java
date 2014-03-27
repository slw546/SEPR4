package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import btc.Main;
import cls.Aircraft;
import scn.MultiplayerGame;
import scn.MultiplayerSetUp;

public class HostThread extends NetworkThread {
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
	
	//Constructor
	public HostThread(int portNumber, MultiplayerSetUp lobby, Main main) {
		this.portNumber = portNumber;
		this.lobby = lobby;
		this.main = main;
	}
	
	@Override
	public void run(){
		try {
			setUp();
		} catch (IOException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_IO_UNAVAILABLE);
			System.err.println("Error setting up connection or IO with client");
			e.printStackTrace();
		}
		
		//Sending a test object
		System.out.println("Test object send 1");
		testCommunication();
		System.out.println("test object send finished");
		
		while (hosting){
			//If lobby has started the game, send game to client and leave this loop.
			
			//Something to do, prevents loop from closing..
			System.out.println("inside hosting loop");
			
			//See if the lobby has clicked start game
			if (playing){
				System.out.println("inside if playing");
				startGame();
				//exit loop and move to the next loop.
				break;
			}
		}
		while(hosting && playing){
			// listen to synchronisation from client
			// send changes to client e.g. aircraft changing airspace
			// update the local airspace
			System.out.println("Inside host+play loop");
		}
		// alert host that thread is exiting
		System.out.println("HostThread exiting");
	}
	
	/**
	 * Sets up the ServerSocket, accepts a client connection,
	 * and sets up the IO streams for that connection.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	public void setUp() throws IOException {
		System.out.println("Set up 1");
		//Init the ServerSocket and await a connection
		ServerSocket socket = new ServerSocket(portNumber);
		//accept an incoming connection request.
		//Blocking: Will wait here until a client connects.
		//May be given a timeout via clientSocket.setSOTimeout(time) before trying to accept
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
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_ESTABLISHED);
	}
	
	/**
	 * Tell the client to start the game, after the host game has begun.
	 * @throws IOException
	 */
	public void startGame(){
		//Alert client that game has started.
		String start = "start";
		System.out.println(start);
		//send a string which tells client to start the game
		sendObject(start);
		lobby.setStartOrdered(true);
	}
	
	/**
	 * Test communications by sending some objects to the client
	 */
	@Override
	public void testCommunication(){
		//generate an aircraft for comm test
		Aircraft test = new Aircraft();
		System.out.println("Generated an aircraft to send");

		System.out.println("test object send 2: network io");
		//Send the aircraft to the client
		//objOutputWriter.writeObject(test);
		sendObject(test);
		//Send some text to the client
		//cheaty way
		String test2 = "Hello Client";
		sendObject(test2);
		System.out.println("Both IO finished");

	}
	
	
	//Getters and Setters
	public void setGameScene(MultiplayerGame game){
		this.game = game;
	}
	
	public void setPlaying(boolean playing){
		this.playing = playing;
		System.out.println("set playing");
	}
	
}
