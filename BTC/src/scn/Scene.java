package scn;

import java.io.File;

import lib.jog.audio;

public abstract class Scene implements lib.jog.input.EventHandler {
	
	protected lib.jog.graphics.Image monitorImage;
	
	protected btc.Main main;
	
	/**
	 * Top level Constructor for a scene
	 * @param main the main class holding the scene, ie the running instance of the game
	 */
	protected Scene(btc.Main main) {
		this.main = main;
		monitorImage = lib.jog.graphics.newImage("gfx" + File.separator + "monitor.png");
	}
	
	/**
	 * Handles initialisation of a scene. Runs once only at the start of the scene
	 */
	abstract public void start();
	
	/**
	 * Handles updates of all objects requiring updates in the scene
	 * Runs regularly when called by Main
	 * @param dt the delta time since the last update was carried out
	 */
	abstract public void update(double dt);
	
	/**
	 * Handles drawing of all drawable objects in the scene to the window
	 * Runs regularly when called by main
	 */
	public void draw() {
		lib.jog.graphics.setColour(255, 255, 255);
		lib.jog.graphics.draw(monitorImage, 0, 0);
	}
	
	/**
	 * Used to cleanly exit a scene, e.g. halting the scene's music so it does not overlap the next scene's music
	 * Runs once only when a scene is closed
	 */
	abstract public void close();
	
	/**
	 * Plays a sound effect
	 * Can be used by a scene's object to request a sound to be played, e.g. aircraft can request a warning beep
	 * when their separation radius is infringed.
	 * @param sound the sound to be played
	 */
	abstract public void playSound(audio.Sound sound);
	
}
