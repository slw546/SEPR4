package thr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import scn.MultiplayerGame;
import scn.MultiplayerSetUp;
import btc.Main;
import cls.Aircraft;
import cls.AircraftBuffer;

public abstract class NetworkThread extends Thread {
	
	//SCENES
	/**
	 * The scenes associated with this networkThread instance
	 * used as handles to call functions in the scene.
	 */
	protected MultiplayerSetUp lobby;
	protected MultiplayerGame game;
	protected Main main;
	
	//Buffer of aircraft to be sent
	/**
	 * The buffer of aircraft with changes requiring synchronisation to the other player
	 */
	protected AircraftBuffer aircraftBuffer;
	
	// an ACK used to syncronise the thread execution with the other player's networkThread
	protected String ack = "ACK";
	
	/** 
	 * An input stream to read objects incoming from the client socket.
	 * Objects in this stream are buffered in order until read
	 * Objects read from this stream must be type cast with a cast matching their object
	 * e.g. new Aircraft a = (Aircraft) objInputStream.readObject();
	 */
	protected ObjectInputStream objInputStream;

	//OUTPUT
	/**
	 * An output stream to send objects through the client socket.
	 * Objects are buffered by the reciever until read.
	 */
	protected ObjectOutputStream objOutputWriter; 
	
	//Constructor
	public NetworkThread(){}
	
	//Set up
	abstract public void setUp() throws UnknownHostException, IOException;
	//Test
	abstract public void testCommunication();
	
	/**
	 * Add an aircraft to the thread's aircraft buffer for sync.
	 * @param aircraft the aircraft to be added
	 */
	public void addToBuffer(Aircraft aircraft){
		System.out.println(aircraft.name() + " added to buffer.");
		//if buffer empty, add new aircraft and return
		if (aircraftBuffer.size() == 0){
			aircraftBuffer.add(aircraft);
			return;
		}
		//otherwise check for a matching aircraft and update it so we don't send duplicates.
		
		//check for equality using aircraft's unique names
		//cant use ArrayList.contains(object o) since new flights will not have the same hashcode
		for (int i = 0; i < aircraftBuffer.size(); i++){
			if (aircraftBuffer.get(i).name() == aircraft.name()){
				//found a match
				//replace the matching aircraft with the new changes
				aircraftBuffer.remove(i);
				aircraftBuffer.add(aircraft);
				//done
				return;
			}
		}
		//no match, so add the new aircraft.
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
	
	/**
	 * End the game cleanly after a game over
	 */
	abstract public void endGame();
	
	//SEND
	
	/*
	 * SENDS VIA OBJECT OUTPUT STREAM ARE NOT BLOCKING
	 * SENT OBJECTS ARE BUFFERED IN ORDER SENT UNTIL READ
	 */
	
	/**
	 * Send an object on the objOutputStream
	 * Non-Blocking IO
	 * Buffered - sent objects are preserved until read.
	 * Objects MUST be read by the client in the order they are sent
	 * @param object the object to be sent
	 */
	protected void sendObject(Object object){
		try {
			objOutputWriter.writeUnshared(object);
		} catch (IOException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_SEND);
			//report error in console
			System.err.println("IO error when sending object");
			e.printStackTrace();
			//kill the thread which caused the error.
			killThread();
		}
	}
	
	/**
	 * Synchronise the aircraft buffer to a receiver
	 * Receive command: recieveAircraftBuffer()
	 * Sends first the number of aircraft to expect
	 * Followed by the aircraft objects
	 * Waits for ACKs.
	 * See also: sendObject for sends
	 * 			 recieveString for ACK
	 */
	protected void sendAircraftBuffer() {
		//Sync threads
		//send order
		String order = "aircraft";
		sendObject(order);
		//get ack
		String recv = recieveString();
		if (!recv.equals(ack)){
			return;
		}
		//get buffSize early in case a flight is added during the send
		int buffSize = aircraftBuffer.size();
		if (recv.equals(ack)){
			//tell receiver how many aircraft to expect
			sendObject(buffSize);
			//send aircraft
			for (int i = 0; i < buffSize; i++){
				Aircraft temp = aircraftBuffer.get(i);
				aircraftBuffer.remove(i);
				sendObject(temp);
				System.out.print("(Sending) ");
				temp.printPath();
			}
		}
	}
	
	/**
	 * score sync isn't as involved as the aircraft sync
	 * It's just sending an int and listening for one in reply.
	 * However the Host and Client must do this in opposite orders.
	 * Therefore this method is left for definition in the Host and Client thread
	 * rather than providing two simple but opposite definitions here.
	 */
	abstract protected void syncScore();
	
	//RECIEVE

	/*
	 * ALL RECIEVES ON THE OBJECT INPUT STREAM ARE BLOCKING
	 * OBJECT MUST BE READ IN THE ORDER THEY ARE SENT TO AVOID CLASS CAST EXCEPTIONS
	 */
	
	/**
	 * Receive the aircraft buffer from a syncAircraftBuffer() function
	 * ACKs the order
	 * listens for the number of aircraft to expect
	 * followed by that many aircraft
	 * see also: syncAircraftBuffer
	 */
	protected void recieveAircraftBuffer(){
		//System.out.println("Start recieve");
		//get order
		String order = recieveString();
		
		if (!order.equals("aircraft")){
			return;
		}
		
		//ack the order
		sendObject(ack);
		//get how many aircraft to expect
		int expected = recieveInt();
		
		//if we recieve -1 sender has exited.
		if (expected == -1){
			//quit out
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			killThread();
		}
		
		//get the aircraft
		for (int i = 0; i < expected; i++) {
			Aircraft a = recieveAircraft();
			//check if the flight is already in the airspace
			int index = game.existsInAirspace(a);
			//index of -2 indicates no matching flight
			//-1 is used to error out when a player presses escape
			//hence -2 is used to avoid confusion / errors
			if (index == -2){
				game.addFlight(a);
				System.out.println(a.name() + " added.");
				continue;
			} else {
				//aircraft already in airspace
				//remove from airspace
				System.out.println("======");
				System.out.println(a.name() + " recieved.");
				System.out.print("(In airspace) ");
				game.aircraftInAirspace().get(index).printPath();
				System.out.print("(recieved) ");
				a.printPath();
				System.out.println("======");
				game.aircraftInAirspace().remove(index);
			}
			//add new flight
			game.aircraftInAirspace().add(a);
		}
	}
	
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
			return recieved;
		} catch (ClassNotFoundException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			//report error in console
			System.err.println("Recieved object does not found: Aircraft");
			e.printStackTrace();
			//kill the thread which caused the error.
			killThread();
		} catch (SocketTimeoutException e){
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_TIMEOUT);
			//report error in console
			System.err.println("Timed out communicating with host");
			//kill the thread which caused the error.
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			//report error in console
			System.err.println("IOException communicating with host");
			//kill the thread which caused the error.
			e.printStackTrace();
			killThread();
		} catch (ClassCastException e){
			//May be called due to a player quitting
			//On quit, -1 is sent to other player intentionally to cause an error
			//If the other thread is waiting for an integer, it can quit gracefully
			//Otherwise a ClassCastException will cause the other player to quit out.
			
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			//report error in console
			System.err.println("Recieved class does not match Aircraft");
			//kill the thread which caused the error.
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
			return recieved;
		}  catch (ClassNotFoundException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			//report error in console
			System.err.println("Could not find class String");
			e.printStackTrace();
			//kill the thread which caused the error.
			killThread();
		} catch (SocketTimeoutException e){
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_TIMEOUT);
			//report error in console
			System.err.println("Timed out communicating with host");
			//kill the thread which caused the error.
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			//report error in console
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			//kill the thread which caused the error.
			killThread();
		} catch (ClassCastException e){
			//May be called due to a player quitting
			//On quit, -1 is sent to other player intentionally to cause an error
			//If the other thread is waiting for an integer, it can quit gracefully
			//Otherwise a ClassCastException will cause the other player to quit out.
			
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			//report error in console
			System.err.println("Recieved object does not match expected: String");
			//kill the thread which caused the error.
			killThread();
			e.printStackTrace();
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
			return recieved;
		}  catch (ClassNotFoundException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			//report error in console
			System.err.println("Could not find class Int.");
			e.printStackTrace();
			//kill the thread which caused the error.
			killThread();
		} catch (SocketTimeoutException e){
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_TIMEOUT);
			//report error in console
			System.err.println("Timed out communicating with host");
			//kill the thread which caused the error.
			e.printStackTrace();
			killThread();
		} catch (IOException e) {
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			//report error in console
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			//kill the thread which caused the error.
			killThread();
		} catch (ClassCastException e){
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			//report error in console
			System.err.println("Recieved object does not match expected: Int");
			//kill the thread which caused the error.
			e.printStackTrace();
			killThread();
		}
		return 0;
	}
	
	/**
	 * @return the size of the aircraftBuffer i.e. the number of aircraft awaiting sync.
	 */
	public int getBufferSize(){
		return this.aircraftBuffer.size();
	}
}
