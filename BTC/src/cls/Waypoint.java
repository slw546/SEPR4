package cls;

import java.io.Serializable;

import lib.jog.graphics;

public class Waypoint implements Serializable {
	
	/**
	 * serialVersionUID used to check consistency between host and reciever
	 * Required for network communication of objects
	 */
	private static final long serialVersionUID = 5196702347805188897L;

	public enum WaypointType implements Serializable {
		AIRSPACE,
		ENTRY,
		EXIT,
		AIRPORT,
	}
	
	/** Leniency to allow mouse input to be accepted in a small area around the waypoint */
	public final static int MOUSE_LENIENCY = 16;
	
	/** Radius of the waypoint icon */
	final protected int RADIUS = 8;
	
	/** Location of the waypoint */
	protected Vector position;
	
	/** Marks whether the waypoint is a point where aircraft may enter and
	 * exit the airspace */
	private WaypointType type;
	
	/**
	 * Constructor for waypoints
	 * @param x the x coord of the waypoint
	 * @param y the y coord of the waypoint
	 * @param type whether the waypoint is:
	 * 					0: a normal waypoint, 
	 * 					1: a point where aircraft may enter and leave the airspace, 
	 * 					2: an airport waypoint
	 */
	public Waypoint(double x, double y, WaypointType type) {
		this.position = new Vector(x, y, 0);
		this.type = type;
		
		// Scale points to fit on screen
		// Entry and exit points are scaled automatically
		if (type != WaypointType.AIRSPACE) {
			Vector scaledPosition = position.remapPosition();
			position = scaledPosition;
		}
	}
	
	/**
	 * Gets the waypoint position
	 * @return the position of the waypoint.
	 */
	public Vector position() {
		return position;
	}
	
	/**
	 * Checks if the mouse is over the waypoint, within MOUSE_LENIENCY
	 * @param mx the mouse's x location
	 * @param my the mouse's y location
	 * @return whether the mouse is considered over the waypoint.
	 */
	public boolean isMouseOver(int mx, int my) {
		double dx = position.x() - mx;
		double dy = position.y() - my;
		return dx*dx + dy*dy < MOUSE_LENIENCY*MOUSE_LENIENCY;
	}
	
	/**
	 * Gets the waypoints type.
	 * @return Waypoint type.
	 */
	public WaypointType type() {
		return this.type;
	}
	
	/**
	 * Gets the cost of travelling between this waypoint and another
	 * Used for pathfinding
	 * @param fromPoint The point to consider cost from, to this waypoint
	 * @return the distance (cost) between the two waypoints
	 */
	public double getCost(Waypoint fromPoint) {
		return position.sub(fromPoint.position()).magnitude();
	}
	
	/**
	 * Gets the cost between two waypoints
	 * @param source the source waypoint
	 * @param target the target waypoint
	 * @return the cost between source and target
	 */
	public static double getCostBetween(Waypoint source, Waypoint target) {
		return target.getCost(source);
	}
	
	/**
	 * Draws the waypoint
	 * @param x the x location to draw at
	 * @param y the y location to draw at
	 */
	public void draw(double x, double y) {
		graphics.setColour(128, 0, 0, 128); 
		graphics.circle(false, x, y, RADIUS);
		graphics.circle(true, x, y, RADIUS - 2);
	}

	/**
	 * Draws the waypoint at its current position
	 */
	public void draw() {
		draw(position.x(), position.y());
	}
	
	/**
	 * Draws the waypoint with a specified colour at its current position
	 * @param colour the colour of the waypoint
	 */
	public void draw(double[] colour) {
		graphics.setColour(colour[0], colour[1], colour[2], colour[3]);
		graphics.circle(false, position.x(), position.y(), RADIUS);
		graphics.circle(true, position.x(), position.y(), RADIUS - 2);
	}
	
	/**
	 * Outputs the waypoint's key details in a readable format.
	 * @return the textual representation of the vector
	 */
	public String toString() {
		return ("Pos: " + position.toString() + " | " + "Type: " + type.toString());
	}

}
