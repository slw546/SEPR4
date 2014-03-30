package thr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
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
	public boolean locked = false;
	
	protected String ack = "ACK";
	
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
		//System.out.println("Sending object");
		try {
			objOutputWriter.writeObject(object);
		} catch (IOException e){
			//set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_SEND);
			//report error in console
			System.out.println("IO error when sending object");
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
	protected void syncAircraftBuffer(){
		//Sync threads
		//send order
		String order = "aircraft";
		sendObject(order);
		//get ack
		String recv = recieveString();
		if (!recv.equals(ack)){
			System.err.println("Expected: ACK, got: " + recv);
			return;
		}
		//get buffSize early in case a flight is added during the send
		int buffSize = aircraftBuffer.size();
		//synced
		
		if (recv.equals(ack)){
			//tell reciever how many aircraft to expect
			sendObject(buffSize);
			//send aircraft
			for (int i = 0; i < buffSize; i++){
				sendObject(aircraftBuffer.get(i));
				//remove sent aircraft from buffer
				aircraftBuffer.remove(i);
			}
		}
		if (buffSize != 0){
			System.out.format("Sent %d flights \n", buffSize);
		}
		//System.out.println("End Sync");
	}
	
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
			System.out.println("Expecting order: aircraft, got: " + order);
			return;
		}
		
		//ack the order
		sendObject(ack);
		//get how many aircraft to expect
		int expected = recieveInt();
		
		if (expected > 0){
			System.out.format("Expecting %d flights \n", expected);
		}
		
		
		//if we recieve -1 sender has exited.
		if (expected == -1){
			//quit out
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			killThread();
		}
		
		//get the aircraft
		for (int i = 0; i < expected; i++){
			Aircraft a = recieveAircraft();
			//check if the flight is already in the airspace
			int index = game.existsInAirspace(a);
			System.out.format("Flight index: %d \n", index);
			
			//index of -2 indicates no matching flight
			//-1 is used to error out when a player presses escape
			//hence -2 is used to avoid confusion / errors
			if (index == -2){
				game.addFlight(a);
				//skip to next loop iteration
				continue;
			} else {
				//aircraft already in airspace
				System.out.println("Before:");
				System.out.println(game.aircraftInAirspace().toString());
				//remove from airspace
				game.aircraftInAirspace().remove(index);
			}
			//add new flight
			System.out.println("New flight's altitudestate: " + a.altitudeState());
			game.aircraftInAirspace().add(a);
			System.out.println("After");
			System.out.println(game.aircraftInAirspace().toString());
			
			/*if (index != -2){
				System.out.println("Before");
				System.out.println(game.aircraftInAirspace().toString());
				//if it is, remove it
				//game.aircraftInAirspace().remove(index);
				//replace it with the updated flight
				game.aircraftInAirspace().add(a);
				System.out.println("After");
				System.out.println(game.aircraftInAirspace().toString());
			} else {
				//otherwise add it as a new flight
				game.addFlight(a);	
			}*/
		}
		//System.out.println("end recieve");
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
			//System.out.println("recieved Aircraft\n" + recieved);
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
			//System.out.println("Recieved String\n"+recieved);
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
			//System.out.println("Recieved Int\n"+ recieved);
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
	
	public int getBufferSize(){
		return this.aircraftBuffer.size();
	}
}
