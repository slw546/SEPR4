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
import cls.AircraftBuffer;
import cls.Waypoint;
import thr.Packet;

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
	
	protected ArrayList<Packet> packetBuffer;
	
	// an ACK used to syncronise the thread execution with the other player's networkThread
	protected String ack = "ACK";
	
	// INPUT
	/** 
	 * An input stream to read text incoming from the client socket
	 * Not used, since text is not buffered which means this cannot be used to synchronise the threads
	 */
	protected BufferedReader textInputStream;
	
	
	/** 
	 * An input stream to read objects incoming from the client socket.
	 * Objects in this stream are buffered in order until read
	 * Objects read from this stream must be type cast with a cast matching their object
	 * e.g. new Aircraft a = (Aircraft) objInputStream.readObject();
	 */
	protected ObjectInputStream objInputStream;

	//OUTPUT
	/**
	 * A print writer to send text through the client socket.
	 * Text is not buffered by reciever. Unused.
	 */
	protected PrintWriter textOutputWriter;

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
	
	public void addToPacketBuffer(Packet packet) {
		packetBuffer.add(packet);
	}
	
	/**
	 * Add an aircraft to the thread's aircraft buffer for sync.
	 * @param aircraft the aircraft to be added
	 */
	public void addToBuffer(Aircraft aircraft){
		//if buffer empty, add new aircraft and return
		if (aircraftBuffer.size() == 0){
			aircraftBuffer.add(aircraft);
			return;
		}
		
		//otherwise check for a matching aircraft
		//and update it so we don't send duplicates.
		
		//check for equality using aircraft's unique names
		//cant use ArrayList.contains(object o) since new flights will have different attributes
		//e.g. different x,y pos, altitude, etc.
		//Hence contains(o) will not consider them equal.
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
			
			objOutputWriter.writeObject(object);
		
		} catch (IOException e){
			
			// Set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_SEND);
			
			// Report error in console
			System.out.println("IO error when sending object");
			e.printStackTrace();
			
			// Kill the thread which caused the error.
			killThread();
			
		}
		
	}
	
	protected void syncPacketBuffer() {
		
		// Send a handshake
		Packet handShake = new Packet(Packet.PacketType.HANDSHAKE, new Object[0]);
		sendObject(handShake);
		//System.out.println("Sent Handshake");
		
		// Recieve the acknowledgement
		Packet recieve = recievePacket();
		if (recieve.getType() != Packet.PacketType.ACKNOWLEDGE) {
			System.err.println("Expected Acknowledgement");
			return;
		}
		
		//System.out.println("Recieved Acknowledgement");
		
		// Get buffer size early in case a packet is added during the send
		int buffSize = packetBuffer.size();
		
		// Tell receiver how many packets to expect
		Packet buffSizePacket = new Packet(Packet.PacketType.BUFFERSIZE, new Object[] { buffSize } );
		sendObject(buffSizePacket);
		
		//System.out.println("Sent Buffersize");
		
		// Send packets
		for (int i = 0; i < buffSize; i++){
			
			Packet temp = packetBuffer.get(i);
			
			// Remove packet from buffer
			packetBuffer.remove(i);
			
			sendObject(temp);
			System.out.println("Sent Packet, type: " + temp.getType().toString());
		}
		
	}
	
	protected void recievePacketBuffer(){
		
		// Get Packet
		Packet order = recievePacket();
		
		if (order.getType() != Packet.PacketType.HANDSHAKE){
			System.err.println("Expected handshake");
			return;
		}
		
		//System.out.println("Recieved Handshake");
		
		// Ackowledge the order
		Packet acknowledge = new Packet(Packet.PacketType.ACKNOWLEDGE, new Object[0]);
		sendObject(acknowledge);
		//System.out.println("Sent Acknowledgement");
		
		// Get how many packets to expect
		Packet expectedBuffSize = recievePacket();
		
		if (expectedBuffSize.getType() != Packet.PacketType.BUFFERSIZE) {
			System.err.println("Expected Buffersize, recieved: " + expectedBuffSize.toString());
			return;
		}
		
		//System.out.println("Recieved Buffersize");
		
		int expected = (int) expectedBuffSize.getData()[0];
		
		if (expected > 0){
			System.out.format("Expecting %d packets\n", expected);
		}
		
		
		// If we recieve -1 sender has exited.
		if (expected == -1){
			//quit out
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			killThread();
		}
		
		// Get the packets
		for (int i = 0; i < expected; i++){
			
			Packet p = recievePacket();
			System.out.println("Recieved Packet, type: " + p.getType().toString());

			switch(p.getType()) {
			case DISCONNECT:
				escapeThread();
				break;
			case NEWFLIGHT:
				Aircraft newPlane = (Aircraft) p.getData()[0];
				
				// Make sure we don't already have this aircraft
				if (game.existsInAirspace(newPlane) != -2)
				{
					System.err.println("Recieved New Flight with aircraft that already exists");
				} else {
					game.addFlight(newPlane);
				}
				break;
			case UPDATEWAYPOINT:
				Aircraft aircraftRecievedWaypoint = (Aircraft) p.getData()[0];
				int routeIndex = (int) p.getData()[1];
				Waypoint waypoint = (Waypoint) p.getData()[2];
				
				int updateAircraftIndex = game.existsInAirspace(aircraftRecievedWaypoint);
				
				if (updateAircraftIndex == -2) {
					System.err.println("Recieved Update Waypoint with aircraft that does not exist");
				} else {
					Aircraft aircraftToUpdate = game.aircraftInAirspace().get(updateAircraftIndex);
					System.out.println("Update Waypoint data: Aircraft: " + aircraftToUpdate.name() + " RouteIndex: " + routeIndex + " New Waypoint Location: " + waypoint.toString());
					game.changeFlightPath(aircraftToUpdate, routeIndex, waypoint);
				}
				break;
			case MANUALCONTROL:
				Aircraft aircraftRecievedManual = (Aircraft) p.getData()[0];
				Packet.ManualType manualType = (Packet.ManualType) p.getData()[1];
				
				int manualAircraftIndex = game.existsInAirspace(aircraftRecievedManual);
				
				if (manualAircraftIndex == -2) {
					System.err.println("Recieved Manual Control with aircraft that does not exist");
				} else {
					Aircraft aircraftManuallyControlled = game.aircraftInAirspace().get(manualAircraftIndex);
					System.out.println("Manual Control data: Aircraft: " + aircraftManuallyControlled.name() + " Manual Control Type: " + manualType.toString());
					if (manualType == Packet.ManualType.STOP)
						game.stopOpponentManuallyControlled(aircraftManuallyControlled);
					else
						game.opponentManuallyControlling(manualType, aircraftManuallyControlled);
				}
				break;
			}
		}
	}
	
	protected Packet recievePacket() {
		
		try {
			
			Packet recieved;
			recieved = (Packet) objInputStream.readObject();
			if (recieved.getType() == Packet.PacketType.DISCONNECT) {
				// Quit out
				lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
				lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
				killThread();
			}
			return recieved;
			
		}  catch (ClassNotFoundException e) {
			
			// Set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_NOT_FOUND);
			
			// Report error in console
			System.err.println("Could not find class Packet");
			e.printStackTrace();
			
			// Kill the thread which caused the error.
			killThread();
			
		} catch (SocketTimeoutException e){
			
			// Set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.SOCKET_TIMEOUT);
			
			// Report error in console
			System.err.println("Timed out communicating with host");
			
			// Kill the thread which caused the error.
			e.printStackTrace();
			killThread();
			
		} catch (IOException e) {
			
			// Set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.IO_ERROR_ON_RECIEVE);
			
			// Report error in console
			System.err.println("IOException communicating with host");
			e.printStackTrace();
			
			// Kill the thread which caused the error.
			killThread();
			
		} catch (ClassCastException e){
			
			// Set flags for lobby to report the error.
			lobby.setNetworkState(MultiplayerSetUp.networkStates.CONNECTION_LOST);
			lobby.setErrorCause(MultiplayerSetUp.errorCauses.CLASS_CAST_EXCEPTION);
			
			// Report error in console
			System.err.println("Recieved object does not match expected: Packet");
			
			// Kill the thread which caused the error.
			killThread();
			e.printStackTrace();
			
		}
		
		return null;
		
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
		
		/*if (buffSize != 0){
			System.out.format("Sent %d flights \n", buffSize);
			System.out.print(aircraftBuffer.toString() + "\n");
		}
		*/
		if (recv.equals(ack)){
			//tell receiver how many aircraft to expect
			sendObject(buffSize);
			//send aircraft
			for (int i = 0; i < buffSize; i++){
				Aircraft temp = aircraftBuffer.get(i);
				//remove sent aircraft from buffer
				aircraftBuffer.remove(i);
				sendObject(temp);
				System.out.println("Sent: " + temp.toString());
			}
		}
		/*if (buffSize != 0){
			System.out.format("Sent %d flights \n", buffSize);
			System.out.print(aircraftBuffer.toString());
		}*/
		//System.out.println("End Sync");
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
			System.out.println("Recv: " + a.toString());

			//check if the flight is already in the airspace
			int index = game.existsInAirspace(a);
			//System.out.format("Flight index: %d \n", index);
			
			//index of -2 indicates no matching flight
			//-1 is used to error out when a player presses escape
			//hence -2 is used to avoid confusion / errors
			if (index == -2){
				game.addFlight(a);
				//System.out.println("New flight: " + a.toString());
				//skip to next loop iteration
				continue;
			} else {
				//aircraft already in airspace
/*				System.out.println("Before:");
				System.out.println(game.aircraftInAirspace().toString());*/
				//remove from airspace
				//System.out.println("Existing flight: " + game.aircraftInAirspace().get(index).toString());
				game.aircraftInAirspace().remove(index);
				//System.out.print(" changed to: " + a.toString() + "\n");
			}
			//add new flight
			//System.out.println("New flight's altitudestate: " + a.altitudeState());
			game.aircraftInAirspace().add(a);
/*			System.out.println("After");
			System.out.println(game.aircraftInAirspace().toString());*/
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
	
	/**
	 * @return the size of the aircraftBuffer i.e. the number of aircraft awaiting sync.
	 */
	public int getBufferSize(){
		return this.aircraftBuffer.size();
	}
	
	public int getPacketBufferSize() {
		return this.packetBuffer.size();
	}
}
