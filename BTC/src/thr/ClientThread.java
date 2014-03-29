package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import lib.jog.input;
import btc.Main;
import cls.Aircraft;
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
		aircraftBuffer = new ArrayList<Aircraft>();
		System.out.println("Constructed a client");
	}
	
	@Override
	public void run(){
		System.out.println("Run started");
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
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_IO_UNAVAILABLE);
			System.err.println("Error setting up IO on socket");
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
		
		while (listening && playing) {
			//get the order from host
			String order = recieveString();
			//act upon the order
			switch (order){
			
			case "aircraft":
				//sync from host
				recieveAircraftBuffer();
				//sync to host
				syncAircraftBuffer();
				break;
				
			case "score":
				syncScore();
				break;
			}
		}
		
		// alert client that thread is exiting
		System.out.println("ClientThread exiting");
	}
	
	@Override
	protected void syncScore(){
		//send the ack
		sendObject(ack);
		//get their score
		int oppScore = recieveInt();
		//send our score
		sendObject(game.getTotalScore());
		
		if (oppScore == -1){
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
	public void setUp() throws UnknownHostException, IOException{
		//Set up connection to the host
		Socket socket = new Socket(hostAddress, portNumber);
		this.socket = socket;
		
		//Set up Output streams
		textOutputWriter = new PrintWriter(this.socket.getOutputStream());
		textOutputWriter.flush();
		objOutputWriter = new ObjectOutputStream(this.socket.getOutputStream());
		objOutputWriter.flush();
		
		//Set up input streams
		textInputStream = new BufferedReader( new InputStreamReader(this.socket.getInputStream()));
		objInputStream = new ObjectInputStream(this.socket.getInputStream());
		
		System.out.println("Set up finished");
		listening = true;
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_ESTABLISHED);
		
		//set to time out after 5 seconds.
		//if an attempt to read does not get data within 5 seconds,
		//a socket error will be thrown.
		socket.setSoTimeout(5000);
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
			System.out.println("Trying to read text from host");
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
	public void killThread(){
		System.out.println("Thread killed due to error");
		//if the game is running, escape to lobby
		if (playing){
			lobby.setStartOrdered(false);
			main.keyReleased(input.KEY_ESCAPE);
		}
		//end while loops to exit the thread
		this.playing = false;
		this.listening = false;
	}
	
	@Override
	public void escapeThread(){
		System.out.println("Exiting thread voluntarily");
		//prevent game scene from restarting on return to lobby
		lobby.setStartOrdered(false);
		//set error messages, connection state
		lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
		lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLOSED_BY_YOU);
		
		//set flags to exit while loops
		this.playing = false;
		this.listening = false;
		
		//send an object to either break the listening thread, or signal game closed.
		sendObject(-1);
	}
	
	//Getters and Setters
	public void setGameScene(MultiplayerGame game){
		this.game = game;
	}
}
