package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scn.GameEnding;

public class GameEndingTest {
	
	/** An instance of GameEnding */
	GameEnding gameEndingTest;
	
	/**
	 * Initialise/set any variables needed for the tests to follow
	 */
	@Before
	public void beforeTests() {
		this.gameEndingTest = new GameEnding(null, 2, 5, false);
		
		this.gameEndingTest.start();
	}
	
	/**
	 * Test that the good ending to the game is drawn
	 * if the player purchased more food than beers from the Shop
	 */
	@Test
	public void testGoodEnding() {
		this.gameEndingTest = new GameEnding(null, 2, 5, false);
		this.gameEndingTest.start();
		assertTrue("Failed to draw good ending", this.gameEndingTest.getEnding() == 1);
	}
	
	/**
	 * Test that the bad ending to the game is drawn
	 * if the player purchased more beers than food from the Shop
	 */
	@Test
	public void testBadEnding() {
		this.gameEndingTest = new GameEnding(null, 5, 2, false);
		this.gameEndingTest.start();
		assertTrue("Failed to draw bad ending", this.gameEndingTest.getEnding() == 2);
	}
	
	/**
	 * Test that the secret ending to the game is drawn
	 * if the player purchased the bear plush from the shop
	 */
	@Test
	public void testSecretEnding() {
		this.gameEndingTest = new GameEnding(null, 2, 5, true);
		this.gameEndingTest.start();
		assertTrue("Failed to draw secret ending", this.gameEndingTest.getEnding() == 3);
	}
}
