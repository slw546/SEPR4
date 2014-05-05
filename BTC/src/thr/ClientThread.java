package thr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import lib.jog.input;
import btc.Main;
import cls.Aircraft;
import cls.AircraftBuffer;
import scn.MultiplayerGame;
import scn.MultiplayerSetUp;

public class ClientThread extends NetworkThread {
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
	
	public ClientThread(String hostAddress, int portNumber, 
			MultiplayerSetUp lobby, Main main) {
		this.portNumber = portNumber;
		this.hostAddress = hostAddress;
		this.lobby = lobby;
		this.main = main;
		//init aircraftBuffer
		aircraftBuffer = new AircraftBuffer();
	}
	
	@Override
	public void run() {
		try {
			setUp();
		} catch (UnknownHostException e1) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.UNKNOWN_HOST);
			System.err.println("Unknown Host: check host local IP address and game Port");
			e1.printStackTrace();
			killThread();
		} catch (IOException e1) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.NO_HOST_RUNNING);
			System.err.println("Error setting up IO on socket. Host not running");
			e1.printStackTrace();
			killThread();
		}
		
		if (listening){
			testCommunication();
		}
		
		while (listening){
			//carry out pre-game lobby operations.
			
			//wait for the order from the host to begin the game
			String order = recieveString();
			if (order.equals("start")){
				lobby.setStartOrdered(true);
				this.playing = true;
				break;
			}
		}
		
		//Entering play loop
		//set to time out after 5 seconds.
		//if an attempt to read does not get data within 5 seconds,
		//a socket error will be thrown.
		//not done earlier, as we don't care if it takes more than 5 seconds
		//for the host to start the game.
		try {
			socket.setSoTimeout(1000);
		} catch (SocketException e1) {
			System.err.println("Failed to set socket timeout");
			e1.printStackTrace();
		}
		System.out.println("----------------------------------");
		while (listening && playing) {
			sendAircraftBuffer();
			recieveAircraftBuffer();
			syncScore();
			//sleep for 1/10th of a second to reduce network load
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//close the socket on the way out.
		//allows for a new connection to be started in the lobby without exiting the program
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("failed to close socket");
			e.printStackTrace();
		}
	}
	
	@Override
	protected void syncScore(){
		String order = recieveString();
		if (!order.equals("score")){
			System.err.println("Expected order: score, got: " + order);
			return;
		}
		//send the ack
		sendObject(ack);
		//get their score
		int oppScore = recieveInt();
		//send our score
		sendObject(game.getTotalScore());
		if (oppScore == Integer.MAX_VALUE){
			//Other player is quitting, and their quit signal has been picked up here.
			//therefore, quit the game
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			killThread();
		} else {
			//update game scene with new opponent score.
			game.setOpponentScore(oppScore);
		}
	}
	
	/**
	 * Set up the connection and IO with the host.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Override
	public void setUp() throws UnknownHostException, IOException {
		lobby.setNetworkState(MultiplayerSetUp.networkStates.ATTEMPTING_CONNECTION);
		//Set up connection to the host
		Socket socket = new Socket(hostAddress, portNumber);
		this.socket = socket;
		//Set up Output streams
		objOutputWriter = new ObjectOutputStream(this.socket.getOutputStream());
		objOutputWriter.flush();
		//Set up input streams
		objInputStream = new ObjectInputStream(this.socket.getInputStream());
		listening = true;
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_ESTABLISHED);
	}
	
	/**
	 * Test communication with the host by recieving some objects.
	 */
	@Override
	public void testCommunication() {
		//Comms test
		System.out.println("Begin comms test");
		try {
			//attempt to read test aircraft from host
			Aircraft test = (Aircraft) objInputStream.readObject();
			System.out.println("Got an aircraft object: ");
			System.out.println(test);
			//attempt to read text from host
			String fromHost = (String) objInputStream.readObject();
			System.out.println("Read text from host: " + fromHost);
		} catch (ClassNotFoundException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			System.err.println("Aircraft class not found");
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			killThread();
		}
	}
	
	@Override
	/**
	 * Kill the thread and exit the game to the lobby.
	 * If called during an error catch, lobby will display cause of exit.
	 */
	public void killThread(){
		System.out.println("Killing thread");
		//if the game is running, escape to lobby
		if (playing){
			lobby.setStartOrdered(false);
			main.keyReleased(input.KEY_ESCAPE);
		}
		//close socket
		//allows for a new connection to be started in the lobby without exiting the program
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("failed to close sockets");
			e.printStackTrace();
		}
		//end while loops to exit the thread
		this.playing = false;
		this.listening = false;
	}
	
	@Override
	/**
	 * Exit the thread due to the escape key being pressed
	 */
	public void escapeThread(){
		System.out.println("Exiting thread voluntarily");
		//prevent game scene from restarting on return to lobby
		lobby.setStartOrdered(false);
		//set error messages, connection state
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
		lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLOSED_BY_YOU);
		//close socket
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("failed to close sockets");
			e.printStackTrace();
		}
		//set flags to exit while loops
		this.playing = false;
		this.listening = false;
		
		//send an object to either break the listening thread, or signal game closed.
		//We can afford to do this since the connection will still be up.
		//Unlike in killThread, where it may have been lost due to an error.
		sendObject(Integer.MAX_VALUE);
	}
	
	//Getters and Setters
	public void setGameScene(MultiplayerGame game){
		this.game = game;
	}
}
