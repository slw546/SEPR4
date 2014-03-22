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
	
	private static final int[] SPLIT_LINE_POSITIONS = new int[] {
		320, 350, 380, 420, 460, 500, 540, 580, 640, 700, 740, 780, 820, 860, 900, 930, 960
	};
	
	/** The current x co-ordinate of the line to split the game screen of the two players **/
	private int splitLine = SPLIT_LINE_POSITIONS[8];
	
	/** The x co-ordinate the split line is moving towards, equals splitLine when line is stationary **/
	private int moveSplitLineTo = 8;
	
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
	
	public void update(double dt) {
		super.update(dt);
		// Increment or decrement the splitLine towards moveSplitLineTo every update, giving a smooth transition of the split line
		if (SPLIT_LINE_POSITIONS[moveSplitLineTo] < splitLine)
			splitLine -= 1;
		if (SPLIT_LINE_POSITIONS[moveSplitLineTo] > splitLine)
			splitLine += 1;
		
		// Updates the owners for each aircraft
		for (int i = 0; i < aircraftList().size(); i ++){
			Aircraft tempAircraft = aircraftList().get(i);
			if ((tempAircraft.position().x() > splitLine) && (tempAircraft.owner() == 0)){
				// If right side player aircraft passes to left side
				if (tempAircraft.isManuallyControlled() == true){
					// The aircraft has been manually flown over to the left side
					// Player 1 looses points
					// New flight plan generated for new aircraft
				}
				else{
					// The aircraft has automatically flown over to the left side
					// Player 0 gets points
				}
				aircraftList().get(i).setOwner(1);
			}
			else if ((tempAircraft.position().x()< splitLine) && (tempAircraft.owner() == 1)){
				// If left side aircraft pases to right side
				if (tempAircraft.isManuallyControlled() == true){
					// The aircraft has been manually flown over to the right side
					// Player 0 looses points
					// New flight plan generated for new aircraft
				}
				else{
					// The aircraft has been automatically flown over to the right side
					// Player 0 gets points
				}
				aircraftList().get(i).setOwner(0);
			}
		}
	}
	
	/**
	 * When an aircraft crosses over the split line manually a new flight plan needs to be generated
	 * This function generates a new flightplan for the aircraft
	 */
	private void alterFlightPlan(Aircraft inputAircraft){
		inputAircraft.regenerateRoute();
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
	
    /**
     * Create a new aircraft object and introduce it to the airspace
     */
	@Override
    protected void generateFlight() {
        Aircraft a = createAircraft();
        if (!Main.testing) ordersBox.addOrder(
        		"<<< " + a.name() + " incoming from "+ a.originName()
        		+ " heading towards " + a.destinationName() + ".");
        
        // Check which player has control of aircraft
        if(a.position().x() < splitLine){
        	// If the aircraft is left of the line, it belongs to the left player
        	a.setOwner(0);
        }
        else{
        	// If the aircraft is right of the line, it belongs to the right player
        	a.setOwner(1);
        }
        
        aircraftInAirspace.add(a);
    }
	
	public void keyReleased(int key) {
		super.keyReleased(key);
        switch (key) {
        	case input.KEY_Q:
        		if (moveSplitLineTo > 0)
        			moveSplitLineTo -= 1;
        		break;
        	case input.KEY_E:
        		if (moveSplitLineTo < SPLIT_LINE_POSITIONS.length)
        			moveSplitLineTo += 1;
        		break;
        }
	}
	
	

}
