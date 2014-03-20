package cls;

import java.util.ArrayList;
import java.util.Random;

import btc.Main;

import scn.Game;

import cls.Aircraft.AirportState;
import cls.Aircraft.AltitudeState;

/**
 * <h1>Airport</h1>
 * <p>
 * Represents an airport. Stores aircraft, launches aircraft.
 * </p>
 */
public class Airport {

	/** The airport's name */
	private String name;
	
	/** The airport's initial capacity - the number of aircraft it
	 * could initially accommodate */
	private int initialCapacity;

	/** The airport's capacity - the number of aircraft it can accommodate */
	private int capacity;

	/** The names to display for the entry points */
	private String[] entryPointNames;

	/** The points through which an aircraft should curve to reach the runway */
	private Waypoint[] entryPoints;

	/** The waypoints which make up the landing runway */
	private Waypoint[] landingPoints;

	/** The waypoints at which an aircraft can park */
	private Waypoint[] parkingPoints;

	/** The waypoints which make up the takeoff runway */
	private Waypoint[] takeoffPoints;

	/** The length of the landing runway */
	private double landingDist;

	/** The length of the takeoff runway */
	private double takeoffDist;

	/** Aircraft currently at the airport */
	private ArrayList<Aircraft> aircraft;

	/** Aircraft currently leaving the airport */
	private ArrayList<Aircraft> aircraftLeaving;
	
	/** The speed at which aircraft travel whilst at the airport */
	private int aircraftSpeed;

	/** List holding the status (true being full, false being empty)
	 * of the parking bays */
	private boolean[] parkingBays;
	
	/** List holding the status (true being active, false being inactive)
	 * of the runways */
	private boolean[] runways;

	/**
	 * Constructor for airport.
	 * @param name the airport's name
	 * @param capacity the number of aircraft the airport can take
	 * @param entryPointNames the names to display for the entry points
	 * @param entryPoints the points through which an aircraft should curve
	 * 						to reach the runway
	 * @param landingPoints the points an aircraft should follow when landing
	 * @param parkingPoints the points at which an aircraft can park
	 * @param takeoffPoints the points an aircraft should follow when taking off
	 */
	public Airport(String name, int capacity, String[] entryPointNames,
			Waypoint[] entryPoints, Waypoint[] landingPoints,
			Waypoint[] parkingPoints, Waypoint[] takeoffPoints) {
		this.name = name;
		this.initialCapacity = Math.min(capacity, entryPoints.length);
		this.capacity = initialCapacity;
		this.entryPointNames = entryPointNames;
		this.entryPoints = entryPoints;
		this.landingPoints = landingPoints;
		this.parkingPoints = parkingPoints;
		this.takeoffPoints = takeoffPoints;

		// Get the length of the landing runway
		landingDist = (new Vector(
				(landingPoints[1].position().x() - landingPoints[0].position().x()),
				(landingPoints[1].position().y() - landingPoints[0].position().y()),
				0)).magnitude();

		// Get the length of the takeoff runway
		takeoffDist = (new Vector(
				(takeoffPoints[takeoffPoints.length - 2].position().x()
						- takeoffPoints[takeoffPoints.length - 1].position().x()),
						(takeoffPoints[takeoffPoints.length - 2].position().y()
								- takeoffPoints[takeoffPoints.length - 1].position().y()),
								0)).magnitude();

		aircraft = new ArrayList<Aircraft>();
		aircraftLeaving = new ArrayList<Aircraft>();

		parkingBays = new boolean[this.landingPoints.length];

		// Clear the parking bays
		for (int i = 0; i < parkingBays.length; i++) {
			parkingBays[i] = false;
		}
		
		// Set up and clear the runways
		runways = new boolean[] {false, false};
		
		// Set the speed aircraft should move at whilst at the airport
		aircraftSpeed = 25;
	}

	/**
	 * Gets the airport's name.
	 * @return the airport's name
	 */
	public String name() {
		return name;
	}
	
	/**
	 * Gets the airport's initial capacity.
	 * <p>
	 * Note: this is not necessarily equal to the number of
	 * parking bays - it is possible to limit an airport to only
	 * use a subset of the available parking bays.
	 * </p>
	 * @return the airport's initial capacity
	 */
	public int initialCapacity() {
		return initialCapacity;
	}

	/**
	 * Gets the airports current capacity.
	 * <p>
	 * This decreases when an aircraft starts to land, and increases
	 * once an aircraft has cleared the takeoff runway.
	 * </p>
	 * @return the airport's current capacity
	 */
	public int capacity() {
		return capacity;
	}
	
	/**
	 * Decrements the airport's capacity.
	 * <p>
	 * This results in one fewer airraft being able to land.
	 * </p>
	 */
	public void decrementCapacity() {
		if (capacity > 0) {
			capacity--;
		}
	}

	/**
	 * Gets a list of the names to display above the first and last entry points.
	 * @return a list of the names to display above entry points
	 */
	public String[] entryPointNames() {
		return entryPointNames;
	}

	/**
	 * Gets a list of waypoints used whilst aircraft are in the stack.
	 * @return a list of the airport's entry waypoints
	 */
	public Waypoint[] entryPoints() {
		return entryPoints;
	}

	/**
	 * Gets a list of waypoints used whilst an aircraft is landing.
	 * <p>
	 * These points guide an aircraft whilst it is taxiing to a parking bay.
	 * </p>
	 * @return a list of the airport's landing waypoints
	 */
	public Waypoint[] landingPoints() {
		return landingPoints;
	}

	/**
	 * Gets a list of possible waypoints at which an aircraft could park.
	 * @return a list of the airport's parking waypoints
	 */
	public Waypoint[] parkingPoints() {
		return parkingPoints;
	}

	/**
	 * Gets a list of waypoints used whilst an aircraft is taking off.
	 * <p>
	 * These points guide an aircraft whilst it is taxiing to the
	 * takeoff runway.
	 * </p>
	 * @return a list of the airport's takeoff waypoints
	 */
	public Waypoint[] takeoffPoints() {
		return takeoffPoints;
	}

	/**
	 * Clears the specified parking bay.
	 * <p>
	 * Use this to indicate when a parking bay has been freed,
	 * for instance when an aircraft leaves to head to the runway.
	 * </p>
	 * @param bayToClear the index of the bay to clear
	 */
	public void clearBay(int bayToClear) {
		parkingBays[bayToClear] = false;
	}
	
	/**
	 * Gets the status of the specified runway.
	 * @param runway the index of the runway to return the status of
	 * 					<ul><li>0: the landing runway</li>
	 * 						<li>1: the takeoff runway</li>
	 * 					</ul>
	 * @return <code>true</code> if the runway is busy,
	 * 			<code>false</code> if the runway is clear
	 */
	public boolean runwayStatus(int runway) {
		return this.runways[runway];
	}
	
	/**
	 * Causes the specified runway to be marked as active.
	 * @param runway the index of the runway to return the status of
	 * 					<ul><li>0: the landing runway</li>
	 * 						<li>1: the takeoff runway</li>
	 * 					</ul>
	 */
	public void activateRunway(int runway) {
		this.runways[runway] = true;
	}
	
	/**
	 * Gets a list of aircraft in the airport.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * Otherwise, this list should only be manipulated within the airport
	 * class.
	 * </p>
	 * @return a list of aircraft at the airport
	 */
	public ArrayList<Aircraft> aircraft() {
		return aircraft;
	}
	
	/**
	 * Gets the aircraft about to be deleted.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * @return the aircraft about to be removed
	 */
	public ArrayList<Aircraft> aircraftLeaving() {
		return aircraftLeaving;
	}

	/**
	 * Gets the first waypoint to pass through to reach the airport.
	 * 
	 * Gets the appropriate waypoint for a flight to pass through in order to reach the
	 * runway. If the position supplied is left of the first landing point, the first
	 * entry point (the one on the left)  will be returned, else the last entry point (the
	 * one on the right) will be returned.
	 * @param position the position of the incoming aircraft
	 * @return the waypoint the flight should pass through to reach the runway
	 */
	public Waypoint getPosition(Vector position) {
		if (position.x() < landingPoints[0].position().x()) {
			return entryPoints[0];
		} else {
			return entryPoints[entryPoints.length - 1];
		}
	}

	/**
	 * Adds an aircraft to the airport.
	 * 
	 * After an aircraft is added to the airport, the airport will take control over its
	 * movement and state.
	 * @param aircraft the aircraft to add
	 */
	public void addAircraft(Aircraft aircraft) {
		this.aircraft.add(aircraft);

		aircraft.setStatus(Aircraft.AirportState.WAITING);
	}

	/**
	 * Updates the aircraft currently at the airport.
	 * <p>
	 * This fires actions when an aircraft reaches its target
	 * waypoint, and handles ascent/descent.
	 * </p>
	 * @param dt
	 */
	public void update(double dt) {
		// Loop through each aircraft at the airport
		for (Aircraft airc : aircraft) {
			// Check if the aircraft is at its next target
			if (airc.isAt(airc.getCurrentTarget())) {
				if (airc.status() == AirportState.WAITING) {
					updateWaitingAircraft(airc);
				} else if (airc.status() == AirportState.LANDING) {
					updateLandingAircraft(airc);
				} else if (airc.status() == AirportState.TAKEOFF) {
					updateTakeoffAircraft(airc);
				}
			}

			// Handle ascent/descent
			if ((airc.status() == AirportState.LANDING)
					&& (airc.altitudeState() == AltitudeState.FALLING)) {
				causeDescent(airc);
			} else if ((airc.status() == AirportState.TAKEOFF)
					&& (airc.altitudeState() == AltitudeState.CLIMBING)) {
				causeAscent(airc);
			}
		}

		removeLeavingAircraft();
	}
	
	/**
	 * Updates an aircraft which is waiting.
	 * <p>
	 * Checks to find which entry point the aircraft is at,
	 * and then sets its current target to the next waypoint in the
	 * entry point list.
	 * </p>
	 * @param airc the aircraft to update
	 */
	private void updateWaitingAircraft(Aircraft airc) {
		// If the aircraft is currently waiting, go to the next entry point
		for (int i = 0; i < entryPoints.length; i++) {
			// Loop through entry points to find where the aircraft is currently
			if (airc.getCurrentTarget().equals(entryPoints[i].position())) {
				// Set the current target to the next entry waypoint
				// If aircraft is at the last point in the list,
				// loop back to the start
				airc.setCurrentTarget(
						entryPoints[(i + 1) % entryPoints.length].position());
				break;
			}
		}
	}
	
	/**
	 * Updates an aircraft which is waiting.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * <b>Otherwise, this method should not be called from
	 * outside the Airport class</b>
	 * </p>
	 * @param testing is the system currently being tested
	 */
	public void updateWaitingAircraft(Aircraft airc, boolean testing) {
		// Only allow this method to run during testing
		if (Main.testing && testing) {
			updateWaitingAircraft(airc);
		}
	}
	
	/**
	 * Updates an aircraft which is landing.
	 * <p>
	 * This handles three cases:
	 * </p>
	 * <p>
	 * Case 1 : If the aircraft has finished taxiing through the landing points
	 * and is now at a parking point, the aircraft's status is set to parked
	 * and its target remains the same.
	 * </p>
	 * <p>
	 * Case 2 : If the aircraft has started to progress down the runway, 
	 * it is forced to start to descend, and its original height 
	 * (which is required for the descent code) is stored in the z 
	 * position of its current target - NB: this is a hack). If the 
	 * aircraft is taxiing, its target is set to the next landing point.
	 * </p>
	 * <p>
	 * Case 3 : If the aircraft has not yet reached a landing point (i.e.
	 * is still in the waiting loop, then provided it has reached the second
	 * waiting point cause it to proceed to the first landing point.
	 * Otherwise, the aircraft's current target is updated to the next waypoint
	 * in the entry point list (and will continue to proceed around the loop
	 * until it reaches the first landing point. Also, warning circles are draw
	 * to ensure it is obvious to the user that aircraft can still collide in
	 * this state.
	 * </p>
	 * @param airc the aircraft to update
	 */
	private void updateLandingAircraft(Aircraft airc) {
		// Check if aircraft is in a parking bay
		for (int k = 0; k < parkingPoints.length; k++) {
			// Loop through entry points to find where the aircraft is currently
			if (airc.getCurrentTarget().equals(parkingPoints[k].position())) {
				airc.setStatus(AirportState.PARKED);
				break;
			}
		}

		// Otherwise point aircraft to the next landing point
		for (int j = 0; j < landingPoints.length; j++) {
			// Loop through landing points to find where the aircraft is currently
			if (airc.getCurrentTarget().equals(landingPoints[j].position())) {
				if (j == 0) {
					// If aircraft at first landing point, start descending
					airc.setAltitudeState(AltitudeState.FALLING);

					airc.setCurrentTarget(new Vector(
							landingPoints[(j + 1) % landingPoints.length].position().x(),
							landingPoints[(j + 1) % landingPoints.length].position().y(),
							airc.position().z()));
				} else if (j == 2) {
					// Aircraft now off the runway, so deactivate it
					runways[0] = false;
					
					airc.setCurrentTarget(
							landingPoints[(j + 1) % landingPoints.length].position());
				} else if (j == (landingPoints.length - 1)) {
					// If aircraft is at the last landing point, allocate a parking bay
					// Loop through parking bays, and allocate the first empty bay
					for (int b = 0; b < parkingBays.length; b++) {
						if (!parkingBays[b]) {
							airc.setCurrentTarget(
									parkingPoints[b].position());
							parkingBays[b] = true;
							break;
						}
					}
				} else {
					airc.setCurrentTarget(
							landingPoints[(j + 1) % landingPoints.length]
									.position());
				}

				break;
			}
		}

		// Check if aircraft still looping
		// If it is, point it to the first landing point
		for (int i = 0; i < entryPoints.length; i++) {
			// Show warning circles
			airc.drawWarningCircles();
			// Loop through entry points to find where the aircraft is currently
			if (airc.getCurrentTarget().equals(entryPoints[i].position())) {
				if (i == 1) {
					// If at the mid entry point, go to runway
					airc.setCurrentTarget(landingPoints[0].position());
				} else {
					// Otherwise, keep looping
					airc.setCurrentTarget(
							entryPoints[(i + 1) % entryPoints.length].position());
				}

				break;
			}
		}
	}
	
	/**
	 * Updates an aircraft which is landing.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * <b>Otherwise, this method should not be called from
	 * outside the Airport class</b>
	 * </p>
	 * @param testing is the system currently being tested
	 */
	public void updateLandingAircraft(Aircraft airc, boolean testing) {
		// Only allow this method to run during testing
		if (Main.testing && testing) {
			updateLandingAircraft(airc);
		}
	}
	
	/**
	 * Updates an aircraft which is taking off.
	 * <p>
	 * This handles four cases:
	 * </p>
	 * <p>
	 * Case 1 : If the aircraft is at the last takeoff point
	 * i.e. has taken off), reset its attributes, and generate
	 * a path to an exit point. Also deactivate the takeoff runway
	 * (as its no longer in use).
	 * </p>
	 * <p>
	 * Case 2 : If the aircraft has reached the start of the runway,
	 * cause it to climb.
	 * </p>
	 * <p>
	 * Case 3 : If the aircraft is at any other landing point,
	 * set its current target to the next takeoff point in the list.
	 * </p>
	 * <p>
	 * Case 4 : If the aircraft is still in a parking bay, set its
	 * current target to the first takeoff point. Also activate
	 * the takeoff runway (as its about to be in use).
	 * </p>
	 * @param airc the aircraft to update
	 */
	private void updateTakeoffAircraft(Aircraft airc) {
		// Point aircraft to the next takeoff point
		for (int j = 0; j < takeoffPoints.length; j++) {
			// Loop through takeoff points to find where the aircraft is currently
			if (airc.getCurrentTarget().equals(takeoffPoints[j].position())) {
				if (j == (takeoffPoints.length - 1)) {
					// If aircraft is at the last takeoff point,
					// it has left the airport
					
					// Deactivate the takeoff runway
					runways[1] = false;

					// Reset aircraft to new route
					airc.clearAirport();

					int d = (new Random()).nextInt(Game.flightEntryPoints.length);
					Waypoint currentPos = new Waypoint(
							airc.position().x(), airc.position().y(), Waypoint.WaypointType.ENTRY);

					airc.setOriginName(name);
					
					airc.setDestinationName(Game.FLIGHT_EXIT_POINT_NAMES[d]);
					airc.setRoute(airc.findGreedyRoute(currentPos,
							Game.flightExitPoints[d], Game.airspaceWaypoints));
					airc.setDestination(Game.flightExitPoints[d]);
					airc.setCurrentTarget(airc.getRoute()[0].position());
					airc.setManuallyControlled(false);
					airc.clearCurrentRouteStage();
					airc.clearManualBearingTarget();
					airc.applyDifficultySettings(false);

					airc.setStatus(AirportState.NORMAL);

					// Remove aircraft from airport
					aircraftLeaving.add(airc);

					capacity++;
				} else if (j == (takeoffPoints.length - 2)) {
					// If aircraft is at the second-to-last landing point,
					// start ascending
					airc.setAltitudeState(AltitudeState.CLIMBING);
					
					airc.setCurrentTarget(
							takeoffPoints[(j + 1) % takeoffPoints.length]
									.position());
				} else {
					// Otherwise just proceed to the next takeoff point
					airc.setCurrentTarget(
							takeoffPoints[(j + 1) % takeoffPoints.length]
									.position());
				}

				break;
			}
		}

		// Check if aircraft still in a parking bay
		// If it is, point it to the first takeoff point
		for (int i = 0; i < parkingPoints.length; i++) {
			// Loop through parking points to find where the aircraft is currently
			if (airc.isAt(parkingPoints[i].position())) {
				airc.setCurrentTarget(takeoffPoints[0].position());
				
				// Activate the takeoff runway
				runways[1] = true;
				break;
			}
		}
	}
	
	/**
	 * Updates an aircraft which is taking off.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * <b>Otherwise, this method should not be called from
	 * outside the Airport class</b>
	 * </p>
	 * @param testing is the system currently being tested
	 */
	public void updateTakeoffAircraft(Aircraft airc, boolean testing) {
		// Only allow this method to run during testing
		if (Main.testing && testing) {
			updateTakeoffAircraft(airc);
		}
	}
	
	/**
	 * Controls an aircraft's descent.
	 * <p>
	 * If the aircraft is at ground level (0ft), set its
	 * altitude state to level (so it doesn't continue to fall).
	 * </p>
	 * <p>
	 * Otherwise, get the distance the aircraft has travelled from
	 * the start of the runway, and generate a ratio between this and
	 * the landing point (which is a fraction of the length of the runway).
	 * Then use this ratio to reduce the aircraft's altitude linearly,
	 * and its speed quartically.
	 * </p>
	 * @param airc the aircraft to cause to ascend
	 */
	private void causeDescent(Aircraft airc) {
		// If at ground level, stop descending and reduce speed
		if (airc.position().z() <= 0) {
			airc.setAltitudeState(AltitudeState.LEVEL);
			airc.setAltitude(0);
			airc.setCurrentTarget(new Vector(
					airc.getCurrentTarget().x(),
					airc.getCurrentTarget().y(),
					airc.position().z()));
		} else {
			// Get remaining distance to the chosen landing point
			double distCovered = (new Vector(
					(airc.position().x() - landingPoints[0].position().x()),
					(airc.position().y() - landingPoints[0].position().y()),
					0)).magnitude();

			// Get the ratio between the total distance and the distance covered
			// Land three-quarters of the way down the runway
			double ratio = distCovered / ((3d / 4d) * landingDist);

			airc.setAltitude(Math.max(airc
					.getCurrentTarget().z() * (1 - ratio), 0));
			
			airc.setVelocity(airc.getVelocity().normalise().scaleBy(
					aircraftSpeed + (Math.pow(1 - ratio, 4)
							* (airc.initialSpeed() - aircraftSpeed))));
		}
	}
	
	/**
	 * Controls an aircraft's descent.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * <b>Otherwise, this method should not be called from
	 * outside the Airport class</b>
	 * </p>
	 * @param testing is the system currently being tested
	 */
	public void causeDescent(Aircraft airc, boolean testing) {
		// Only allow this method to run during testing
		if (Main.testing && testing) {
			causeDescent(airc);
		}
	}
	
	/**
	 * Controls an aircraft's ascent.
	 * <p>
	 * If the aircraft is at the top flight level (30000ft), set its
	 * altitude state to level (so it doesn't continue to climb).
	 * </p>
	 * <p>
	 * Otherwise, get the distance the aircraft has travelled from
	 * the start of the runway, and generate a ratio between this and
	 * the end of the runway. Then use this ratio to increase the aircraft's
	 * altitude linearly, and its speed quadratically.
	 * </p>
	 * @param airc the aircraft to cause to descend
	 */
	private void causeAscent(Aircraft airc) {
		// If at flight level, stop ascending
		if (airc.position().z() >= 30000) {
			airc.setAltitudeState(AltitudeState.LEVEL);
			airc.setAltitude(30000);
		} else {
			// Get remaining distance to the end of the runway
			double distCovered = (new Vector(
					(airc.position().x() - takeoffPoints[takeoffPoints.length - 2]
							.position().x()),
							(airc.position().y() - takeoffPoints[takeoffPoints.length - 2]
									.position().y()),
									0)).magnitude();

			// Get the ratio between the total distance and the distance covered
			double ratio = distCovered / takeoffDist;

			if (distCovered > (takeoffDist * 1d/5d)) {
				airc.setAltitude(Math.min(30000 * ratio, 30000));
			}
			
			airc.setVelocity(airc.getVelocity().normalise().scaleBy(
					aircraftSpeed + (Math.pow(ratio, 2)
							* (airc.initialSpeed() - aircraftSpeed))));
		}
	}
	
	/**
	 * Controls an aircraft's ascent.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * <b>Otherwise, this method should not be called from
	 * outside the Airport class</b>
	 * </p>
	 * @param testing is the system currently being tested
	 */
	public void causeAscent(Aircraft airc, boolean testing) {
		// Only allow this method to run during testing
		if (Main.testing && testing) {
			causeAscent(airc);
		}
	}
	
	/**
	 * Removes aircraft which are on the list of leaving aircraft.
	 */
	private void removeLeavingAircraft() {
		for (Aircraft aircToRemove : aircraftLeaving) {
			if (aircraft.contains(aircToRemove)) {
				aircraft.remove(aircToRemove);
			}
		}
	}
	
	/**
	 * Removes aircraft which are on the list of leaving aircraft.
	 * <p>
	 * <b>This should be used for testing purposes ONLY</b>
	 * </p>
	 * <p>
	 * <b>Otherwise, this method should not be called from
	 * outside the Airport class</b>
	 * </p>
	 * @param testing is the system currently being tested
	 */
	public void removeLeavingAircraft(boolean testing) {
		// Only allow this method to run during testing
		if (Main.testing && testing) {
			removeLeavingAircraft();
		}
	}
	
}

