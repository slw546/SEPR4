package tst;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import cls.Aircraft;
import cls.Waypoint;
import cls.Vector;

/**
 * Tests for the Aircraft class
 * 
 * <p>
 * Covers:
 * <ul>
 * <li>Accessors</li>
 * </ul>
 * </p>
 */
public class AircraftTest {
	
	/** The origin to give to testing aircraft */
	private String testAircraftOrigin;
	
	/** The destination to give to testing aircraft */
	private String testAircraftDest;
	
	/**
	 * Before method.
	 * 
	 * <p>
	 * Runs before any tests in this class. Sets up attributes.
	 * </p>
	 */
	@Before
	public void beforeTests() {
		this.testAircraftOrigin = "Dublin";
		this.testAircraftDest = "Berlin";
	}
	
	/**
	 * Creates a 'testing' aircraft
	 * 
	 * <p>
	 * Helper method - builds a new aircraft (with a flight plan) for
	 * use with testing methods.
	 * </p>
	 */
	private Aircraft generateTestAircraft() {
		Waypoint[] waypointList = new Waypoint[] {new Waypoint(0, 0, 1),
				new Waypoint(100, 100, 1), new Waypoint(25, 75, 0),
				new Waypoint(75, 25, 0), new Waypoint(50, 50, 0)};
		
		Aircraft testAircraft = new Aircraft(this.testAircraftDest, this.testAircraftOrigin,
				new Waypoint(100, 100, 1), new Waypoint(0, 0, 1),
				null, 10.0, waypointList, null);
		
		return testAircraft;
	}

	/**
	 * Tests the Aircraft.position() method
	 * 
	 * <p>
	 * Checks that the position set by the generateTestAircraft method is valid.
	 * </p>
	 * <p>
	 * NOTE: this <b>doesn't</b> ensure that the position is set correctly,
	 * just that the value returned is valid.
	 * </p>
	 */
	@Test
	public void testGetPosition() {
		Aircraft testAircraft = generateTestAircraft();
		Vector resultPosition = testAircraft.position();
		assertTrue("x >= -128 and xy <= 27, y = 0, z = 28,000 or z = 30,000",
				((0 == resultPosition.y()) && (128 >= resultPosition.x())
						&& (-128 <= resultPosition.x()) && ((28000 == resultPosition.z())
								|| (30000 == resultPosition.z()))));
	}
	
	/**
	 * Tests the Aircraft.originName() method
	 * 
	 * <p>
	 * Checks that the origin name is returned correctly.
	 * </p>
	 */
	@Test
	public void testGetOriginName() {
		Aircraft testAircraft = generateTestAircraft();
		String name = testAircraft.originName();
		assertTrue("Origin name = test origin name",
				name.equals(this.testAircraftOrigin));
	}
	
	/**
	 * Tests the Aircraft.destinationName() method
	 * 
	 * <p>
	 * Checks that the destination name is returned correctly.
	 * </p>
	 */
	@Test
	public void testGetDestinationName() {
		Aircraft testAircraft = generateTestAircraft();
		String name = testAircraft.destinationName();
		assertTrue("Destination name = test dest name",
				name.equals(this.testAircraftDest));
	}
	
	/**
	 * Tests the Aircraft.isFinished() method
	 * 
	 * <p>
	 * Checks that the 'finished' property is returned correctly.
	 * </p>
	 */
	@Test
	public void testGetIsFinishedName() {
		Aircraft testAircraft = generateTestAircraft();
		Aircraft.AirportState status = testAircraft.status();
		assertTrue("Finished = false", status == Aircraft.AirportState.NORMAL);
	}
	
	/**
	 * Tests the Aircraft.isManuallyControlled() method
	 * 
	 * <p>
	 * Checks that the manual control flag is returned correctly.
	 * </p>
	 */
	@Test
	public void testIsManuallyControlled() {
		Aircraft testAircraft = generateTestAircraft();
		boolean status = testAircraft.isManuallyControlled();
		assertTrue("Manually controlled = false", false == status);
	}
	
	/**
	 * Tests the Aircraft.speed() method
	 * 
	 * <p>
	 * Checks that the speed is returned correctly.
	 * </p>
	 */
	@Test
	public void testGetSpeed() {
		Aircraft testAircraft = generateTestAircraft();
		double speed = (int) (testAircraft.speed() + 0.5);
		assertTrue("Speed = 20", speed == 20.0);
	}
	
	/**
	 * Tests the Aircraft.altitudeState() method
	 * 
	 * <p>
	 * Checks that the altitude state is returned correctly.
	 * </p>
	 */
	@Test
	public void testAltitudeState() {
		Aircraft testAircraft = generateTestAircraft();
		testAircraft.setAltitudeState(Aircraft.AltitudeState.CLIMBING);
		Aircraft.AltitudeState altState = testAircraft.altitudeState();
		assertTrue("Altitude State = 1", altState == Aircraft.AltitudeState.CLIMBING);
	}
	
	/**
	 * Tests the Aircraft.outOfBounds() method
	 * 
	 * <p>
	 * Checks that the 'out of bounds' flag is correctly set.
	 * </p>
	 * <p>
	 * NOTE: This only tests for one instance where it is known that the
	 * plane is out of bounds. This does <b>not</b> test if <code>false</code>
	 * is correctly returned when the aircraft is within the game bounds.
	 * </p>
	 */
	@Test
	public void testOutOfBounds() {
		Aircraft testAircraft = generateTestAircraft();		
		boolean x = testAircraft.outOfBounds();
		assertTrue("Out of bounds = false", x == true);
	}

	/**
	 * Tests the Aircraft.setAltitudeState() method
	 * 
	 * <p>
	 * Checks that the altitude state is correctly set.
	 * </p>
	 * <p>
	 * NOTE: This only tests for one instance where altitude state is set to 1.
	 * </p>
	 */
	@Test
	public void testSetAltitudeState() {
		Aircraft testAircraft = generateTestAircraft();
		testAircraft.setAltitudeState(Aircraft.AltitudeState.CLIMBING);
		Aircraft.AltitudeState altState = testAircraft.altitudeState();
		assertTrue("Altitude State = 1", altState == Aircraft.AltitudeState.CLIMBING);
	}

}
