
package scn;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import lib.jog.audio;
import lib.jog.audio.Music;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.window;

import cls.Aircraft;
import cls.Aircraft.AirportState;
import cls.Aircraft.AltitudeState;
import cls.Airport;
import cls.Altimeter;
import cls.OrdersBox;
import cls.Waypoint;

import btc.Main;

public class Game extends Scene {
   
    // Position of things drawn to window  
    private final int AIRCRAFT_INFO_X = 16;
    private final int AIRCRAFT_INFO_Y = window.height() - 120;
    private final int AIRCRAFT_INFO_W = window.width()/4 - 16;
    private final int AIRCRAFT_INFO_H = 112;
   
    private final int ALTIMETER_X = AIRCRAFT_INFO_X + AIRCRAFT_INFO_W + 8;
    private final int ALTIMETER_Y = window.height() - 120;
    private final int ALTIMETER_W = 244;
    private final int ALTIMETER_H = 112;
   
    private final int ORDERSBOX_X = ALTIMETER_X + ALTIMETER_W + 8;
    private final static int ORDERSBOX_Y = window.height() - 120;
    private final int ORDERSBOX_W = window.width() - (ORDERSBOX_X + 16);
    private final static int ORDERSBOX_H = 112;
   
    // Difficulty of demo scene determined by difficulty selection scene
    public final static int DIFFICULTY_EASY = 0;
    public final static int DIFFICULTY_MEDIUM = 1;
    public final static int DIFFICULTY_HARD = 2;
    
    public static int difficulty = DIFFICULTY_MEDIUM;
   
    /** Orders box to print orders from ACTO to aircraft to */
    private OrdersBox ordersBox;
   
    /** Time since the scene began */
    private double timeElapsed;
   
    /** Score attained by the user as a result of successful flights */
    private int totalScore;

    /** The currently selected aircraft */
    protected Aircraft selectedAircraft;
    
    /** The currently selected waypoint */
    protected Waypoint selectedWaypoint;
    
    /** Selected path point, in an aircraft's route, used for altering the route */
    protected int selectedPathpoint;
    
    /** A list of all aircraft present in the airspace */
    protected ArrayList<Aircraft> aircraftInAirspace;
   
    /** The image to be used for aircraft */
    private Image aircraftImage;
    
    /** Tracks if manual heading compass of a manually controller
     * aircraft has been dragged */
    private boolean compassDragged;
    
    /** An altimeter to display aircraft altitidue, heading, etc. */
    private Altimeter altimeter;
    
    /** The interval in seconds to generate flights after */
    private double flightGenerationInterval = 12;
    
    /** The time elapsed since the last flight was generated */
    private double flightGenerationTimeElapsed = 10;
    
    /** Maximum number of aircraft allowed in the airspace at once */
    private int maxAircraft = 6;
    
    /** Total number of aircraft to generate */
    private int maxTotalAircraft = 16;
    
    /** Number of aircraft generated so far */
    private int totalAircraft;
    
    /** The current control altitude of the ATCO */
    //private int controlAltitude = 30000;
   
    /** Music to play during the game scene */
    private Music music;
    
    /** The background to draw in the airspace. */
    private Image background;


    /** List of names to be assigned to flight entry points */
    public static final String[] FLIGHT_ENTRY_POINT_NAMES = new String[] {
    	"Moscow",
    	"Khorlovo",
    };
    
    /** List of names to be assigned to flight exit points */
    public static final String[] FLIGHT_EXIT_POINT_NAMES = new String[] {
    	"Malino",
		"Peski",
    };
    
    /** Create the set of waypoints that are flight entry points */
    public static Waypoint[] flightEntryPoints = new Waypoint[] {
    	new Waypoint(8, 8, Waypoint.WaypointType.ENTRY), // top left
    	new Waypoint(window.width() - 40, window.height() - ORDERSBOX_H - 40, Waypoint.WaypointType.ENTRY), // bottom right
    };
    
    /** Create the set of waypoints that are flight exit points */
    public static Waypoint[] flightExitPoints = new Waypoint[] {
    	new Waypoint(8, window.height() - ORDERSBOX_H - 40, Waypoint.WaypointType.EXIT), // bottom left
    	new Waypoint(window.width() - 40, 8, Waypoint.WaypointType.EXIT), // top right
    };
    
    /** The set of airports in the airspace */
    public static Airport[] airports;

    /** All waypoints in the airspace, <b>including</b> location waypoints. */
    public static Waypoint[] airspaceWaypoints = new Waypoint[] {
    	// Airspace waypoints
    	new Waypoint(125, 70, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(200, 635, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(250, 400, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(300, 100, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(450, 605, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(515, 300, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(600, 700, Waypoint.WaypointType.AIRSPACE),
    	new Waypoint(670, 400, Waypoint.WaypointType.AIRSPACE),

    	//Flight entry and exit points
    	flightEntryPoints[0],
    	flightEntryPoints[1],
    	flightExitPoints[0],
    	flightExitPoints[1],
    };

	/**
     * Constructor
     * @param main the main containing the scene
     * @param difficulty the difficulty the scene is to be initialised with
     */
    public Game(Main main, int difficulty) {
        super(main);
        Game.difficulty = difficulty;
    }
    
    public static int difficulty() {
    	return difficulty;
    }
    
    public Aircraft selectedAircraft() {
		return selectedAircraft;
	}

	public void setSelectedAircraft(Aircraft selectedAircraft) {
		this.selectedAircraft = selectedAircraft;
	}
	
    public int totalScore() {
    	return this.totalScore;
    }
   
    public ArrayList<Aircraft> aircraftInAirspace() {
		return aircraftInAirspace;
	}

	public void setAircraftInAirspace(ArrayList<Aircraft> aircraftInAirspace) {
		this.aircraftInAirspace = aircraftInAirspace;
	}
	
	/**
     * Getter for aircraft list
     * @return the arrayList of aircraft in the airspace
     */
    public ArrayList<Aircraft> aircraftList() {
        return aircraftInAirspace;
    }

    /**
     * Initialise and begin music, init background image and scene variables.
     * Shorten flight generation timer according to difficulty
     */
    @Override
    public void start() {
    	if (!Main.testing) {
    		background = graphics.newImage("gfx" + File.separator + "map.png");
        	music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
        	music.play();
    	}
    	
        ordersBox = new OrdersBox(ORDERSBOX_X, ORDERSBOX_Y,
        		ORDERSBOX_W, ORDERSBOX_H, 6, Main.testing);
        
        aircraftInAirspace = new ArrayList<Aircraft>();
        
        if (!Main.testing) { 
        	aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");
        }
        
        timeElapsed = 0;
        compassDragged = false;
        selectedAircraft = null;
        selectedWaypoint = null;
        selectedPathpoint = -1;
        totalAircraft = 0;
       
        if (!Main.testing) {
        	altimeter = new Altimeter(ALTIMETER_X, ALTIMETER_Y, ALTIMETER_W, ALTIMETER_H);
        }
        
        deselectAircraft();
       
        // Set attributes according to the selected difficulty
        // Flights spawn more often on harder difficulties.
        switch (difficulty) {
        	case DIFFICULTY_EASY:
        		break;
        	case DIFFICULTY_MEDIUM:
        		flightGenerationInterval = flightGenerationInterval / 1.3;
        		break;
        	case DIFFICULTY_HARD:
        		flightGenerationInterval = flightGenerationInterval / 1.6;
        		break;
        }
        // Airport Setup
        // Waypoints an aircraft will turn through to reach the runway
        Waypoint[] entryWaypoints = new Waypoint[] {
        		new Waypoint(677, 44, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(767, 0, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(873, 0, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(957, 44, Waypoint.WaypointType.AIRPORT),
        };
        // Names of above waypoints
        String[] entryWaypointNames = new String[] { "Chkalovsky West", "Chkalovsky East" };
        // Waypoints an aircraft will pass through while landing
        Waypoint[] landingWaypoints = new Waypoint[] {
        		new Waypoint(817, 104, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(795, 726, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(825, 754, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(842, 652, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(866, 656, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(882, 546, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(950, 496, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(970, 486, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1010, 486, Waypoint.WaypointType.AIRPORT),
        };
        // Waypoints an aircraft can park at
        Waypoint[] parkingWaypoints = new Waypoint[] {
        		new Waypoint(1092, 358, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1113, 425, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1108, 493, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1085, 567, Waypoint.WaypointType.AIRPORT),
        };
        // Waypoints an aircraft will pass through while taking off
        Waypoint[] takeoffWaypoints = new Waypoint[] {
        		new Waypoint(1010, 486, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1001, 488, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(991, 671, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(954, 678, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(954, 655, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(976, 46, Waypoint.WaypointType.AIRPORT),
        };
        
        airports = new Airport[2];
        airports[0] = new Airport("Chkalovsky Airport", 4,
        			entryWaypointNames,
        			entryWaypoints,
        			landingWaypoints,
        			parkingWaypoints,
        			takeoffWaypoints);
        
        entryWaypoints = new Waypoint[] {
        		new Waypoint(677, 44, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(767, 0, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(873, 0, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(957, 44, Waypoint.WaypointType.AIRPORT),
        };
        entryWaypointNames = new String[] { "Syrrilicovich West", "Syrrilicovich East" };
        landingWaypoints = new Waypoint[] {
        		new Waypoint(817, 104, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(795, 726, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(825, 754, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(842, 652, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(866, 656, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(882, 546, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(950, 496, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(970, 486, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1010, 486, Waypoint.WaypointType.AIRPORT)
        };
        parkingWaypoints = new Waypoint[] {
        		new Waypoint(1092, 358, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1113, 425, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1108, 493, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1085, 567, Waypoint.WaypointType.AIRPORT),
        };
        takeoffWaypoints = new Waypoint[] {
        		new Waypoint(1010, 486, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(1001, 488, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(991, 671, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(954, 678, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(954, 655, Waypoint.WaypointType.AIRPORT),
        		new Waypoint(976, 46, Waypoint.WaypointType.AIRPORT),
        };
        airports[1] = new Airport("Syrrilicovich Airport", 4,
        			entryWaypointNames,
        			entryWaypoints,
        			landingWaypoints,
          			parkingWaypoints,
          			takeoffWaypoints);
    }
   
    /**
     * Update all objects within the scene, ie aircraft, orders box altimeter.
     * Cause collision detection to occur
     * Generate a new flight if flight generation interval has been exceeded.
     */
    @Override
    public void update(double dt) {
    	timeElapsed += dt;
    	ordersBox.update(dt);
    	
    	for (Aircraft aircraft : aircraftInAirspace) {
    		aircraft.update(dt);
    		if (aircraft.status() == AirportState.FINISHED) {
    			if(aircraft.score() > 50) {
    				switch ((new Random()).nextInt(3)) {
    				case 0:
    					ordersBox.addOrder(">>> Success to us!");
    					break;
    				case 1:
    					ordersBox.addOrder(">>> Good job comrade.");
    					break;
    				case 2:
    					ordersBox.addOrder(">>> Many thanks.");
    					break;
    				}
    				// Update cumulative score
    				totalScore += aircraft.score();
    				aircraft.clearScore();
    			} else if (aircraft.score() > 0
    					&& aircraft.status() == AirportState.FINISHED) {
    				ordersBox.addOrder(">>> You deserve nothing.");
    				
    				// Update cumulative score
    				totalScore += aircraft.score();
    				aircraft.clearScore();
    			}               
    		}
    	}

    	checkCollisions(dt);

    	for (int i = aircraftInAirspace.size()-1; i >=0; i--) {
    		if (aircraftInAirspace.get(i).status() == AirportState.FINISHED) {
    			if (aircraftInAirspace.get(i) == selectedAircraft) {
    				deselectAircraft();
    			}
    			aircraftInAirspace.remove(i);
    			totalAircraft++;
    			
    			// Game win
    			if (totalAircraft == maxTotalAircraft) {
    				gameWin(totalScore);
    			}
    		} else if ((aircraftInAirspace.get(i)
    						.status() == AirportState.LANDING)
    				|| (aircraftInAirspace.get(i)
    						.status() == AirportState.TAKEOFF)) {
    			if (aircraftInAirspace.get(i) == selectedAircraft) {
    				deselectAircraft();
    			}
    		}
    	}

    	if (!Main.testing) altimeter.update(dt);

    	if (selectedAircraft != null) {
    		if (selectedAircraft.status() == AirportState.NORMAL
    				|| selectedAircraft.status() == AirportState.WAITING) {
    			if (input.isKeyDown(input.KEY_LEFT)) {
    				selectedAircraft.setManuallyControlled(true);
    				selectedAircraft.turnLeft(dt);
    			} else if (input.isKeyDown(input.KEY_RIGHT)) {
    				selectedAircraft.setManuallyControlled(true);
    				selectedAircraft.turnRight(dt);
    			}
    		}
    		
    		if (selectedAircraft.isManuallyControlled()) {
    			if (selectedAircraft.outOfBounds()) {
    				ordersBox.addOrder(">>> " + selectedAircraft.name()
    						+ " out of bounds, returning to route");
    				deselectAircraft();
    			}
    		}
    	}

    	flightGenerationTimeElapsed += dt;

    	if(flightGenerationTimeElapsed >= flightGenerationInterval) {
    		flightGenerationTimeElapsed -= flightGenerationInterval;
    		if (aircraftInAirspace.size() < maxAircraft
    				&& (totalAircraft + aircraftInAirspace.size())
    				< maxTotalAircraft) {
    			generateFlight();
    		}
    	}

    	// Run airports
    	for (Airport airport : airports) {
    		airport.update(dt);
    	}
    }
   
    /**
     * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints
     */
    @Override
    public void draw() {
        graphics.setColour(0, 128, 0);
        graphics.rectangle(false, 16, 16, window.width() - 32, window.height() - 144);
        graphics.setViewport(16, 16, window.width() - 32, window.height() - 144);
        graphics.setColour(255, 255, 255, 60);
        graphics.drawq(background, graphics.newQuad(0, 0, background.width(), background.height(),
        		background.width() * (Main.width() / Main.TARGET_WIDTH),
        		background.height() * (Main.width() / Main.TARGET_WIDTH)),
        		0, 0);
        
        for (Airport airport : airports) {
        	Waypoint[] entryPoints = airport.entryPoints();
        	//Waypoint[] landingPoints = airport.landingPoints();
        	Waypoint[] parkingPoints = airport.parkingPoints();
        	//Waypoint[] takeoffPoints = airport.takeoffPoints();
        	
        	for (int j = 0; j < entryPoints.length; j++) {
        		entryPoints[j].draw();
        	}

        	/*for (int k = 0; k < landingPoints.length; k++) {
        		landingPoints[k].draw();
        	}*/
        	
        	for (int m = 0; m < parkingPoints.length; m++) {
        		parkingPoints[m].draw();
        	}
        	
        	/*for (int n = 0; n < takeoffPoints.length; n++) {
        		takeoffPoints[n].draw();
        	}*/
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
    }
   
    /**
     * Draw waypoints, and route of a selected aircraft between waypoints
     * print waypoint names next to waypoints
     */
    protected void drawMap() {
        for (Waypoint waypoint : airspaceWaypoints) {
            waypoint.draw();
        }
        
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
     * Draw the info of a selected aircraft in the scene GUI
     */
    private void drawAircraftInfo() {
        graphics.setColour(0, 128, 0);
        graphics.rectangle(false, AIRCRAFT_INFO_X, AIRCRAFT_INFO_Y, AIRCRAFT_INFO_W, AIRCRAFT_INFO_H);
        if (selectedAircraft != null) {
            graphics.setViewport(AIRCRAFT_INFO_X, AIRCRAFT_INFO_Y, AIRCRAFT_INFO_W, AIRCRAFT_INFO_H);
            graphics.printCentred(selectedAircraft.name(), 0, 5, 2, AIRCRAFT_INFO_W);
            
            // Altitude
            String altitude = String.format("%.0f", selectedAircraft.position().z()) + "+";
            graphics.print("Altitude:", 10, 40);
            graphics.print(altitude, AIRCRAFT_INFO_W - 10 - altitude.length()*8, 40);
            
            // Speed
            String speed = String.format("%.2f", selectedAircraft.speed() * 1.687810) + "=";
            graphics.print("Speed:", 10, 55);
            graphics.print(speed, AIRCRAFT_INFO_W - 10 - speed.length()*8, 55);
            
            // Origin
            graphics.print("Origin:", 10, 70);
            graphics.print(selectedAircraft.originName(),
            		AIRCRAFT_INFO_W - 10 - selectedAircraft.originName().length()*8, 70);
            
            // Destination
            graphics.print("Destination:", 10, 85);
            graphics.print(selectedAircraft.destinationName(),
            		AIRCRAFT_INFO_W - 10 - selectedAircraft.destinationName().length()*8, 85);
            
            // Score #goahardorgoahome
            String score = String.format("`%d", selectedAircraft.score());
            graphics.print("Value:", 10, 100);
            graphics.print(score, AIRCRAFT_INFO_W - 10 - score.length()*8, 100);
            
            graphics.setViewport();
        }
    }
   
    /**
     * Draw a readout of the time the game has been played for,
     * aircraft in the sky, etc.
     */
    private void drawScore() {
        int hours = (int)(timeElapsed / (60 * 60));
        int minutes = (int)(timeElapsed / 60);
        minutes %= 60;
        double seconds = timeElapsed % 60;
        java.text.DecimalFormat df = new java.text.DecimalFormat("00.00");
        String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds);
        graphics.print(timePlayed, window.width() - (timePlayed.length() * 8 + 32), 0);
        graphics.print(String.valueOf(aircraftInAirspace.size())
        		+ " aircraft in the airspace.", 32, 0);
        //graphics.print("Control Altitude: " + String.valueOf(controlAltitude) + "+", 544, 0);
       
        // GOA CODE FOLLOWS
        
        // Write total score to string for printing
        String earnings = String.format("`%d earned for family", totalScore);
        
        // Print previous string in bottom centre of display
        graphics.print(earnings, ((window.width()/2)-((earnings.length()*8)/2)),
        		ORDERSBOX_Y - 20);
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
                
                if (a.isMouseOver(x-16, y-16)) {
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

    @Override
    public void mouseReleased(int key, int x, int y) {
    	if (key == input.MOUSE_LEFT && selectedAircraft != null
    			&& selectedWaypoint != null) {
    		for (Waypoint w : airspaceWaypoints) {
    			// Only allow route edit if waypoint isn't an
    			// entry or exit point, and when aircraft is in its
    			// 'normal' state
    			if (selectedAircraft.status() == AirportState.NORMAL) {
    				if ((w.type() == Waypoint.WaypointType.AIRSPACE) && w.isMouseOver(x-16, y-16)) {
    					selectedAircraft.alterPath(selectedPathpoint, w);
    					ordersBox.addOrder(">>> " + selectedAircraft.name()
    							+ " please alter your course");
    					ordersBox.addOrder("<<< Roger that. Altering course now.");
    					selectedPathpoint = -1;
    					selectedWaypoint = null;
    				} else {
    					selectedWaypoint = null;
    				}
    			}
    		}
    	}

    	/*if (key == input.MOUSE_WHEEL_UP && controlAltitude < 30000) {
        	controlAltitude += 2000;
        }
        
        if (key == input.MOUSE_WHEEL_DOWN && controlAltitude > 28000) {
        	controlAltitude -= 2000;
        }*/
        
        // Change altitude when altitude buttons pressed
        AltitudeState altitudeState = AltitudeState.LEVEL;
        if (selectedAircraft != null) {
            altitudeState = selectedAircraft.altitudeState();
        }
        
        altimeter.mouseReleased(key, x, y);
        
        if (selectedAircraft != null) {
            if ((altitudeState != selectedAircraft.altitudeState())
            		&& (selectedAircraft.status() == AirportState.NORMAL)) {
                ordersBox.addOrder(">>> " + selectedAircraft.name() + ", please adjust your altitude");
                ordersBox.addOrder("<<< Roger that. Altering altitude now.");
            }
        }

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
        		}
        	}
        }
        compassDragged = false;
    }

    @Override
    public void keyPressed(int key) {}

    /**
     * Handle keyboard input
     */
    @Override
    public void keyReleased(int key) {
        switch (key) {
            /*case input.KEY_LCRTL:
				generateFlight();
                break;*/
            case input.KEY_ESCAPE:
            	// The escape key returns the user to the main menu
                main.closeScene();
                break;
            /*case input.KEY_F5:
                Aircraft a1 = createAircraft();
                Aircraft a2 = createAircraft();
                gameOver(a1, a2, totalScore);
                break;*/
            case input.KEY_UP:
            	// Up key causes the aircraft to climb
            	// But only if the aircraft is not waiting/landing/taking off
            	// And only if the aircraft is 
            	if ((selectedAircraft != null)
            			&& (selectedAircraft.status() == AirportState.NORMAL)
            			&& (selectedAircraft.position().z() < 30000)) {
            		selectedAircraft.setAltitudeState(AltitudeState.CLIMBING);
            	}
            	break;
            case input.KEY_DOWN:
            	// Down key causes the aircraft to fall
            	// But only if the aircraft is not waiting/landing/taking off
            	// And only if the aircraft is 
            	if ((selectedAircraft != null)
            			&& (selectedAircraft.status() == AirportState.NORMAL)
            			&& (selectedAircraft.position().z() > 28000)) {
            		selectedAircraft.setAltitudeState(AltitudeState.FALLING);
            	}
            	break;
            case input.KEY_T:
            	if ((selectedAircraft != null)
            			&& (selectedAircraft.status() == AirportState.PARKED)) {
            		// Find the parking bay the aircraft was in, and clear it
            		Airport curAirport = selectedAircraft.airport();
            		for (int i = 0; i < curAirport.parkingPoints().length; i++) {
            			if (selectedAircraft.isAt(curAirport.parkingPoints()[i]
            					.position())) {
            				curAirport.clearBay(i);
            			}
            		}

            		if (!selectedAircraft.airport().runwayStatus(1)) {
            			selectedAircraft.setStatus(AirportState.TAKEOFF);
            			selectedAircraft.resetScore();
            			
            			ordersBox.addOrder(">>> " + selectedAircraft.name()
            					+ " You are cleared to takeoff. Please proceed to the runway.");
            			ordersBox.addOrder("<<< Roger that.");
            		} else {
        				ordersBox.addOrder(">>> " + selectedAircraft.name()
                        		+ " Please remain in your bay. The runway is currently busy.");
            			ordersBox.addOrder("<<< Roger that.");
            		}
            	}
            	break;
            case input.KEY_L:
            	if ((selectedAircraft != null)
            			&& (selectedAircraft.status() == AirportState.WAITING)) {
            		if (selectedAircraft.airport().capacity() > 0) {
            			if (!selectedAircraft.airport().runwayStatus(0)) {
            				selectedAircraft.setStatus(AirportState.LANDING);
            				selectedAircraft.setTurnSpeed(Math.PI);
            				selectedAircraft.airport().activateRunway(0);
            				selectedAircraft.airport().decrementCapacity();
            				
            				ordersBox.addOrder(">>> " + selectedAircraft.name()
            						+ " You are cleared to land. Please proceed.");
                			ordersBox.addOrder("<<< Roger that.");
            			} else {
            				ordersBox.addOrder(">>> " + selectedAircraft.name()
                            		+ " Please remain in the stack. The runway is currently busy.");
                			ordersBox.addOrder("<<< Roger that. Will continue going round in circles.");
            			}
            		} else {
            			ordersBox.addOrder(">>> " + selectedAircraft.name()
                        		+ " Please remain in the stack. The airport is currently full.");
            			ordersBox.addOrder("<<< Roger that. Will continue going round in circles.");
            		}
            	}
            	break;
        }
    }
    
	/**
     * Cause all aircraft in airspace to update collisions
     * Catch and handle a resultant game over state
     * @param dt delta time since last collision check
     */
    private void checkCollisions(double dt) {
        for (Aircraft aircraft : aircraftInAirspace) {
            int collisionState = aircraft.updateCollisions(dt, aircraftList());
            if (collisionState >= 0) {
                gameOver(aircraft, aircraftList().get(collisionState), totalScore);
                return;
            }
        }
    }
    
    /**
     * Causes an aircraft to call methods to handle deselection
     */
    private void deselectAircraft() {
    	if (selectedAircraft != null && selectedAircraft
    			.isManuallyControlled()) {
    		selectedAircraft.setManuallyControlled(false);
    	}

        selectedAircraft = null;
        selectedWaypoint = null;
        selectedPathpoint = -1;
        
        if (!Main.testing){ 
        	altimeter.hide();
        }
    }
   
    @Override
    public void playSound(audio.Sound sound) {
    	if (!Main.testing) {
    		sound.stop();
        	sound.play();
    	}
    }
    
    /**
     * Handle a game over caused by reaching the max number of aircraft.
     * The player has successfully won
     * @param score the score the player achieved
     */
    public void gameWin(int score) {
    	main.closeScene();
    	main.setScene(new Shop(main, score));
    }

    /**
     * Handle a game over caused by two aircraft colliding.
     * Create a gameOver scene and make it the current scene.
     * @param aircraft1 the first aircraft involved in the collision
     * @param aircraft2 the second aircraft in the collision
     */
    public void gameOver(Aircraft aircraft1, Aircraft aircraft2, int score) {
    	if (!Main.testing) {
    		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
    	}
    	
    	main.closeScene();
    	main.setScene(new GameOver(main, aircraft1, aircraft2, score));
    }
   
    /**
     * Create a new aircraft object and introduce it to the airspace
     */
    private void generateFlight() {
        Aircraft a = createAircraft();
        if (!Main.testing) ordersBox.addOrder(
        		"<<< " + a.name() + " incoming from "+ a.originName()
        		+ " heading towards " + a.destinationName() + ".");
        aircraftInAirspace.add(a);
    }
   
    /**
     * Handles aircraft creation
     * including randomisation of entry, exit, altitude, etc.
     * @return the created aircraft object
     */
    private Aircraft createAircraft() {
    	int d, o;
    	String originName;
    	Waypoint originPoint;
    	String destinationName;
    	Waypoint destinationPoint;
        
        // Reduce probability that an aircraft will be heading to an airport
        // based on how full the airport is
        double p = 0;
        double ratio = 0;
    	
        // Get the percentage capacity used at the airport
        if (airports[0].initialCapacity() > 0) {
        	ratio = (airports[0].capacity() / airports[0].initialCapacity());
        }
        
        // Use the percentage capacity to determine the probability
        // that a flight will be heading to the airport
        if (ratio == 0) {
        	p = 0.9;
        } else if (ratio <= 0.25) {
        	p = 0.75;
        } else if (ratio <= 0.5) {
        	p = 0.5;
        } else if (ratio <= 0.75) {
        	p = 0.25;
        } else if (ratio <= 1) {
        	p = 0.1;
        }
        
        // However, if 
        
    	// Destination is an airport with probability p
    	if (Math.random() < p) {
    		do {
    			// Random used to determine the origin point
    			o = (new Random()).nextInt(flightEntryPoints.length);

    			// Random used to determine the destination point
    			d = (new Random()).nextInt(flightExitPoints.length);
    		} while (d == o);

    		originName = FLIGHT_ENTRY_POINT_NAMES[o];
    		originPoint = flightEntryPoints[o];
    		destinationName = FLIGHT_EXIT_POINT_NAMES[d];
    		destinationPoint = flightExitPoints[d];
    		
    		return new Aircraft(destinationName, originName,
            		destinationPoint, originPoint, aircraftImage,
            		32 + (int)(10 * Math.random()), airspaceWaypoints,
            		null);
    	} else {
    		// Random used to determine the origin point
			o = (new Random()).nextInt(flightEntryPoints.length);
			// Random used to determine the destination airport
			d = (new Random()).nextInt(airports.length);
			
			originName = FLIGHT_ENTRY_POINT_NAMES[o];
    		originPoint = flightEntryPoints[o];				
    		destinationPoint = airports[d].getPosition(originPoint.position());
    		destinationName = airports[d].name();
    		
    		return new Aircraft(destinationName, originName,
            		destinationPoint, originPoint, aircraftImage,
            		32 + (int)(10 * Math.random()), airspaceWaypoints,
            		airports[d]);
    	}
    }
   
    /**
     * Decide which aircraft are selectable at the current control altitude
     * Aircraft must be on the current control altitude, or changing altitude towards it
     * @param a an aircraft to be checked for selectability
     * @param altitude the current control altitude
     * @return whether or not the aircraft is selectable at the current control altitude
     */
    /*private boolean aircraftSelectableAtAltitude(Aircraft a, int altitude) {
    	return true;
        if (a.position().z() == altitude) return true;
        if (a.position().z() < altitude && a.altitudeState() == Aircraft.ALTITUDE_CLIMB) return true;
        if (a.position().z() > altitude && a.altitudeState() == Aircraft.ALTITUDE_FALL) return true;
        return false;
    }*/
   
    /**
     * Cleanly exit by stopping the scene's music
     */
    @Override
    public void close() {
    	if (!Main.testing) {
    		music.stop();
    	}
    }
	
}
