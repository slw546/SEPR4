package thr;

import java.io.Serializable;

import cls.Aircraft;
import cls.Waypoint;

public class Packet implements Serializable {

	public enum PacketType {
		HANDSHAKE("Handshake"),
		ACKNOWLEDGE("Acknowledgement"),
		DISCONNECT("Disconnect"),
		LOBBYJOIN("Join Lobby"),
		LOBBYLEAVE("Leave Lobby"),
		LOBBYFIND("Find Lobby"),
		STARTGAME("Start Game"),
		BUFFERSIZE("Buffersize"),
		NEWFLIGHT("New Flight"),
		UPDATEWAYPOINT("Update Waypoint"),
		MANUALCONTROL("Manual Control"),
		SCORE("Score");
		
		private final String representation;
		
		PacketType(String rep) {
			this.representation = rep;
		}
		
		public String toString() {
			return this.representation;
		}
	}
	
	public enum ManualType {
		LEFT("Left"),
		RIGHT("Right"),
		NONE("None"),
		STOP("Stop");
		
		
		private final String representation;
		
		ManualType(String rep) {
			this.representation = rep;
		}
		
		public String toString() {
			return this.representation;
		}
	}
	
	private PacketType type;
	private Object[] data;
	
	public Packet(PacketType type, Object[] args) {
		switch(type) {
		case HANDSHAKE:
			this.type = type;
			break;
		case ACKNOWLEDGE:
			this.type = type;
			break;
		case DISCONNECT:
			this.type = type;
			break;
		case LOBBYJOIN:
			this.type = type;
			break;
		case LOBBYLEAVE:
			this.type = type;
			break;
		case LOBBYFIND:
			this.type = type;
			break;
		case STARTGAME:
			this.type = type;
			break;
		case BUFFERSIZE:
			if ((args.length != 1) || (!(args[0] instanceof Integer))) {
				System.err.println("BUFFERSIZE arguments: (Integer) [Size of incoming buffer]");
			} else {
				// Has correct arguments
				this.type = type;
				this.data = args;
			}
			break;
		case NEWFLIGHT:
			if ((args.length != 1) || (!(args[0] instanceof Aircraft))) {
				System.err.println("NEWFLIGHT arguments: (Aircraft) [New Aircraft]");
			} else {
				// Has correct arguments
				this.type = type;
				this.data = args;
			}
			break;
		case UPDATEWAYPOINT:
			if ((args.length != 3) || (!(args[0] instanceof Aircraft)) || (!(args[1] instanceof Integer)) || (!(args[2] instanceof Waypoint))) {
				System.err.println("UPDATEWAYPOINT arguments: (Aircraft, Integer, Waypoint) [Aircraft to update, index of waypoint in route, new waypoint]");
			} else {
				// Has correct arguments
				this.type = type;
				this.data = args;
			}
			break;
		case MANUALCONTROL:
			if ((args.length != 2) || (!(args[0] instanceof Aircraft)) || (!(args[1] instanceof ManualType))) {
				System.err.println("MANUALCONTROL arguments: (Aircraft, ManualType) [Aircraft being controlled, new manual control type]");
			} else {
				// Has correct arguments
				this.type = type;
				this.data = args;
			}
			break;
		case SCORE:
			if ((args.length != 2) || (!(args[0] instanceof Integer)) || (!(args[1] instanceof Integer))) {
				System.err.println("SCORE arguments: (Integer, Integer) [Host score, Client score]");
			} else {
				// Has correct arguments
				this.type = type;
				this.data = args;
			}
			break;
		}
	}
	
	public PacketType getType() {
		return this.type;
	}
	
	public Object[] getData() {
		return this.data;
	}
}
