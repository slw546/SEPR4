package scn;

import java.util.Random;

import thr.NetworkThread;
import cls.Aircraft;
import cls.AircraftBuffer;
import cls.Airport;
import cls.Waypoint;
import cls.Aircraft.AirportState;
import cls.Aircraft.AltitudeState;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

public class MultiplayerGame extends Game {
	
	/**
	 * 17 x positions for the line to be at. 
	 */
	private static final int[] SPLIT_LINE_POSITIONS = new int[] {
		320, 350, 380, 420, 460, 500, 540, 580, 640, 700, 740, 780, 820, 860, 900, 930, 960
	};
	
	/** The current x co-ordinate of the line to split the game screen of the two players **/
	private int splitLine = SPLIT_LINE_POSITIONS[8];
	
	/** The x co-ordinate the split line is moving towards, equals splitLine when line is stationary **/
	private int moveSplitLineTo = 8;
	
	/** The time the dividing line was last moved **/
	private long lastMoveTime = System.currentTimeMillis();
	
	/** Enumerator for the two players, constructed with the colour of their controlled waypoints **/
	public enum Player {
		LEFT(0, 0, 128), // HOST
		RIGHT(128, 0, 0); // CLIENT
		
		private double r, g, b;
		private int score;
		
		private Player(double r, double g, double b) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.score = 0;
		}
		
		public double[] getColour() {
			return new double[] { r, g, b, 128.0 };
		}
		
		public int getScore() {
			return this.score;
		}
		
		public void setScore(int score) {
			this.score = score;
		}
		
		public void addScore(int score) {
			System.out.println("Adding: " + score + " to " + this.score + ". New score: " + (this.score + score));
			this.score += score;
		}
		
		public void subtractScore(int score) {
			this.score -= score;
		}
	}
	
	/**
	 * The last time a manually controlled aircraft was added to the buffer for sync
	 * Used to reduce spamming of the buffer (since manual control is in the main update loop
	 * it would otherwise be added to the buffer every update)
	 */
	private long lastManualControlSync = 0l;
	
	/**
	 * The gameType, ie. whether we are the Left (HOST) or Right (CLIENT) player.
	 * Determined by the TYPE of networkThread associated with this instance
	 * of MultiplayerGame
	 */
	private Player self;
	
	private Player opponent;
	
	/**
	 * A handle to the associated networkThread
	 * used to call functions e.g. adding aircraft to the buffer for sync
	 */
	private NetworkThread networkThread;
	
	/**
	 * A handle to the lobby scene
	 */
	private MultiplayerSetUp lobby;
	
	public lib.ButtonText landButton;
	public lib.ButtonText takeOffButton;
	
	//Constructor
	public MultiplayerGame(Main main, Player type, NetworkThread thread, MultiplayerSetUp lobby) {
		super(main, 0);
		if (type == Player.LEFT) {
			self = Player.LEFT;
			opponent = Player.RIGHT;
		} else {
			self = Player.RIGHT;
			opponent = Player.LEFT;
		}
		this.lobby = lobby;
		networkThread = thread;
		lastManualControlSync = System.currentTimeMillis();
		lib.ButtonText.Action landAction = new lib.ButtonText.Action() {
			@Override
			public void action() {
				landAircraft();
			}
		};
		landButton = new lib.ButtonText("Land", landAction, self == Player.RIGHT ? 1062 : 118, 32, 100, 25, 34, -6);
		lib.ButtonText.Action takeOffAction = new lib.ButtonText.Action() {
			@Override
			public void action() {
				takeOffAircraft();
			}
		};
		takeOffButton = new lib.ButtonText("Take Off", takeOffAction, self == Player.RIGHT ? 1062 : 118, 32, 128, 25, 18, -6);
	}
	
	/** 
	 * Determine which player controls a waypoint
	 * @param waypoint the waypoint to decide upon
	 * @return the player the waypoint is controlled by
	 */
	private Player controlledBy(Waypoint waypoint) {
		if (waypoint.position().x() < splitLine) {
			return Player.LEFT;
		} else {
			return Player.RIGHT;
		}
	}
	
	private Player owner(Aircraft aircraft) {
		if (aircraft.owner() == 0)
			return leftPlayer();
		else
			return rightPlayer();
	}
	
	private void setOwner(Aircraft aircraft, Player owner) {
		if (owner == Player.LEFT)
			aircraft.setOwner(0);
		else
			aircraft.setOwner(1);
	}
	
	private Player leftPlayer() {
		if (self == Player.LEFT)
			return self;
		else
			return opponent;
	}
	
	private Player rightPlayer() {
		if (self == Player.RIGHT)
			return self;
		else
			return opponent;
	}
	
	//Update and draw
	@Override
	public void update(double dt) {
		
		//If we are the client, never allow a flight to be generated.
		//Sidesteps the flight generator present in Game, which this class is inheriting from.
		if (self == Player.RIGHT){
			this.setFlightGenerationTimeElapsed(0);
		}
		// Update scores if aircraft have gone through waypoints
		for (Aircraft aircraft : aircraftInAirspace) {
			if (aircraft.score() > 0) {
				owner(aircraft).addScore(aircraft.score());
				aircraft.clearScore();
			}
    	}
		
		//Main game logic - uses update from scn.Game
		super.update(dt);
		
		//if holding down the left or right key and has an aircraft selected
		if (selectedAircraft != null &&
				(input.isKeyDown(input.KEY_LEFT) || input.isKeyDown(input.KEY_RIGHT))){
			//selected flight's heading has been manually altered
			//send to buffer for sync
			long sysTime = System.currentTimeMillis();
			
			//only add the flight if it's been 0.1 secs since it was last synced
			//enforces several syncs in between the last send and this
			//prevents one side spamming the other player's thread with sends
			if (lastManualControlSync + 100 < sysTime){
				//unset manual control
				//prevents other player getting an unselectable aircraft
				selectedAircraft.setManuallyControlled(false);
				//add aircraft to list for sync
				System.out.println(selectedAircraft.name() + " is manually controlled.");
				networkThread.addToBuffer(selectedAircraft);
				//update last manual sync time
				lastManualControlSync = sysTime;
				//reset manual control for local player
				selectedAircraft.setManuallyControlled(true);
			}
		}
		
		//Move line if it's been 5 seconds since the last move
		if (System.currentTimeMillis() > lastMoveTime + 5000){
			lastMoveTime = System.currentTimeMillis();
			if (leftPlayer().getScore() > rightPlayer().getScore() + 30){
				// Move line right
				if (moveSplitLineTo < SPLIT_LINE_POSITIONS.length)
					moveSplitLineTo += 1;
			} else if (leftPlayer().getScore() + 30 < rightPlayer().getScore()) {
				// Move line left
				if (moveSplitLineTo > 0)
					moveSplitLineTo -= 1;
			}
		}
		
		// Increment or decrement the splitLine towards moveSplitLineTo every update, giving a smooth transition of the split line
		if (SPLIT_LINE_POSITIONS[moveSplitLineTo] < splitLine)
			splitLine -= 1;
		if (SPLIT_LINE_POSITIONS[moveSplitLineTo] > splitLine)
			splitLine += 1;
		
		// Updates the owners for each aircraft
		for (int i = 0; i < aircraftList().size(); i ++){
			Aircraft tempAircraft = aircraftList().get(i);
			if ((tempAircraft.position().x() > splitLine) && (owner(tempAircraft) == Player.LEFT)){
				// If right side player aircraft passes to left side
				if (tempAircraft.isManuallyControlled() == true){
					// The aircraft has been manually flown over to the left side
					// Player 1 looses points
					leftPlayer().subtractScore(20);
					// Generating a new route for the aircraft
					if (self == Player.LEFT)
						regenRoute(i, tempAircraft);
					tempAircraft.setManuallyControlled(false);
				}
				else{
					// The aircraft has automatically flown over to the left side
					// Player 0 gets points
					rightPlayer().addScore(10);
				}
				// Remove selection of plane and manual control if the plane is selected while crossing the line
				if (selectedAircraft != null && selectedAircraft.equals(tempAircraft)) {
					networkThread.addToBuffer(tempAircraft);
					deselectAircraft();
				}
				setOwner(tempAircraft, rightPlayer());
			}
			else if ((tempAircraft.position().x()< splitLine) && (owner(tempAircraft) == Player.RIGHT)){
				// If left side aircraft passes to right side
				if (tempAircraft.isManuallyControlled() == true){
					// The aircraft has been manually flown over to the right side
					// Player 0 loses points
					rightPlayer().subtractScore(20);
					// Generating a new route
					if (self == Player.LEFT)
						regenRoute(i, tempAircraft);
					tempAircraft.setManuallyControlled(false);
				}
				else{
					// The aircraft has been automatically flown over to the right side
					// Player 1 gets points
					leftPlayer().addScore(10);
				}
				// Remove selection of plane and manual control if the plane is selected while crossing the line
				if (selectedAircraft != null && selectedAircraft.equals(tempAircraft)) {
					networkThread.addToBuffer(tempAircraft);
					deselectAircraft();
				}
				setOwner(tempAircraft, leftPlayer());
			}
		}
		if (splitLine == SPLIT_LINE_POSITIONS[0])
			multiGameOver(true, leftPlayer().getScore(), rightPlayer().getScore());
		else if (splitLine == SPLIT_LINE_POSITIONS[SPLIT_LINE_POSITIONS.length - 1])
			multiGameOver(false, leftPlayer().getScore(), rightPlayer().getScore());
	}
	
	private void regenRoute(int i, Aircraft tempAircraft){
		// Generating a new route for the aircraft
		Waypoint origin = tempAircraft.getRoute()[0];
		Waypoint[] waypoints = this.airspaceWaypoints;
		int numberOfWaypoints = 3;
		int d = new Random().nextInt(11);
		if (d < 5){
			d = (new Random()).nextInt(flightExitPoints.length);
		} else {
			d = (new Random()).nextInt(airports.length);
		}
		Waypoint destination = flightExitPoints[d];
		aircraftList().get(i).setRoute(aircraftList().get(i).findGreedyRoute(origin, destination, waypoints));
		networkThread.addToBuffer(selectedAircraft);
	}
	
	 public void multiGameOver(boolean leftSide, int score1, int score2) {
	    main.closeScene();
    	if (leftSide) {
    		//Line is on the left
    		if (self.equals(Player.LEFT)){
    			//Host lost
    			main.setScene(new MultiGameOver(main, score1, false, lobby, networkThread));
    		} else {
    			main.setScene(new MultiGameOver(main, score1, true, lobby, networkThread));
    		}
    	} else {
    		//Line is on the right
    		if (self.equals(Player.RIGHT)){
    			//Client lost
    			main.setScene(new MultiGameOver(main, score2, false, lobby, networkThread));
    		} else {
    			main.setScene(new MultiGameOver(main, score2, true, lobby, networkThread));
    		}
    	}
    }

	@Override
    public void draw() {
        graphics.setColour(0, 128, 0);
        graphics.rectangle(false, 16, 16, window.width() - 32, window.height() - 144);
        graphics.setViewport(16, 16, window.width() - 32, window.height() - 144);
        graphics.setColour(255, 255, 255, 60);
        graphics.drawq(background, backgroundQuad, 0, 0);
        
        for (Airport airport : airports) {
        	for (Waypoint w : airport.entryPoints()) {
        		w.draw(controlledBy(w).getColour());
        	}
        	for (Waypoint w : airport.parkingPoints()) {
        		w.draw(controlledBy(w).getColour());
        	}
        }
        
        drawMap();       
        graphics.setViewport();
       
        if (selectedAircraft != null
        		&& selectedAircraft.status() != AirportState.PARKED) {
            selectedAircraft.drawCompass();
        }
       
        ordersBox.draw();
        altimeter.draw();
        drawAircraftInfo();
       
        graphics.setColour(0, 128, 0);
        drawScore();
        super.draw();
    }
	
	@Override
	protected void drawScore() {
		int hours = (int)(timeElapsed / (60 * 60));
        int minutes = (int)(timeElapsed / 60);
        minutes %= 60;
        double seconds = timeElapsed % 60;
        java.text.DecimalFormat df = new java.text.DecimalFormat("00.00");
        String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds);
        graphics.print(timePlayed, window.width() - (timePlayed.length() * 8 + 32), 30);
        graphics.print(String.valueOf(aircraftInAirspace.size())
        		+ " aircraft in the airspace.", 32, 30);
       
        // GOA CODE FOLLOWS
        String earnings = "";
        // Write total score to string for printing
        earnings = String.format("`%d earned for family, `%d earned by opponent.", self.getScore(), opponent.getScore());
        
        // Print previous string in bottom centre of display
        graphics.print(earnings, ((window.width()/2)-((earnings.length()*8)/2)),
        		ORDERSBOX_Y - 40);
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
        	if (selectedAircraft.status() == AirportState.WAITING) {
        		graphics.setColour(0, 0, 0);
        		graphics.rectangle(true, self == Player.RIGHT ? 1062 : 118, 16, 100, 25);
        		graphics.setColour(0, 128, 0);
        		graphics.rectangle(false, self == Player.RIGHT ? 1062 : 118, 16, 100, 25);
        		landButton.draw();
        	}
        	if (selectedAircraft.status() == AirportState.PARKED) {
        		graphics.setColour(0, 0, 0);
        		graphics.rectangle(true, self == Player.RIGHT ? 1062 : 118, 16, 100, 25);
        		graphics.setColour(0, 128, 0);
        		graphics.rectangle(false, self == Player.RIGHT ? 1062 : 118, 16, 100, 25);
        		takeOffButton.draw();
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
		addFlight(a);
		System.out.println(a.name() + " created.");
		//add the aircraft to the thread's buffer to be sent.
		networkThread.addToBuffer(a);
    }
	
	/**
	 * Adds a flight to the airspace and reports it in the order box
	 * @param a the aircraft to be added
	 */
	public void addFlight(Aircraft a){
		if (!Main.testing) ordersBox.addOrder(
				"<<< " + a.name() + " incoming from "+ a.originName()
				+ " heading towards " + a.destinationName() + ".");

		// Check which player has control of aircraft
		if(a.position().x() < splitLine){
			// If the aircraft is left of the line, it belongs to the left player
			setOwner(a, leftPlayer());
			//a.setOwner(0);
		}
		else{
			// If the aircraft is right of the line, it belongs to the right player
			setOwner(a, rightPlayer());
			//a.setOwner(1);
		}

		//add the aircraft to the list of the aircraft in the airspace
		aircraftInAirspace.add(a); 
	}
	
	/**
	 * Checks the aircraftInAirspace to see if a flight already exists
	 * @param a the aircraft to check the airspace for
	 * @return the index of the flight if it exists in the airspace, else -2
	 * -2 used since -1 is in use as an error code due to a player quitting.
	 */
	public int existsInAirspace(Aircraft a){
		//check for equality using aircraft's unique names
		//cant use ArrayList.contains(object o) since new flights will have different attributes
		//e.g. differeny x,y pos, altitude, etc.
		//Hence contains(o) will not consider them equal.
		String name = a.name();
		for (int i = 0; i < aircraftInAirspace.size(); i++){
			if (aircraftInAirspace.get(i).name().equals(name)){
				return i;
			}
		}
		//no match
		return -2;
	}
	
	
	//Input handler
	@Override
	public void keyReleased(int key) {
		super.keyReleased(key);
        switch (key) {
        	case input.KEY_ESCAPE:
        		//main.closeScene() called in super.keyReleased.
        		networkThread.escapeThread();
        		break;
        	case input.KEY_UP:
        		if (selectedAircraft != null){
        			//Aircraft was ordered to ascend, add it to buffer to be sent
        			System.out.println(selectedAircraft.name() + " ordered to ascend");
        			selectedAircraft.setAltitudeState(Aircraft.AltitudeState.CLIMBING);
        			networkThread.addToBuffer(selectedAircraft);
        		}
        		break;
        	case input.KEY_DOWN:
        		if (selectedAircraft != null){
        			//Aircraft was ordered to descend, add it to buffer to be sent
        			System.out.println(selectedAircraft.name() + " ordered to descend");
        			selectedAircraft.setAltitudeState(Aircraft.AltitudeState.FALLING);
        			networkThread.addToBuffer(selectedAircraft);
        		}//
        		break;
        	case input.KEY_T:
        		//aircraft ordered to take off
        		//add it to buffer to be sent
        		if (selectedAircraft != null){
        			System.out.println(selectedAircraft.name() + " ordered to take off");
        			networkThread.addToBuffer(selectedAircraft);
        		}
        		break;
        	case input.KEY_L:
        		//aircraft ordered to land
        		//add it to buffer to be sent
        		if (selectedAircraft != null){
        			System.out.println(selectedAircraft.name() + " ordered to land");
        			networkThread.addToBuffer(selectedAircraft);
        		}
        		break;
        }
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
                	if (owner(a) == self)
                		newSelected = a;
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
                    		&& controlledBy(w) == self
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
        	Aircraft temp = selectedAircraft;
        	deselectAircraft();	
        	networkThread.addToBuffer(temp);
        }
        
        // Trigger altimeter actions - not in use?
        altimeter.mousePressed(key, x, y);
    }
    
    @Override
    //Overridden to allow aircraft to be sync at sensible places in the code
    //rather than only before or after a super.mouseReleased()
    public void mouseReleased(int key, int x, int y){
    	if (selectedAircraft != null) {
    		if (landButton.isMouseOver(x, y)) landButton.act();
    		if (takeOffButton.isMouseOver(x, y)) takeOffButton.act();
    	}
    	//SYNC flight path change
    	if (key == input.MOUSE_LEFT && selectedAircraft != null
    			&& selectedWaypoint != null) {
    		for (Waypoint w : airspaceWaypoints) {
    			// Only allow route edit if waypoint isn't an
    			// entry or exit point, and when aircraft is in its
    			// 'normal' state
    			if (selectedAircraft.status() == AirportState.NORMAL) {
    				if ((w.type() == Waypoint.WaypointType.AIRSPACE) && controlledBy(w) == self && w.isMouseOver(x-16, y-16)) {
    					selectedAircraft.alterPath(selectedPathpoint, w);
    					ordersBox.addOrder(">>> " + selectedAircraft.name()
    							+ " please alter your course");
    					ordersBox.addOrder("<<< Roger that. Altering course now.");
    					selectedPathpoint = -1;
    					selectedWaypoint = null;
    					System.out.println(selectedAircraft.name() + " route was altered");
    					networkThread.addToBuffer(selectedAircraft);
    				} else {
    					selectedWaypoint = null;
    				}
    			}
    		}
    	}
    	
    	// Change altitude when altitude buttons pressed
        AltitudeState altitudeState = AltitudeState.LEVEL;
        if (selectedAircraft != null) {
            altitudeState = selectedAircraft.altitudeState();
        }
        
        //act on mouse press
        altimeter.mouseReleased(key, x, y);
        
        //SYNC altimeter altitude change
        //If altimeter action changed altitude state
        if (selectedAircraft != null) {
            if ((altitudeState != selectedAircraft.altitudeState())
            		&& (selectedAircraft.status() == AirportState.NORMAL)) {
                ordersBox.addOrder(">>> " + selectedAircraft.name() + ", please adjust your altitude");
                ordersBox.addOrder("<<< Roger that. Altering altitude now.");
                System.out.println(selectedAircraft.name() + " altitude changed via altimeter");
				networkThread.addToBuffer(selectedAircraft);
            }
        }
        
        //SYNC compass direction change
        // Change bearing when compass clicked
        // But only when the aircraft is in the 'normal'
        // or 'waiting' states
        if (selectedAircraft != null
        		&& (selectedAircraft.status() == AirportState.NORMAL)
        		&& (selectedAircraft.status() == AirportState.WAITING)) {
        	if (compassDragged) {
        		if (selectedAircraft.manualBearing() != Double.NaN) {
        			double dx = input.mouseX() - selectedAircraft.position().x();
        			double dy = input.mouseY() - selectedAircraft.position().y();
        			double newHeading = Math.atan2(dy, dx);
        			selectedAircraft.setBearing(newHeading);
        			selectedAircraft.setManuallyControlled(true);
        			System.out.println(selectedAircraft.name() + " heading altered via compass");
        			networkThread.addToBuffer(selectedAircraft);
        		}
        	}
        }
        compassDragged = false;
  
    }
    
    public void setOpponentScore(int opponentScore) {
    	opponent.setScore(opponentScore);
    }
    
    public int getSelfScore() {
    	return self.getScore();
    }
    
    public void setLastMoveTime(long moveTime){
    	this.lastMoveTime = moveTime;
    }
    
    @Override
    protected void crash(Aircraft aircraft, int collisionState) {
    	self.subtractScore(100);
    	ordersBox.addOrder("<<< You crashed two planes! That is coming out of your pay!");
    	main.screenShake(24, 0.6);
    }
	
}
