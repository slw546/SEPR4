package tst;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Causes all tests to run.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AircraftTest.class,
						VectorTest.class,
						WaypointTest.class,
						AirportTest.class,
						ScoreTest.class
						})
public class AllTests {
	// Runs all tests
}
