package thr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import lib.jog.input;
import btc.Main;
import cls.Aircraft;
import cls.AircraftBuffer;
import scn.MultiplayerGame;
import scn.MultiplayerSetUp;

public class HostThread extends NetworkThread {
	/**
	 * Thread which hosts the multiplayer service on a TCP socket.
	 * Communication with client is not delegated to separate threads
	 * As a result only one client may be connected at a time.
	 */
	
	// The port to open the socket on.
		private int portNumber = 4445;
		
	// A socket through which communications can take place
	// communication is bidirectional.
	// available when a client attempting to connect to the ServerSocket is accepted.
	private Socket clientSocket = null;
		
	// The socket to which clients will attach.
	// Clients may latch to a ServerSocket to recieve the service
	// In this case, the service will be the hosting work for the multiplayer mode.
	// A server socket only needs a port. Clients may access the socket given the IP of the server AND the port used.
	private ServerSocket socket = null;

	// Flag to raise when we are hosting the service.
	private boolean hosting = false;
	//flag to raise when we are playing the game.
	private boolean playing = false;
	
	//Constructor
	public HostThread(int portNumber, MultiplayerSetUp lobby, Main main) {
		this.portNumber = portNumber;
		this.lobby = lobby;
		this.main = main;
		//init aircraftBuffer
		aircraftBuffer = new AircraftBuffer();
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
			killThread();
		}
		
		//Sending a test object
		testCommunication();
		
		while (hosting){
			//If lobby has started the game, send game to client and leave this loop.
			
			//Something to do, prevents loop from closing..
			System.out.print("");
			
			//See if the lobby has clicked start game
			if (playing){
				startGame();
				//exit loop and move to the next loop.
				break;
			}
		}
		System.out.println("----------------------------------");
		while(hosting && playing){
			recieveAircraftBuffer();
			sendAircraftBuffer();
			syncScore();
			//sleep for 1/10th of a second to reduce network load
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//close the sockets on the way out.
		//allows for a new connection to be started in the lobby without exiting the program
		try {
			clientSocket.close();
			socket.close();
		} catch (IOException e) {
			System.err.println("failed to close sockets");
			e.printStackTrace();
		}
	}
	
	@Override
	protected void syncScore(){
		int oppScore = Integer.MAX_VALUE;
		String order = "score";
		sendObject(order);
		//wait for ack
		String recv = recieveString();
		if (recv.equals(ack)){
			//send our score
			sendObject(game.getTotalScore());
			//get their score
			oppScore = recieveInt();
		}
		
		if (oppScore == Integer.MAX_VALUE){
			//opponent is quitting and their quit signal has been found here.
			//or we failed recieveInt()
			//therefore, quit the game.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			killThread();
		} else {
			//update opponent's score
			game.setOpponentScore(oppScore);
		}
	}
	
	/**
	 * Sets up the ServerSocket, accepts a client connection,
	 * and sets up the IO streams for that connection.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	public void setUp() throws IOException {
		//get the local address and tell the lobby it
		//this is the address of the host pc on the LAN
		InetAddress addr = InetAddress.getLocalHost();
		lobby.setAddress(addr.getHostAddress());
		//Init the ServerSocket and await a connection
		ServerSocket socket = new ServerSocket(portNumber);
		//accept an incoming connection request.
		//Blocking: Will wait here until a client connects.
		//May be given a timeout via clientSocket.setSOTimeout(time) before trying to accept
		Socket clientSocket = socket.accept();
		this.socket = socket;
		this.clientSocket = clientSocket;
		//set up IO
		//Outputs
		objOutputWriter = new ObjectOutputStream(clientSocket.getOutputStream());
		objOutputWriter.flush();
		//Inputs
		objInputStream = new ObjectInputStream(clientSocket.getInputStream());
		//Raise hosting flag
		hosting = true;
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_ESTABLISHED);
		
		//set to time out after 5 seconds.
		//if an attempt to read does not get data within 5 seconds,
		//a socket error will be thrown.
		clientSocket.setSoTimeout(5000);
	}
	
	/**
	 * Tell the client to start the game, after the host game has begun.
	 * @throws IOException
	 */
	public void startGame(){
		//Alert client that game has started.
		String start = "start";
		//send a string which tells client to start the game
		sendObject(start);
		lobby.setStartOrdered(true);
		game.setLastMoveTime(System.currentTimeMillis());
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
		sendObject(test);
		System.out.println("Sent " + test);
		//Send some text to the client
		String test2 = "Hello Client";
		sendObject(test2);
		System.out.println("Sent: " + test2);
		System.out.println("Both IO finished");
	}
	
	/**
	 * Method for INVOLUNTARY thread death
	 * e.g. due to connection loss
	 */
	@Override
	public void killThread(){
		System.out.println("Killing Thread");
		//if game is running, escape to lobby
		if (playing) {
			lobby.setStartOrdered(false);
			main.keyReleased(input.KEY_ESCAPE);
		}
		//close the sockets
		//allows for a new connection to be started in the lobby without exiting the program
		try {
			clientSocket.close();
			socket.close();
		} catch (IOException e) {
			System.err.println("failed to close sockets");
			e.printStackTrace();
		}
		//end while loops to exit thread
		this.playing = false;
		this.hosting = false;
	}
	
	/**
	 * Method for VOLUNTARY thread exit
	 * E.g. due to escape key being pressed.
	 */
	public void escapeThread(){
		//prevent game scene from restarting on return to lobby
		lobby.setStartOrdered(false);
		//set error messages, connection state
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
		lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLOSED_BY_YOU);
		
		//set flags to exit while loops
		this.playing = false;
		this.hosting = false;
		
		//send an object to either break the listening thread, or signal game closed.
		//We can afford to do this since the connection will still be up.
		//Unlike in killThread, where it may have been lost due to an error.
		sendObject(Integer.MAX_VALUE);

	}
	
	public void endGame(){
		//set flags to exit while loops
		this.playing = false;
		this.hosting = false;
	}
	
	
	//Getters and Setters
	public void setGameScene(MultiplayerGame game){
		this.game = game;
	}
	
	public void setPlaying(boolean playing){
		this.playing = playing;
	}
	
}
