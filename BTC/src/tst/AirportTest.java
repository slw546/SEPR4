package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cls.Aircraft;
import cls.Aircraft.AirportState;
import cls.Aircraft.AltitudeState;
import cls.Airport;
import cls.Waypoint;

import scn.Game;

public class AirportTest {

	/** The game instance to run tests in */
	Game testGame;

	/** The entry points to assign to the test airport */
	Waypoint[] testEntryWaypoints = new Waypoint[] {
			new Waypoint(677, 44, 2),
			new Waypoint(767, 0, 2),
			new Waypoint(957, 44, 2)
	};

	/** The entry point names to assign to the test airport */
	String[] testEntryWaypointNames = new String[] {
			"Land West",
			"Land East"
	};

	/** The landing points to assign to the test airport */
	Waypoint[] testLandingWaypoints = new Waypoint[] {
			new Waypoint(817, 104, 2),
			new Waypoint(790, 743, 2),
			new Waypoint(800, 733, 2),
			new Waypoint(793, 723, 2),
			new Waypoint(804, 608, 2),
			new Waypoint(869, 657, 2),
			new Waypoint(882, 546, 2),
			new Waypoint(950, 496, 2),
			new Waypoint(970, 486, 2),
			new Waypoint(1010, 486, 2)
	};

	/** The parking points to assign to the test airport */
	Waypoint[] testParkingWaypoints = new Waypoint[] {
			new Waypoint(1102, 358, 2),
			new Waypoint(1103, 425, 2),
			new Waypoint(1098, 493, 2),
			new Waypoint(1095, 567, 2)
	};

	/** The takeoff points to assign to the test airport */
	Waypoint[] testTakeoffWaypoints = new Waypoint[] {
			new Waypoint(1010, 486, 2),
			new Waypoint(1001, 488, 2),
			new Waypoint(991, 671, 2),
			new Waypoint(954, 678, 2),
			new Waypoint(954, 655, 2),
			new Waypoint(976, 46, 2)
	};

	/** A test airport */
	Airport testAirport = new Airport("Chkalovsky Airport",
			4,
			testEntryWaypointNames,
			testEntryWaypoints,
			testLandingWaypoints,
			testParkingWaypoints,
			testTakeoffWaypoints);

	/** The name to give to the first testing aircraft */
	private String testAircraftName;

	/** The origin to give to testing aircraft */
	private String testAircraftOrigin;

	/** The destination to give to testing aircraft */
	private String testAircraftDest;
	
	/**
	 * Sets up the testing environment.
	 * <p>
	 * Specifically, this creates an instance of Game to used in the tests in
	 * this class.
	 * </p>
	 */
	@Before
	public void beforeTests() {
		this.testGame = new Game(null, 1);
		this.testAircraftName = "TestAircraft1";
		this.testAircraftOrigin = "Dublin";
		this.testAircraftDest = "Berlin";
		this.testGame = new Game(null, 1);
	}

	/**
	 * Generates the first test aircraft.
	 * @return the first test aircraft
	 */
	private Aircraft generateTestAircraft() {
		Waypoint[] waypointList = new Waypoint[] {new Waypoint(0, 0, 1),
				new Waypoint(100, 100, 1), new Waypoint(25, 75, 0),
				new Waypoint(75, 25, 0), new Waypoint(50, 50, 0)};

		Aircraft testAircraft = new Aircraft(this.testAircraftName,
				this.testAircraftDest, this.testAircraftOrigin,
				new Waypoint(100, 100, 1), new Waypoint(0, 0, 1),
				null, 10.0, waypointList, null);

		return testAircraft;
	}

	/**
	 * Generates a test aircraft with altitude set to zero.
	 * @return the test aircraft
	 */
	private Aircraft generateTestAircraftZeroAltitude() {
		Waypoint[] waypointList = new Waypoint[] {new Waypoint(0, 0, 1),
				new Waypoint(100, 100, 1), new Waypoint(25, 75, 0),
				new Waypoint(75, 25, 0), new Waypoint(50, 50, 0)};

		Aircraft testAircraft = new Aircraft(this.testAircraftName,
				this.testAircraftDest, this.testAircraftOrigin,
				new Waypoint(100, 100, 1), new Waypoint(0, 0, 1),
				null, 10.0, waypointList, null);

		return testAircraft;
	}

	/**
	 * Generates the second test aircraft.
	 * @return the second test aircraft
	 */
	private Aircraft generateTestAircraft2() {
		Waypoint[] waypointList = new Waypoint[] {new Waypoint(0, 0, 1),
				new Waypoint(100, 100, 1), new Waypoint(25, 75, 0),
				new Waypoint(75, 25, 0), new Waypoint(50, 50, 0)};

		Aircraft testAircraft = new Aircraft(this.testAircraftName,
				this.testAircraftDest, this.testAircraftOrigin,
				new Waypoint(100, 100, 1), new Waypoint(0, 0, 1),
				null, 10.0, waypointList, null);

		return testAircraft;
	}

	/**
	 * Generates the third test aircraft.
	 * @return the third test aircraft
	 */
	private Aircraft generateTestAircraft3() {
		Waypoint[] waypointList = {new Waypoint(767, 0, 2)};

		Aircraft testAircraft = new Aircraft(this.testAircraftName,
				this.testAircraftDest, this.testAircraftOrigin,
				new Waypoint(767, 0, 2), new Waypoint(670, 44, 2),
				null, 10.0, waypointList, testAirport, true);

		return testAircraft;
	}

	/**
	 * Tests that adding an aircraft to an airport functions correctly.
	 */
	@Test
	public void testAddPlane(){
		Aircraft testAircraft = generateTestAircraft();
		testAirport.addAircraft(testAircraft);
		assertTrue("Add plane to airport",testAirport.aircraft().contains(testAircraft)==true); 
	}

	/**
	 * Tests that aircraft descend correctly.
	 */
	@Test
	public void testCauseDescent(){
		//change causeAscent to public
		//doesn't descend at 0
		Aircraft testAircraft = generateTestAircraft();	
		testAirport.addAircraft(testAircraft);
		testAircraft.setAltitudeState(AltitudeState.FALLING);
		testAircraft.setAltitude(0);
		testAirport.causeDescent(testAircraft, true);
		assertTrue("Altitude state changes to LEVEL", testAircraft.altitudeState()==AltitudeState.LEVEL);	
		assertTrue("Altitude is at 0ft", testAircraft.getAltitude() == 0);
	}

	/**
	 * Tests that aircraft ascend correctly.
	 */
	@Test
	public void testCauseAscent(){
		// change causeDescent to public
		// doesnt ascend at 30000
		Aircraft testAircraft = generateTestAircraft();
		testAirport.addAircraft(testAircraft);
		testAircraft.setAltitudeState(AltitudeState.CLIMBING);
		testAircraft.setAltitude(30000);
		testAirport.causeAscent(testAircraft, true);
		assertTrue("Altitude state changes to LEVEL", testAircraft.altitudeState()==AltitudeState.LEVEL);	
		assertTrue("Altitude stays at 30000ft", testAircraft.getAltitude() == 30000);

		//same as above
	}

	/**
	 * Tests that aircraft are removed correctly.
	 */
	@Test 
	public void testRemoveLeavingAircraft(){
		// aircraftLeaving to public
		// removeLeavingAircraft to public
		Aircraft testAircraft = generateTestAircraft();
		testAirport.addAircraft(testAircraft);
		testAirport.aircraftLeaving().add(testAircraft);
		assertTrue("Aircraft leaving list has testAircraft in.", testAirport.aircraftLeaving().contains(testAircraft)==true);
		testAirport.removeLeavingAircraft(true);
		assertTrue("Aircraft in airport is 0", testAirport.aircraft().contains(testAircraft)==false);
	}

	/** 
	 * Tests that aircraft are added to the start of the airport aircraft list.
	 */
	@Test	
	public void testGetWaypointArrayIndex(){
		Aircraft testAircraft = generateTestAircraft();
		testAirport.addAircraft(testAircraft);
		assertTrue("Test get aircraft index", testAirport.aircraft().indexOf(testAircraft) == 0);
	}

	/**
	 * Tests that aircraft can enter the waiting state.
	 */
	@Test
	public void testUpdateStartInWaiting(){
		Aircraft testAircraft = generateTestAircraft();
		testAirport.addAircraft(testAircraft);
		assertTrue("Starts in WAITING state", testAircraft.status() == AirportState.WAITING);
	}
	
}


