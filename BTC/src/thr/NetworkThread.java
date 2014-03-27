package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;

import scn.MultiplayerGame;
import scn.MultiplayerSetUp;
import btc.Main;
import cls.Aircraft;

public abstract class NetworkThread extends Thread {
	
	//SCENES
	protected MultiplayerSetUp lobby;
	protected MultiplayerGame game;
	protected Main main;
	
	//Buffer of aircraft to be sent
	protected ArrayList<Aircraft> aircraftBuffer;
	
	// INPUT
	// A buffered input stream to read text incoming from the client socket
	protected BufferedReader textInputStream;
	// An input stream to read objects incoming from the client socket.
	protected ObjectInputStream objInputStream;

	//OUTPUT
	// A print writer to send text through the client socket.
	protected PrintWriter textOutputWriter;
	// An output stream to send objects through the client socket.
	protected ObjectOutputStream objOutputWriter; 
	
	//Constructor
	public NetworkThread(){
	}
	
	//Set up
	abstract public void setUp() throws UnknownHostException, IOException;
	//Test
	abstract public void testCommunication();
	
	public void addToBuffer(Aircraft aircraft){
		aircraftBuffer.add(aircraft);
	}
	
	//Error handling
	/**
	 * Involuntary thread death handler
	 */
	abstract public void killThread();
	
	/**
	 * Voluntary thread death handler
	 */
	abstract public void escapeThread();
	
	//SEND
	/**
	 * Send an object on the objOutputStream
	 * Non-Blocking IO
	 * Buffered - sent objects are preserved until read.
	 * Objects MUST be read by the client in the order they are sent
	 * @param object the object to be sent
	 */
	protected void sendObject(Object object){
		System.out.println("Sending object");
		try {
			objOutputWriter.writeObject(object);
		} catch (IOException e){
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_SEND);
			System.out.println("Comms test: Failed to object; IOException.");
			e.printStackTrace();
			killThread();
		}
	}
	
	//RECIEVE
	/**
	 * Read an aircraft in from the objectInputStream
	 * Order is important: objects must be read in the order they are sent.
	 * Be certain an aircraft is at the front of the stream before use.
	 * Blocking: If the stream is empty, will wait until something arrives.
	 * @return one aircraft from the stream.
	 */
	protected Aircraft recieveAircraft(){
		try {
			Aircraft recieved;
			//read an aircraft from the stream
			recieved = (Aircraft) objInputStream.readObject();
			System.out.println(recieved);
			return recieved;
		} catch (ClassNotFoundException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			System.err.println("Recieved object does not match expected: Aircraft");
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			killThread();
		} catch (ClassCastException e){
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			killThread();
		}
		return null;
	}
	
	/**
	 * Read a string in from the objectInputStream
	 * Order is important: objects must be read in the order they are sent.
	 * Be certain a string is at the front of the stream before use.
	 * Blocking: If the stream is empty, will wait until something arrives.
	 * @return one string from the stream.
	 */
	protected String recieveString(){
		try {
			String recieved;
			recieved = (String) objInputStream.readObject();
			System.out.println(recieved);
			return recieved;
		}  catch (ClassNotFoundException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			System.err.println("Recieved object does not match expected: String");
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			killThread();
		}
		return null;
	}
	
	/**
	 * Read an integer in from the objectInputStream
	 * Order is important: objects must be read in the order they are sent.
	 * Be certain an integer is at the front of the stream before use.
	 * Blocking: If the stream is empty, will wait until something arrives.
	 * @return one integer from the stream
	 */
	protected int recieveInt(){
		try {
			int recieved;
			recieved = (int) objInputStream.readObject();
			System.out.println(recieved);
			return recieved;
		}  catch (ClassNotFoundException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			System.err.println("Recieved object does not match expected: String");
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			killThread();
		}
		return 0;
	}

}
