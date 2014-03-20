package tst;

import static org.junit.Assert.*;

import org.junit.Test;

import cls.Waypoint;

import cls.Vector;

public class WaypointTest {

	// Test Get Functions
	// Test get position function
	@Test
	public void testGetPosition() {
		Waypoint testWaypoint = new Waypoint(10, 10, Waypoint.WaypointType.AIRSPACE);
		Vector resultVector = testWaypoint.position();
		assertTrue("Position = (10, 10, 0)", (10 == resultVector.x()) && (10 == resultVector.y()) && (0 == resultVector.z()));
	}
	
	// Test isEntryOrExit function
	@Test
	public void testIsEntryOrExit() {
		Waypoint testWaypoint = new Waypoint(10, 10, Waypoint.WaypointType.AIRSPACE);
		assertTrue("Entry/Exit = false", Waypoint.WaypointType.AIRSPACE == testWaypoint.type());
	}
	
	@Test
	public void testIsEntryOrExit2() {
		Waypoint testWaypoint = new Waypoint(0, 0, Waypoint.WaypointType.AIRSPACE);
		assertTrue("Entry = true", Waypoint.WaypointType.ENTRY == testWaypoint.type());
		assertTrue("Exit = true", Waypoint.WaypointType.EXIT == testWaypoint.type());
	}
	
	// Test mouseOver checking
	@Test
	public void testIsMouseOver(){
		Waypoint testWaypoint = new Waypoint(5,5, Waypoint.WaypointType.AIRSPACE);
		assertTrue("Mouse over = true", true == testWaypoint.isMouseOver(10,10));
	}
	@Test
	public void testIsMouseOver2(){
		Waypoint testWaypoint = new Waypoint(25,25, Waypoint.WaypointType.AIRSPACE);
		assertTrue("Mouse over = false", false == testWaypoint.isMouseOver(10,10));
	}
	
	// Test getCost function
	@Test
	public void testGetCost(){
		Waypoint testWaypoint = new Waypoint(2, 4, Waypoint.WaypointType.AIRSPACE);
		Waypoint testWaypoint2 = new Waypoint(2, 2, Waypoint.WaypointType.AIRSPACE);
		double result = testWaypoint.getCost(testWaypoint2);
		assertTrue("Cost = 2", 2 == result);
	}@Test
	public void testGetCost2(){
		Waypoint testWaypoint = new Waypoint(6, 15, Waypoint.WaypointType.AIRSPACE);
		Waypoint testWaypoint2 = new Waypoint(15, 15, Waypoint.WaypointType.AIRSPACE);
		double result = testWaypoint.getCost(testWaypoint2);
		assertTrue("Cost = 9", 9 == result);
	}
	
	// Test getCostBetween function
	@Test
	public void testGetCostBetween(){
		Waypoint testWaypoint = new Waypoint(2, 4, Waypoint.WaypointType.AIRSPACE);
		Waypoint testWaypoint2 = new Waypoint(2, 2, Waypoint.WaypointType.AIRSPACE);
		double result = Waypoint.getCostBetween(testWaypoint, testWaypoint2);
		assertTrue("Cost = 2", 2 == result);
	}@Test
	public void testGetCostBetween2(){
		Waypoint testWaypoint = new Waypoint(6, 15, Waypoint.WaypointType.AIRSPACE);
		Waypoint testWaypoint2 = new Waypoint(15, 15, Waypoint.WaypointType.AIRSPACE);
		double result = Waypoint.getCostBetween(testWaypoint, testWaypoint2);
		assertTrue("Cost = 9", 9 == result);
	}
	
	
}
