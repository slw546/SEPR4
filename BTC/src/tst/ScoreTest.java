package tst;

import org.junit.Before;
import org.junit.Test;

import cls.Aircraft;
import cls.Aircraft.AirportState;
import cls.Waypoint;
import static org.junit.Assert.*;
import scn.Game;


public class ScoreTest {
	
	/** The game instance to run tests in */
	Game testGame; 
	
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
		this.testAircraftOrigin = "Dublin";
		this.testAircraftDest = "Berlin";
		this.testGame = new Game(null, 1);
	}
	
	/**
	 * Generates the first test aircraft.
	 * @return the first test aircraft
	 */
	private Aircraft generateTestAircraft() {
		Waypoint[] waypointList = new Waypoint[] {
			new Waypoint(0, 0, Waypoint.WaypointType.ENTRY),
			new Waypoint(100, 100, Waypoint.WaypointType.EXIT), 
			new Waypoint(25, 75, Waypoint.WaypointType.AIRSPACE),
			new Waypoint(75, 25, Waypoint.WaypointType.AIRSPACE), 
			new Waypoint(50, 50, Waypoint.WaypointType.AIRSPACE)
		};
		
		Aircraft testAircraft = new Aircraft(this.testAircraftDest, this.testAircraftOrigin,
				waypointList[1], waypointList[0], null, 10.0, waypointList, null);
		
		return testAircraft;
	}
	
	/**
	 * Generates another test aircraft.
	 * <p>
	 * This aircraft will be violating the separation distance of the
	 * first aircraft.
	 * </p>
	 * @return the test aircraft
	 */
	private Aircraft generateTestAircraft2() {
		Waypoint[] waypointList = new Waypoint[] {
			new Waypoint(0, 0, Waypoint.WaypointType.ENTRY),
			new Waypoint(100, 100, Waypoint.WaypointType.EXIT), 
			new Waypoint(25, 75, Waypoint.WaypointType.AIRSPACE),
			new Waypoint(75, 25, Waypoint.WaypointType.AIRSPACE), 
			new Waypoint(50, 50, Waypoint.WaypointType.AIRSPACE)
		};
		
		Aircraft testAircraft = new Aircraft(this.testAircraftDest, this.testAircraftOrigin,
				waypointList[1], new Waypoint(-70, 0, Waypoint.WaypointType.ENTRY), null, 10.0, waypointList, null);
		
		return testAircraft;
	}
	
	/**
	 * Generates another test aircraft.
	 * <p>
	 * This aircraft will <b>not</b> be violating the separation distance of the
	 * first aircraft.
	 * </p>
	 * @return the test aircraft
	 */
	private Aircraft generateTestAircraft3() {
		Waypoint[] waypointList = new Waypoint[] {
			new Waypoint(0, 0, Waypoint.WaypointType.ENTRY),
			new Waypoint(100, 100, Waypoint.WaypointType.EXIT), 
			new Waypoint(25, 75, Waypoint.WaypointType.AIRSPACE),
			new Waypoint(75, 25, Waypoint.WaypointType.AIRSPACE), 
			new Waypoint(50, 50, Waypoint.WaypointType.AIRSPACE)
		};
		
		Aircraft testAircraft = new Aircraft(this.testAircraftDest, this.testAircraftOrigin,
				waypointList[1], new Waypoint(65, 0, Waypoint.WaypointType.ENTRY), null, 10.0, waypointList, null);
		
		return testAircraft;
	}
	
	/**
	 * Tests that when the game starts, the score is zero.
	 */
	@Test
	public void testScoreIsZero() {
		
		int testScore = testGame.totalScore(); 
		assertTrue("Score starts at 0", testScore == 0);
		
	}
		
	/**
	 * Tests that when an aircraft exits it's score is added to the game.
	 */
	@Test
	public void testPlaneExitScore() {
		Aircraft testAircraft = generateTestAircraft(); //create the plane
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testAircraft.setStatus(AirportState.FINISHED);
		//test if score has incremented
		testGame.update(1);
		int testScore = testGame.totalScore();
		assertTrue("Score = 100", testScore == 100);
		
	}
		
	// This is a redundant test, as score is not incremented upon landing anymore

	/**
	 * Tests that when an aircraft lands it's score is added to the game.
	 */
	/*@Test
	public void testPlaneLandingScore() {
		Aircraft testAircraft = generateTestAircraft(); //create the plane
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testAircraft.setStatus(AirportState.PARKED);
		test if score has incremented
		testGame.update(1);
		int testScore = testGame.totalScore();
		assertTrue("Score = 100", testScore == 100);
	}*/
	


	// Test that plane score decrements upon separation violation:

	/**
	 * Tests that an aircraft's score decrements as a result of a separation violation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 100.
	 * </p>
	 */
	@Test
	public void testPlaneDecrementViolation() {
		Aircraft testAircraft = generateTestAircraft();
		Aircraft testAircraft2 = generateTestAircraft2();
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testGame.aircraftInAirspace().add(testAircraft2);
		testAircraft.updateCollisions(1, testGame.aircraftInAirspace());

		assertTrue("Score = 90", testAircraft.score() == 90);
	}
	
	/**
	 * Tests that an aircraft's score decrements as a result of a separation violation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 50.
	 * </p>
	 */
	@Test
	public void testPlaneDecrementViolation2() {
		Aircraft testAircraft = generateTestAircraft();
		Aircraft testAircraft2 = generateTestAircraft2();
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testGame.aircraftInAirspace().add(testAircraft2);
		testAircraft.setScore(50);
		testAircraft.updateCollisions(1, testGame.aircraftInAirspace());

		assertTrue("Score = 45", testAircraft.score() == 45);	
	}
	
	/**
	 * Tests that an aircraft's score decrements as a result of a separation violation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 20.
	 * </p>
	 */
	@Test
	public void testPlaneDecrementViolation3() {
		Aircraft testAircraft = generateTestAircraft();
		Aircraft testAircraft2 = generateTestAircraft2();
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testGame.aircraftInAirspace().add(testAircraft2);
		testAircraft.setScore(20);
		testAircraft.updateCollisions(1, testGame.aircraftInAirspace());

		assertTrue("Score = 19", testAircraft.score() == 19);
	}	
		
	/**
	 * Tests that an aircraft's score decrements as a result of a separation violation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 0.
	 * </p>
	 */
	@Test
	public void testPlaneDecrementViolation4() {
		Aircraft testAircraft = generateTestAircraft();
		Aircraft testAircraft2 = generateTestAircraft2();
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testGame.aircraftInAirspace().add(testAircraft2);
		testAircraft.setScore(0);
		testAircraft.updateCollisions(1, testGame.aircraftInAirspace());

		assertTrue("Score = 0", testAircraft.score() == 0);

	}	

	/**
	 * Tests that the score <b>doesn't</b> decrement for edge cases.
	 */
	@Test
	public void testPlaneDecrementViolationEdge(){
		Aircraft testAircraft = generateTestAircraft();
		Aircraft testAircraft2 = generateTestAircraft3();
		testGame.start();
		testGame.aircraftInAirspace().add(testAircraft);
		testGame.aircraftInAirspace().add(testAircraft2);
		testAircraft.updateCollisions(1, testGame.aircraftInAirspace());
		assertTrue("", testAircraft2.score() == 100);
	}
	
	// Test that plane score decrements upon flight plan manipulation:
	
	/**
	 * Tests that an aircraft's score decrements as a result of flight plan manipulation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 100.
	 * </p>
	 */
	@Test
	public void testScoreDecrementsUponFlightPlanManipulation() {
		Aircraft testAircraft = generateTestAircraft();
		testAircraft.alterPath(1, new Waypoint(65, 0, Waypoint.WaypointType.EXIT));
		assertTrue("Score = 98", testAircraft.score() == 98);
	}
	
	/**
	 * Tests that an aircraft's score decrements as a result of flight plan manipulation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 50.
	 * </p>
	 */
	@Test
	public void testScoreDecrementsUponFlightPlanManipulation2() {
		Aircraft testAircraft = generateTestAircraft();
		testAircraft.setScore(50);
		testAircraft.alterPath(1, new Waypoint(65, 0, Waypoint.WaypointType.EXIT));
		assertTrue("Score = 49", testAircraft.score() == 49);
	}
	
	/**
	 * Tests that an aircraft's score decrements as a result of flight plan manipulation.
	 * <p>
	 * This test covers the case where the aircraft's score is initially 0.
	 * </p>
	 */
	@Test
	public void testScoreDecrementsUponFlightPlanManipulation3() {
		Aircraft testAircraft = generateTestAircraft();
		testAircraft.setScore(0);
		testAircraft.alterPath(1, new Waypoint(65, 0, Waypoint.WaypointType.EXIT));
		assertTrue("Score = 0", testAircraft.score() == 0);
	}	

}
		
				
	
	