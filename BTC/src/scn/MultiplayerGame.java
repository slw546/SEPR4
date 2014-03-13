package scn;

import java.io.Serializable;

import cls.Aircraft;
import cls.Airport;
import cls.Waypoint;
import cls.Aircraft.AirportState;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

public class MultiplayerGame extends Game {
	
	/** The current x co-ordinate of the line to split the game screen of the two players **/
	private int splitLine = window.width()/2 - 16;
	
	/** Enumerator for the two players, constructed with the colour of their controlled waypoints **/
	public enum Player {
		LEFT(0, 0, 128),
		RIGHT(128, 0, 0);
		
		private double r, g, b;
		
		private Player(double r, double g, double b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public double[] getColour() {
			return new double[] { r, g, b, 128.0 };
		}
	}
	
	/** 
	 * Determine which player controls a waypoint
	 * @param waypoint the waypoint to decide upon
	 * @return the player the waypoint is controlled by
	 */
	private Player controlledBy(Waypoint waypoint) {
		if (waypoint.position().x() < splitLine)
			return Player.LEFT;
		else
			return Player.RIGHT;
	}

	public MultiplayerGame(Main main) {
		super(main, 0);
	}
	
	/**
     * Draw waypoints, and route of a selected aircraft between waypoints
     * print waypoint names next to waypoints
     */
	@Override
	protected void drawMap() {
		for (Waypoint waypoint : airspaceWaypoints) {
            waypoint.draw(controlledBy(waypoint).getColour());
        }
        
        graphics.setColour(255, 255, 255, 108);
        
        graphics.line(splitLine, 0, splitLine, window.height() - 144);
        
        graphics.setColour(255, 255, 255);
        for (Aircraft aircraft : aircraftInAirspace) {
            aircraft.draw(0);
        }
       
        if (selectedAircraft != null) {
        	if (selectedAircraft.status() == AirportState.NORMAL) {
        		// Flight Path
        		selectedAircraft.drawFlightPath();
        		graphics.setColour(0, 128, 0);
        		selectedAircraft.drawFlightPath();
        		graphics.setColour(0, 128, 0);
        	}
        }
       
        if (selectedWaypoint != null) {
            selectedAircraft.drawModifiedPath(
            		selectedPathpoint, input.mouseX() - 16, input.mouseY() - 16);
        }
       
        graphics.setViewport();
        graphics.setColour(0, 128, 0);
        
        graphics.print(FLIGHT_ENTRY_POINT_NAMES[0],
        		flightEntryPoints[0].position().x() + 25,
        		flightEntryPoints[0].position().y() + 10);
        graphics.print(FLIGHT_EXIT_POINT_NAMES[0],
        		flightExitPoints[0].position().x() + 25,
        		flightExitPoints[0].position().y() + 10);
        graphics.print(FLIGHT_ENTRY_POINT_NAMES[1],
        		flightEntryPoints[1].position().x() - 70,
        		flightEntryPoints[1].position().y() + 10);
        graphics.print(FLIGHT_EXIT_POINT_NAMES[1],
        		flightExitPoints[1].position().x() - 50,
        		flightExitPoints[1].position().y() + 10);
        
        for (Airport airport : airports) {
        	String[] names = airport.entryPointNames();
        	Waypoint[] points = airport.entryPoints();
        	
        	graphics.print(names[0],
        			points[0].position().x() - 16,
        			points[0].position().y() - 5);
        	
        	graphics.print(names[names.length - 1],
        			points[points.length - 1].position().x() - 16,
        			points[points.length - 1].position().y() - 5);
        }
	}
	
	public void keyReleased(int key) {
		super.keyReleased(key);
        switch (key) {
        	case input.KEY_Q:
        		if (splitLine > (window.width()*0.25))
        			splitLine -= 50;
        		break;
        	case input.KEY_E:
        		if (splitLine < (window.width()*0.75))
        			splitLine += 50;
        		break;
        }
	}

}
