package cls;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import btc.Main;

import scn.Game;

import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.window;

/**
 * <h1>Aircraft</h1>
 * <p>
 * Represents an in-game aircraft. Calculates velocity, route-following, etc.
 * </p>
 */
public class Aircraft {

	/** The physical size of the aircraft in pixels. This determines crashes */
	public final static int RADIUS = 16;
	
	/** How far away (in pixels) the mouse can be from the aircraft but
	 * still select it */
	public final static int MOUSE_LENIENCY = 16; 
	
	/** How large to draw the bearing circle. */
	public final static int COMPASS_RADIUS = 64;
	
	/** How far away another aircraft has to be to not cause a separation
	 * violation */
	public static int separationRule = 64;
	
	/** The score a aircraft will reward the player with upon
	 * landing/clearing the airspace */
	private int score;
	
	/** How much the aircraft can turn per second, in radians */
	private double turnSpeed;
	
	/** How far off course the aircraft must be for its bearing to be corrected */
	private double bearingLeniency;
	
	/** The position of the aircraft */
	private Vector position;
	
	/** The initial speed of the aircraft */
	private double initialSpeed;

	/** The velocity of the aircraft */
	private Vector velocity;
	
	/** Whether the aircraft is being manually controlled */
	private boolean isManuallyControlled;
	
	/** The flight name of the aircraft */
	private String flightName;
	
	/** The position the aircraft is currently flying towards (if not
	 * manually controlled) */
	private Vector currentTarget;
	
	/** The target the player has told the aircraft to fly at when
	 * manually controlled */
	private double manualBearingTarget;
	
	/** The name of the location the aircraft is flying from */
	private String originName;
	
	/** The name of the location the aircraft is flying to */
	private String destinationName;
	
	/** An array of waypoints from the aircraft's origin to its destination */
	private Waypoint[] route;
	
	/** The current stage the aircraft is at in its route */
	private int currentRouteStage;
	
	/** The off-screen point (or airport) the aircraft will end up at before disappearing */
	private Waypoint destination;
	
	/** The target airport (if applicable) */
	private Airport airport;
	
	/** The image to be drawn representing the aircraft */
	private Image image;
	
	/** Whether the aircraft has reached its destination and can be disposed of */
	private AirportState status;
	
	/** The angle the aircraft is currently turning by */
	private double currentlyTurningBy;
	
	/** Holds a list of aircraft currently in violation of
	 * separation rules with this aircraft */
	private ArrayList<Aircraft> aircraftTooNear = new ArrayList<Aircraft>();
	
	/** The current state of the aircraft's altitude,
	 * i.e. if the aircraft is climbing or falling */
	private AltitudeState altitudeState;
	
	/** The speed to climb or fall by */
	private int altitudeChangeSpeed;
	
	/** Altitude states */
	public enum AltitudeState {
		FALLING, LEVEL, CLIMBING
	};
	
	/** Airport states */
	public enum AirportState {
		NORMAL, FINISHED, WAITING, LANDING, TAKEOFF, PARKED
	};
	
	private static ArrayList<String> usedNames = new ArrayList<String>();
	
	/**
	 * Flags whether the collision warning sound has been played before.
	 * If set, aircraft will not play warning again until it the separation 
	 * violation involving it ends.
	 */
	private boolean collisionWarningSoundFlag = false;

	/** A warning sound to be played when the aircraft enters separation violation */
	private final static audio.Sound WARNING_SOUND = Main.testing ? null
			: audio.newSoundEffect("sfx" + File.separator + "beep.ogg");
	
	/**
	 * Constructor for an aircraft.
	 * @param name the name of the flight
	 * @param nameDestination the name of the location to which the aircraft is going
	 * @param nameOrigin the name of the location from which the aircraft hails
	 * @param destinationPoint the end point of the aircraft's route
	 * @param originPoint the point to initialise the aircraft
	 * @param img the image to draw to represent the aircraft
	 * @param speed the speed the aircraft will travel at
	 * @param sceneWaypoints the waypoints on the map
	 * @param airport the airport the aircraft is travelling to
	 */
	public Aircraft(String nameDestination,
			String nameOrigin, Waypoint destinationPoint,
			Waypoint originPoint, graphics.Image img,
			double speed, Waypoint[] sceneWaypoints,
			Airport airport) {
		generateAircraft(nameDestination, nameOrigin,
				destinationPoint, originPoint, img, speed,
				sceneWaypoints, airport, false);
	}

	public Aircraft(String nameDestination,
			String nameOrigin, Waypoint destinationPoint,
			Waypoint originPoint, graphics.Image img,
			double speed, Waypoint[] sceneWaypoints,
			Airport airport, boolean testing) {
		generateAircraft(nameDestination, nameOrigin,
				destinationPoint, originPoint, img, speed,
				sceneWaypoints, airport, testing);
	}
	
	private void generateAircraft(String nameDestination,
			String nameOrigin, Waypoint destinationPoint,
			Waypoint originPoint, graphics.Image img,
			double speed, Waypoint[] sceneWaypoints,
			Airport airport, boolean testing) {
		flightName = generateName();
		destinationName = nameDestination;
		originName = nameOrigin;
		image = img;
		initialSpeed = speed;
		
		// Find route
		this.airport = airport;
		if (!testing) {
			route = findGreedyRoute(originPoint, destinationPoint,
					sceneWaypoints);
		} else {
			route = new Waypoint[] {sceneWaypoints[0]};
		}
		
		destination = destinationPoint;
		
		// Place on spawn waypoint
		position = originPoint.position();
		
		// Offsets the spawn location of the aircraft around
		// the origin waypoint, for variety
		// This also prevents collisions between just-spawned aircraft
		// and existing aircraft flying to the waypoint.
		int offset = 0;

		if (!Main.testing) { 
			//if ((new Random()).nextInt(2) == 0) {
			if (position.x() == 0) {
				// Apply positive offset
				offset = (new Random()).nextInt((separationRule - 10) + 1) + 10;
			} else {
				// Apply negative offset
				offset = (new Random()).nextInt((separationRule - 10) + 1)
						- separationRule;
			}
		}

		int altitudeOffset = 0;

		if (!Main.testing) {
			if ((new Random()).nextInt(2) == 0) {
				altitudeOffset = 28000;
			} else {
				altitudeOffset = 30000;
			}
		}
		
		// Update position
		if (!Main.testing){ 
			position = position.add(new Vector(offset, 0, altitudeOffset));
		}
		
		// Calculate initial velocity (direction)
		currentTarget = route[0].position();
		velocity = new Vector(currentTarget.x() - position.x(),
				currentTarget.y() - position.y(),
				0).normalise().scaleBy(speed);
		
		isManuallyControlled = false;
		status = AirportState.NORMAL;
		currentRouteStage = 0;
		currentlyTurningBy = 0;
		bearingLeniency = 0.03;
		manualBearingTarget = Double.NaN;
		score = 100;
		
		applyDifficultySettings(true);
	}
	
	private String generateName() {
		String name = "";
		boolean nameTaken = true;
		while (nameTaken) {
			name = "Flight " + (int)(900 * Math.random() + 100);
			nameTaken = false;
			for (String used : usedNames) {
				if (name == used) nameTaken = true;
			}
		}
		usedNames.add(name);
		return name;
	}
	
	public void applyDifficultySettings(boolean setVelocity) {
		// Adjust the aircraft's attributes according to the difficulty of the parent scene.
		switch (Game.difficulty()) {
			// 0 has the easiest attributes (slower aircraft, more forgiving separation rules)
			// 2 has the hardest attributes (faster aircraft, least forgiving separation rules).
			case Game.DIFFICULTY_EASY:
				separationRule = 64;
				turnSpeed = Math.PI / (2 * (2 * Main.getScale()));
				altitudeChangeSpeed = (int) (400 / (2 * Main.getScale()));
				break;
			case Game.DIFFICULTY_MEDIUM:
				separationRule = 96;
				if (setVelocity) velocity = velocity.scaleBy(2);
				if (setVelocity) initialSpeed *= 2;
				turnSpeed = Math.PI / (1 * (2 * Main.getScale()));
				altitudeChangeSpeed = (int) (200 / (2 * Main.getScale()));
				break;
			case Game.DIFFICULTY_HARD:
				separationRule = 128;
				if (setVelocity) velocity = velocity.scaleBy(3);
				if (setVelocity) initialSpeed *= 3;
				// At high velocities, the aircraft is allowed to turn faster
				// this helps keep the aircraft on track.
				turnSpeed = Math.PI / (0.5 * (2 * Main.getScale()));
				altitudeChangeSpeed = (int) (100 / (2 * Main.getScale()));
				break;
			default :
				Exception e = new Exception("Invalid Difficulty : "
												+ Game.difficulty() + ".");
				e.printStackTrace();
		}
	}

	/**
	 * Allows access to the aircraft's current position.
	 * @return the aircraft's current position.
	 */
	public Vector position() {
		return position;
	}
	
	/**
	 * Allows access to the aircraft's name.
	 * @return the aircraft's name.
	 */
	public String name() {
		return flightName;
	}
	
	/**
	 * Allows access to the name of the location from which this aircraft hails.
	 * @return the origin's name.
	 */
	public String originName() {
		return originName;
	}
	
	public void setOriginName(String originName) {
		this.originName = originName;
	}
	
	/**
	 * Allows access to the name of the location to which this aircraft travels.
	 * @return the destination's name.
	 */
	public String destinationName() {
		return destinationName;
	}
	
	public void setDestinationName(String newDestinationName) {
		this.destinationName = newDestinationName;
	}
	
	/**
	 * Allows access to whether the aircraft is being manually controlled.
	 * @return true, if the aircraft is currently manually controlled. False, otherwise.
	 */
	public boolean isManuallyControlled() {
		return isManuallyControlled;
	}
	
	public void setManuallyControlled(boolean isManuallyControlled) {
		this.isManuallyControlled = isManuallyControlled;
		if (!isManuallyControlled) resetBearing();
	}
	
	public AltitudeState altitudeState() {
		return altitudeState;
	}
	
	public Vector getCurrentTarget() {
		return currentTarget;
	}
	
	public void setCurrentTarget(Vector newTarget) {
		this.currentTarget = newTarget;
	}
	
	public Waypoint getDestination() {
		return destination;
	}

	public void setDestination(Waypoint w) {
		this.destination = w;
	}
	
	public Waypoint[] getRoute() {
		return this.route;
	}
	
	public void setRoute(Waypoint[] r) {
		this.route = r;
	}
	
	/**
	 * Sets the aircraft's altitude state, e.g. climbing or falling
	 * @param altitudeState the altitude state to set
	 */
	public void setAltitudeState(AltitudeState altitudeState){
		this.altitudeState = altitudeState;
	}

	public void setTurnSpeed(double turnSpeed) {
		this.turnSpeed = turnSpeed;
	}
	
	public double initialSpeed() {
		return initialSpeed;
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
	
	/**
	 * Gets the aircraft's score.
	 * @return the score of the aircraft.
	 */
	public int score() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}

	public void resetScore() {
		this.score = 100;
	}

	public void clearScore() {
		this.score = 0;
	}

	/**
	 * Allows access to whether the aircraft has reached its destination.
	 * @return the aircraft's status
	 */
	public AirportState status() {
		return status;
	}

	public void setStatus(AirportState finishedStatus) {
		this.status = finishedStatus;
	}
	
	public Airport airport() {
		return airport;
	}
	
	public void clearAirport() {
		this.airport = null;
	}
	
	public void clearCurrentRouteStage() {
		this.currentRouteStage = 0;
	}
	
	public void clearManualBearingTarget() {
		this.manualBearingTarget = Double.NaN;
	}

	/**
	 * Calculates the angle from the aircraft's position, to its current target.
	 * @return an angle in radians to the aircraft's current target.
	 */
	public double angleToTarget() {
		if (isManuallyControlled) {
			return (manualBearingTarget == Double.NaN) ? bearing() : manualBearingTarget;
		} else {
			return Math.atan2(currentTarget.y() - position.y(),
					currentTarget.x() - position.x());
		}
	}
	
	/**
	 * Checks whether the aircraft lies outside of the airspace.
	 * @return true, if the aircraft is out of the airspace. False, otherwise.
	 */
	public boolean outOfBounds() {
		double x = position.x();
		double y = position.y();
		return (x < RADIUS
				|| x > window.width() + RADIUS - 32
				|| y < RADIUS
				|| y > window.height() + RADIUS - 144);
	}

	/**
	 * Calculates the angle at which the aircraft is travelling.
	 * @return the angle in radians of the aircraft's current velocity.
	 */
	public double bearing() {
		return Math.atan2(velocity.y(), velocity.x());
	}
	
	/**
	 * Allows access to the magnitude of the aircraft's velocity. 
	 * @return the speed at which the aircraft is currently going.
	 */
	public double speed() {
		return velocity.magnitude();
	}
	
	/**
	 * Checks if the aircraft is at a given position.
	 * @param point
	 * @return true, if the aircraft is near enough the point. False, otherwise.
	 */
	public boolean isAt(Vector point) {
		double dy = point.y() - position.y();
		double dx = point.x() - position.x();
		return (dy*dy + dx*dx) < 4*4;
	}
	
	/**
	 * Checks whether the angle at which the aircraft is turning is less than 0.
	 * @return true, if the aircraft is turning left (anti-clockwise). False, otherwise.
	 */
	public boolean isTurningLeft() {
		return currentlyTurningBy < 0;
	}
	
	/**
	 * Checks whether the angle at which the aircraft is turning is greater than 0.
	 * @return true, if the aircraft is turning right (clockwise). False, otherwise.
	 */
	public boolean isTurningRight() {
		return currentlyTurningBy > 0;
	}
	
	/**
	 * Checks the aircraft's route to see if a waypoint is included in it.
	 * @param waypoint the waypoint to check for.
	 * @return true, if the waypoint is in the aircraft's route. False, otherwise.
	 */
	public int flightPathContains(Waypoint waypoint) {
		int index = -1;
		for (int i = 0; i < route.length; i++) {
			if (route[i] == waypoint) index = i;
		}
		return index;
	}
	
	public boolean doesflightPathContain(Waypoint waypoint) {
		for (int i = 0; i < route.length; i ++) {
			if (route[i] == waypoint) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Edits the aircraft's path by changing the waypoint it will go to at a certain stage
	 * in its route.
	 * @param routeStage the stage at which the new waypoint will replace the old.
	 * @param newWaypoint the new waypoint to travel to.
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		route[routeStage] = newWaypoint;
		if (routeStage == currentRouteStage) {
			currentTarget = newWaypoint.position();
		}
		
		// Following decrements score in case of flight path alteration.
		if (score > 50) {			
			score -= 2;			// Greatest penalty for 'best' aircraft.
		}
		else if (score > 0) {
			score -= 1;			// Penalty decreases, score must be +ve.
		}
		
		// Release from manual control
		setManuallyControlled(false);
	}
	
	/**
	 * Checks whether the mouse cursor is over this aircraft.
	 * @param mx the x coordinate of the mouse cursor.
	 * @param my the y coordinate of the mouse cursor.
	 * @return true, if the mouse is close enough to this aircraft;
	 * 			false otherwise
	 */
	public boolean isMouseOver(int mx, int my) {
		double dx = position.x() - mx;
		double dy = position.y() - my;
		return dx*dx + dy*dy < MOUSE_LENIENCY*MOUSE_LENIENCY;
	}
	
	/**
	 * Calls {@link #isMouseOver(int, int)} using {@link lib.jog.input#mouseX()} and
	 * {@link  lib.jog.input#mouseY()} as the arguments.
	 * @return true, if the mouse is close enough to this aircraft;
	 * 			false otherwise
	 */
	public boolean isMouseOver() {
		return isMouseOver(input.mouseX(), input.mouseY());
	}
	
	/**
	 * Updates the aircraft's position and bearing, the stage of its route,
	 * and whether it has finished its flight.
	 * @param dt
	 */
	public void update(double dt) {
		if (status == AirportState.FINISHED) return;
		
		if ((altitudeState != null) && (status == AirportState.NORMAL)) {
			switch (altitudeState) {
			case FALLING:
				fall();
				break;
			case LEVEL:
				break;
			case CLIMBING:
				climb();
				break;
			}
		}

		// Update target
		if (isAt(currentTarget) && (status == AirportState.NORMAL)) {	
			if (currentTarget.equals(destination.position())) {
				if (destination.type() == Waypoint.WaypointType.EXIT) {
					status = AirportState.FINISHED;
				} else if (destination.type() == Waypoint.WaypointType.AIRPORT) {
					altitudeState = AltitudeState.LEVEL;
					airport.addAircraft(this);
				}
			} else if (currentRouteStage == route.length-1) {
				if (status == AirportState.NORMAL) {
					currentRouteStage++;
					currentTarget = destination.position();
				}
			} else {
				if (status == AirportState.NORMAL) {
					currentRouteStage++;
					currentTarget = route[currentRouteStage].position();
				}
			}
		}
		
		if (status != AirportState.PARKED) {
			// Update position
			Vector dv = new Vector(0, 0, 0);
			
			if (status == AirportState.NORMAL) {
				dv = velocity.scaleBy(dt);
			} else {
				dv = new Vector(velocity.x(), velocity.y(), 0).scaleBy(dt);
			}
			
			position = position.add(dv);

			currentlyTurningBy = 0;

			// Update bearing
			if (Math.abs(angleToTarget() - bearing()) > bearingLeniency) {
				turnTowardsTarget(dt);
			}
		}
	}
	
	/**
	 * Turns the aircraft left.
	 * @param dt the time elapsed since the last frame.
	 */
	public void turnLeft(double dt) {
		turnBy(-dt * turnSpeed);
		manualBearingTarget = Double.NaN;
	}
	
	/**
	 * Turns the aircraft right.
	 * @param dt the time elapsed since the last frame.
	 */
	public void turnRight(double dt) {
		turnBy(dt * turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	/**
	 * Turns the aircraft by a certain angle (in radians).
	 * Positive angles turn the aircraft clockwise.
	 * @param angle the angle by which to turn.
	 */
	private void turnBy(double angle) {
		currentlyTurningBy = angle;
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		double x = velocity.x();
		double y = velocity.y();
		velocity = new Vector(x*cosA - y*sinA, y*cosA + x*sinA, velocity.z());
	}

	/**
	 * Turns the aircraft towards its current target.
	 * How much it turns is determined by the aircraft's
	 * {@link #turnSpeed}.
	 * @param dt the time elapsed since the last frame.
	 */
	private void turnTowardsTarget(double dt) {
		// Get difference in angle
		double angleDifference = (angleToTarget() % (2 * Math.PI)) - (bearing() % (2 * Math.PI));
		boolean crossesPositiveNegativeDivide = angleDifference < -Math.PI * 7 / 8;
		// Correct difference
		angleDifference += Math.PI;
		angleDifference %= (2 * Math.PI);
		angleDifference -= Math.PI;
		// Get which way to turn.
		int angleDirection = (int)(angleDifference /= Math.abs(angleDifference));
		if (crossesPositiveNegativeDivide) angleDirection *= -1;  
		double angleMagnitude = Math.min(Math.abs((dt * turnSpeed)), Math.abs(angleDifference)); 
		turnBy(angleMagnitude * angleDirection);
	}
	
	/**
	 * Draws the aircraft and any warning circles if necessary. 
	 */
	public void draw(int controlAltitude) {
		double scale = 2*(Math.max(position.z(), 28000) / 30000);
		graphics.setColour(128, 128, 128, 255);
		graphics.draw(image, scale, position.x(), position.y(), bearing(), 8, 8);
		graphics.setColour(128, 128, 128, 255/2.5);
		graphics.print(String.format("%.0f", position.z()) + "+", position.x()+8, position.y()-8);
		
		// Draw warning circles, but only if the aircraft isn't
		// at the airport
		if (!(this.status == AirportState.LANDING)
			&& !(this.status == AirportState.TAKEOFF)
			&& !(this.status == AirportState.PARKED)) {
			drawWarningCircles();
		}
	}
	
	/**
	 * Draws the compass around this aircraft
	 */
	public void drawCompass() {
		graphics.setColour(0, 128, 0);
		graphics.circle(false, position.x() + 16, position.y() + 16, COMPASS_RADIUS);
		
		for (int i = 0; i < 360; i += 60) {
			double r = Math.toRadians(i - 90);
			double x = position.x() + 16 + (1.1 * COMPASS_RADIUS * Math.cos(r));
			double y = position.y() + 14 + (1.1 * COMPASS_RADIUS * Math.sin(r));
			if (i > 170) x -= 24;
			if (i == 180) x += 12;
			graphics.print(String.valueOf(i), x, y);
		}
		
		double x, y;
		
		if (isManuallyControlled && input.isMouseDown(input.MOUSE_LEFT)) {
			Vector vectorToMouse = new Vector(input.mouseY() - position.y(),
					input.mouseX() - position.x(), 0);
			if (vectorToMouse.magnitudeSquared()
					<= COMPASS_RADIUS * COMPASS_RADIUS) {
				graphics.setColour(0, 128, 0, 128);
				double r = Math.atan2(input.mouseY() - position.y(),
						input.mouseX() - position.x());
				x = 16 + position.x() + (COMPASS_RADIUS * Math.cos(r));
				y = 16 + position.y() + (COMPASS_RADIUS * Math.sin(r));
				graphics.line(position.x() + 16, position.y() + 16, x, y);
				graphics.line(position.x() + 15, position.y() + 16, x, y);
				graphics.line(position.x() + 16, position.y() + 15, x, y);
				graphics.line(position.x() + 17, position.y() + 16, x, y);
				graphics.line(position.x() + 17, position.y() + 17, x, y);
				graphics.setColour(0, 128, 0, 16);
			}
		}
		
		x = 16 + position.x() + (COMPASS_RADIUS * Math.cos(bearing()));
		y = 16 + position.y() + (COMPASS_RADIUS * Math.sin(bearing()));
		
		graphics.line(position.x() + 16, position.y() + 16, x, y);
		graphics.line(position.x() + 15, position.y() + 16, x, y);
		graphics.line(position.x() + 16, position.y() + 15, x, y);
		graphics.line(position.x() + 17, position.y() + 16, x, y);
		graphics.line(position.x() + 17, position.y() + 17, x, y);
		
	}
	
	/**
	 * Draws warning circles around this aircraft and any others that are too near.
	 */
	public void drawWarningCircles() {
		for (Aircraft aircraft : aircraftTooNear) {
			Vector midPoint = position.add(aircraft.position).scaleBy(0.5);
			double radius = position.sub(midPoint).magnitude() * 2;
			graphics.setColour(128, 0, 0);
			graphics.circle(false, midPoint.x(), midPoint.y(), radius);
		}	
	}

	/**
	 * Draws lines starting from the aircraft, along its flight path to its destination.
	 */
	public void drawFlightPath() {
		graphics.setColour(0, 128, 128);
		
		// Draw lines between each waypoint on route
		for (int i = currentRouteStage; i < route.length-1; i++) {
			graphics.line(route[i].position().x(),
					route[i].position().y(),
					route[i+1].position().x(),
					route[i+1].position().y());	
		}
		
		if (currentTarget == destination.position()) {
			// If next waypoint is the destination,
			// draw direct line from aircraft to destination
			graphics.line(position.x(),
					position.y(),
					destination.position().x(),
					destination.position().y());
		} else {
			// Draw line from aircraft to first waypoint
			graphics.line(position.x(),
					position.y(),
					route[currentRouteStage].position().x(),
					route[currentRouteStage].position().y());

			// Draw line from last waypoint to destination
			graphics.line(route[route.length-1].position().x(),
					route[route.length-1].position().y(),
					destination.position().x(),
					destination.position().y());
		}
	}
	
	/**
	 * Visually represents the pathpoint being moved.
	 * @param mouseX current position of mouse
	 * @param mouseY current position of mouse
	 */
	public void drawModifiedPath(int modified, double mouseX, double mouseY) {
		graphics.setColour(0, 128, 128, 128);
		if (currentRouteStage > modified-1) {
			graphics.line(position().x(), position().y(), mouseX, mouseY);
		} else {
			graphics.line(route[modified-1].position().x(),
					route[modified-1].position().y(), mouseX, mouseY);
		}
		
		if (currentTarget == destination.position()) {
			graphics.line(mouseX, mouseY,
					destination.position().x(), destination.position().y());
		} else {
			int index = modified + 1;
			
			if (index == route.length){ //modifying final waypoint in route
				//line drawn to final waypoint
				graphics.line(mouseX, mouseY,
						destination.position().x(), destination.position().y());
			} else {
				graphics.line(mouseX, mouseY, route[index].position().x(),
						route[index].position().y());
			}
		}
	}
	
	/**
	 * Creates a sensible route from an origin to a destination from an array of waypoints.
	 * 
	 * Waypoint costs are considered according to distance from current aircraft location
	 * Costs are further weighted by distance from waypoint to destination
	 * @param origin the waypoint from which to begin
	 * @param destination the waypoint at which to end
	 * @param waypoints the waypoints to be used
	 * @return a sensible route between the origin and the destination, using a sensible
	 * 			amount of waypoint
	 */
	public Waypoint[] findGreedyRoute(Waypoint origin, Waypoint destination,
			Waypoint[] waypoints) {
		Waypoint[] allWaypoints;
		
		// is destination in waypoints list
		boolean destInWaypoints = false;
		
		for (int i = 0; i < waypoints.length; i++) {
			if (waypoints[i] == destination) {
				destInWaypoints = true;
				break;
			}
		}
		
		// if dest not in waypoint list, add it in
		if (!destInWaypoints) {
			int waypointCount = 0;
			allWaypoints = new Waypoint[waypoints.length + 1];
			
			for (Waypoint waypoint : waypoints) {
				allWaypoints[waypointCount] = waypoint;
				waypointCount++;
			}

			allWaypoints[waypointCount] = destination;
		} else {
			allWaypoints = waypoints.clone();
		}
		
		// Create an array to hold the route as we generate it
		ArrayList<Waypoint> selectedWaypoints = new ArrayList<Waypoint>();
		
		// Create a waypoint which will track our position as we generate the route
		// Initialise this to the start of the route
		Waypoint currentPos = origin;

		// Set the cost to a high value (as per using a greedy algorithm)
		double cost = 99999999999999.0;
		
		// Create a waypoint which will track the closest waypoint
		Waypoint cheapest = null;
		
		// Set a flag so we can tell if the route is complete
		boolean atDestination = false;
		
		while (!atDestination) {
			// For each possible waypoint
			for (Waypoint point : allWaypoints) {
				boolean skip = false;

				for (Waypoint routePoints : selectedWaypoints) {
					// Check we have not already selected the waypoint
					// If we have, skip evaluating the point
					// This protects the aircraft from getting stuck looping between points
					if (routePoints.position().equals(point.position())) {
						skip = true; //flag to skip
						break; // no need to check rest of list, already found a match.
					}
				}
				
				// Do not consider the waypoint we are currently at or the origin
				// Do not consider offscreen waypoints which are not the destination
				// Also skip if flagged as a previously selected waypoint
				if (skip == true
						|| point.position().equals(currentPos.position())
						|| point.position().equals(origin.position())
						|| ((point.type() != Waypoint.WaypointType.AIRSPACE)
								&& (!point.position().equals(destination.position())))) {
					skip = false;
					continue;
				} else {
					// Get the cost of visiting waypoint
					// Compare cost this cost to the current cheapest
					// If smaller, then this is the new cheapest waypoint
					if (point.getCost(currentPos) + 0.5
							* Waypoint.getCostBetween(point, destination) < cost) {
						// Cheaper route found, so update
						cheapest = point;
						cost = point.getCost(currentPos) + 0.5
								* Waypoint.getCostBetween(point, destination);
					}
				}
			}

			// The cheapest waypoint must have been found
			assert cheapest != null : "The cheapest waypoint was not found";

			// If the cheapest waypoint is the destination, then we have sucessfully
			// generated a route to the aircraft's destination, so break out of loop
			if (cheapest.position().equals(destination.position())) {
				// route has reached destination 
				// break out of while loop
				atDestination = true;
			}

			// Update the selected route
			// Consider further points in route from the position of the selected point
			selectedWaypoints.add(cheapest);
			currentPos = cheapest;
			
			// Resaturate cost for next loop
			cost = 99999999999.0;
		}
		
		// Create an array to hold the new route
		Waypoint[] route = new Waypoint[selectedWaypoints.size()];
		
		// Fill route with the selected waypoints
		for (int i = 0; i < selectedWaypoints.size(); i++) {
			route[i] = selectedWaypoints.get(i);
		}

		return route;
	}

	/**
	 * Updates the amount of aircraft which are too close, violating the separation rules,
	 * and also checks for crashes.
	 * @param dt the time elapsed since the last frame.
	 * @param aircraftList the list of aircraft to update
	 * @return 0 if no collisions, 1 if separation violation, 2 if crash
	 */
	public int updateCollisions(double dt, ArrayList<Aircraft> aircraftList) {
		aircraftTooNear.clear();
		for (int i = 0; i < aircraftList.size(); i++) {
			Aircraft aircraft = aircraftList.get(i);
			if ((aircraft != this) && (isWithin(aircraft, RADIUS))) {
				status = AirportState.FINISHED;
				return i;
			} else if (aircraft != this && isWithin(aircraft, separationRule)) {
				aircraftTooNear.add(aircraft);
				if (collisionWarningSoundFlag == false){
					collisionWarningSoundFlag = true;
					
					if (WARNING_SOUND != null) {
						WARNING_SOUND.play();
					}
					
					// Following decrements score in case of separation violation
					// But don't apply if aircraft are in stack
					if (status != AirportState.WAITING) {
						if (score > 50) {		
							score -= 10;			// Greatest penalty for 'best' aircraft.
						}
						else if (score > 20) {
							score -= 5;			// Penalty decreases.
						}
						else if (score >  0) {
							score -= 1;			// Decrease again, score must be +ve.
						}
					}
				}
			}
		}
		
		if (aircraftTooNear.isEmpty()){
			collisionWarningSoundFlag = false;
		}
		
		return -1;
	}
	
	/**
	 * Checks whether an aircraft is within a certain distance from this one.
	 * @param aircraft the aircraft to check.
	 * @param distance the distance within which to care about.
	 * @return true, if the aircraft is within the distance. False, otherwise.
	 */
	private boolean isWithin(Aircraft aircraft, int distance) {
		double dx = aircraft.position().x() - position.x();
		double dy = aircraft.position().y() - position.y();
		double dz = aircraft.position().z() - position.z();
		return dx*dx + dy*dy + dz*dz < distance*distance;
	}

	/**
	 * Toggles the state of whether this aircraft is manually controlled.
	 */
	public void toggleManualControl() {
		isManuallyControlled = !isManuallyControlled;
		if (!isManuallyControlled) resetBearing();
	}
	
	/**
	 * Gets the direction the aircraft is going in.
	 * @return the direction the aircraft is going in
	 */
	public double manualBearing() {
		return manualBearingTarget;
	}

	/**
	 * Changes the direction the aircraft is going towards.
	 * @param newHeading
	 */
	public void setBearing(double newHeading) {
		manualBearingTarget = newHeading;
	}

	/**
	 * Resets the direction towards which the aircraft will head.
	 */
	private void resetBearing() {
		if (currentRouteStage < route.length) {
			currentTarget = route[currentRouteStage].position();
		}
		turnTowardsTarget(0);
	}
	
	/**
	 * Increases the aircraft's altitude.
	 */
	public void climb() {
		if (position.z() < 30000 && altitudeState == AltitudeState.CLIMBING)
			velocity.setZ(altitudeChangeSpeed);
		if (position.z() >= 30000){
			velocity.setZ(0);
			altitudeState = AltitudeState.LEVEL;
			position = new Vector(position.x(), position.y(), 30000);
		}
	}

	/**
	 * Decreases the aircraft's altitude.
	 */
	public void fall() {
		if (position.z() > 28000 && altitudeState == AltitudeState.FALLING)
			velocity.setZ(-altitudeChangeSpeed);
		if (position.z() <= 28000){
			velocity.setZ(0);
			altitudeState = AltitudeState.LEVEL;
			position = new Vector(position.x(), position.y(), 28000);
		}
	}
	
	/**
	 * Sets the aircraft's altitude.
	 * @param altitude the altitude to set
	 */
	public void setAltitude(double altitude) {
		position.setZ(altitude);
	}
	
	/**
	 * Outputs the aircraft's key details in a readable format.
	 * @return the textual representation of the aircraft
	 */
	public String toString() {
		return ("Name: " + flightName + " | "
				+ "XPos: " + position.x() + " | "
				+ "YPos: " + position.y());
	}

	public double getAltitude() {
		return position.z();
	}
	
}
