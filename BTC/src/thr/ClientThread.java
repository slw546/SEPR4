package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
			MultiplayerSetUp lobby, MultiplayerGame game, Main main) {
		super();
		this.portNumber = portNumber;
		this.hostAddress = hostAddress;
		this.lobby = lobby;
		this.main = main;
		this.game = game;
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
		} catch (IOException e1) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_IO_UNAVAILABLE);
			System.err.println("Error setting up IO on socket");
			e1.printStackTrace();
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
			// listen to synchronisation from host
			// send required synchronisations back, e.g. aircraft enters opposing player's airspace.
			// update local airspace
		}
		// alert client that thread is exiting
		System.out.println("ClientThread exiting");
	}
	
	/**
	 * Set up the connection and IO with the host.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Override
	public void setUp() throws UnknownHostException, IOException{
		//Set up connection to the host
		//System.out.println("Set up 1");
		Socket socket = new Socket(hostAddress, portNumber);
		this.socket = socket;
		
		//Set up Output streams
		//System.out.println("Set up 2");
		textOutputWriter = new PrintWriter(this.socket.getOutputStream());
		textOutputWriter.flush();
		//System.out.println("Set up 3");
		objOutputWriter = new ObjectOutputStream(this.socket.getOutputStream());
		objOutputWriter.flush();
		
		//Set up input streams
		//System.out.println("Set up 4");
		textInputStream = new BufferedReader( new InputStreamReader(this.socket.getInputStream()));
		objInputStream = new ObjectInputStream(this.socket.getInputStream());
		//System.out.println("Set up 5");
		
		System.out.println("Set up finished");
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
			//String fromHost;
			System.out.println("Trying to read text from host");
			//fromHost = textInputStream.readLine();
			String fromHost = (String) objInputStream.readObject();
			System.out.println("Read text from host");
			System.out.println(fromHost);
		} catch (ClassNotFoundException e) {
			System.err.println("Aircraft class not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException communicating with host");
			e.printStackTrace();
		}
	}
	
	//Getters and Setters

}
