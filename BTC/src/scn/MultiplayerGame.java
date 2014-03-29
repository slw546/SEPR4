package scn;

import java.io.Serializable;

import thr.NetworkThread;
import cls.Aircraft;
import cls.Airport;
import cls.Waypoint;
import cls.Aircraft.AirportState;
import cls.Aircraft.AltitudeState;
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
	
	public enum Type {
		CLIENT, HOST
	}
	
	private Type gameType;
	
	private NetworkThread networkThread;
	
	private int opponentScore = 0;
	
	//Constructor
	public MultiplayerGame(Main main, Type type, NetworkThread thread) {
		super(main, 0);
		gameType = type;
		networkThread = thread;
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
	
	//Update and draw
	@Override
	public void update(double dt) {
		super.update(dt);
		
		//If we are the client, never allow a flight to be generated.
		//Sidesteps the flight generator present in Game, which this class is inheriting from.
		if (gameType.equals(Type.CLIENT)){
			this.setFlightGenerationTimeElapsed(0);
		}
		
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

					// Generating a new route for the aircraft
					Waypoint origin = tempAircraft.getRoute()[0];
					Waypoint[] waypoints = tempAircraft.getRoute();
					int numberOfWaypoints = tempAircraft.getRoute().length;
					Waypoint destination = tempAircraft.getRoute()[numberOfWaypoints - 1];
					aircraftList().get(i).findGreedyRoute(origin, destination, waypoints);
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
					
					// Generating a new route
					Waypoint origin = tempAircraft.getRoute()[0];
					Waypoint[] waypoints = tempAircraft.getRoute();
					int numberOfWaypoints = tempAircraft.getRoute().length;
					Waypoint destination = tempAircraft.getRoute()[numberOfWaypoints - 1];
					aircraftList().get(i).findGreedyRoute(origin, destination, waypoints);
				}
				else{
					// The aircraft has been automatically flown over to the right side
					// Player 0 gets points
				}
				aircraftList().get(i).setOwner(0);
			}
		}
	}
	
	@Override
	protected void drawScore() {
		int hours = (int)(timeElapsed / (60 * 60));
        int minutes = (int)(timeElapsed / 60);
        minutes %= 60;
        double seconds = timeElapsed % 60;
        java.text.DecimalFormat df = new java.text.DecimalFormat("00.00");
        String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds);
        graphics.print(timePlayed, window.width() - (timePlayed.length() * 8 + 32), 0);
        graphics.print(String.valueOf(aircraftInAirspace.size())
        		+ " aircraft in the airspace.", 32, 0);
       
        // GOA CODE FOLLOWS
        
        // Write total score to string for printing
        String earnings = String.format("`%d earned for family, `%d earned by opponent.", totalScore, opponentScore);
        
        // Print previous string in bottom centre of display
        graphics.print(earnings, ((window.width()/2)-((earnings.length()*8)/2)),
        		ORDERSBOX_Y - 20);
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
	
	
	//Aircraft functions	
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
        
        //add the aircraft to the thread's buffer to be sent.
        networkThread.addToBuffer(a);
        //add the aircraft to the list of the aircraft in the airspace
        aircraftInAirspace.add(a);
    }
	
	
	//Input handler
	@Override
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
        	case input.KEY_ESCAPE:
        		//main.closeScene() called in super.keyReleased.
        		networkThread.escapeThread();
        		break;
        }
	}

	public void setOpponentScore(int opponentScore) {
		this.opponentScore = opponentScore;
	}
	
    /**
     * Handle mouse input
     */
    @Override
    public void mousePressed(int key, int x, int y) {
    	if (key == input.MOUSE_LEFT) {
        	// If a aircraft is clicked, select it
            Aircraft newSelected = selectedAircraft;
            for (Aircraft a : aircraftInAirspace) {
                /*if (a.isMouseOver(x-16, y-16)
                		&& aircraftSelectableAtAltitude(a, controlAltitude)) {
                    newSelected = a;
                }*/
                           	
                if (a.isMouseOver(x-16, y-16)){
                	// Check for ownership
                	if ((gameType.equals(Type.HOST) && (a.owner() == 0))){
                        newSelected = a;
                	}
                	else if ((gameType.equals(Type.CLIENT) && (a.owner() == 1))){
                		newSelected = a;
                	}
                }
            }
            
            boolean dontChangeCourse = false;
            
            // If the aircraft clicked is not the one already selected, select it
            // Provided the selected aircraft is not finished, landing or taking off
            if (newSelected != null
            		&& newSelected.status() != AirportState.FINISHED
            		&& newSelected.status() != AirportState.LANDING
            		&& newSelected.status() != AirportState.TAKEOFF) {
            	if (newSelected != selectedAircraft) {
            		deselectAircraft();
            		selectedAircraft = newSelected;
            		dontChangeCourse = true;
            	}
            }
            altimeter.show(selectedAircraft);
            
            if (selectedAircraft != null) {
                for (Waypoint w : airspaceWaypoints) {
                    if ((w.type() == Waypoint.WaypointType.AIRSPACE)
                    		&& w.isMouseOver(x-16, y-16)
                    		&& selectedAircraft.flightPathContains(w) > -1) {
                        selectedWaypoint = w;
                        selectedPathpoint = selectedAircraft.flightPathContains(w);
                    }
                }
                
                if (selectedWaypoint == null && !dontChangeCourse) {
                    // If mouse is over compass
                    double dx = selectedAircraft.position().x() - input.mouseX();
                    double dy = selectedAircraft.position().y() - input.mouseY();
                    int r = Aircraft.COMPASS_RADIUS;
                    if (dx*dx + dy*dy < r*r) {
                        compassDragged = true;
                    }
                }
            }
        }
        
        // Deselect aircraft on right click
        if (key == input.MOUSE_RIGHT) {
        	deselectAircraft();
        }
        
        // Trigger altimeter actions - not in use?
        altimeter.mousePressed(key, x, y);
    }
	
}
