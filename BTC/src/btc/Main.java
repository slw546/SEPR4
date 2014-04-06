package btc;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;

import org.lwjgl.Sys;

import scn.Scene;
import lib.jog.*;

/**
 * <h1>Main</h1>
 * <p>Main class that is run when file is run. Main handles the scenes (gamestates).</p>
 * @author Huw Taylor
 */
public class Main implements input.EventHandler {

	/**
	 * Is the game currently being tested
	 */
	public static boolean testing = true;
	
	/**
	 * Creates a new instance of Main, starting a new game.
	 * @param args any command-line arguments.
	 */
	public static void main(String[] args) {
		new Main(false);
	}
	
	final private String TITLE = "Bare Traffic Controller";
	final public static double TARGET_WIDTH = 1280;
	final public static double TARGET_HEIGHT = 960;
	private static double width;
	private static double height;
	
	final private String[] ICONS = {
		"gfx" + File.separator + "icon16.png", // 16
		"gfx" + File.separator + "icon32.png", // 32
		"gfx" + File.separator + "icon64.png", // 64
	};

	private double lastFrameTime;
	private double dt;
	private java.util.Stack<scn.Scene> sceneStack;
	private scn.Scene currentScene;
	private int fps;
	private long lastfps;
	
	/**
	 * Constructor for Main. Initialises the jog library classes, and then
	 * begins the game loop, calculating time between frames, and then when
	 * the window is closed it releases resources and closes the programme
	 * successfully.
	 */
	public Main(boolean testing) {
		// Set testing status
		Main.testing = testing;
		
		double scale = 1;
		
		if (!testing) {
			// Resize window if necessary
			Rectangle windowBounds = GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.getMaximumWindowBounds();

			int actualWidth = windowBounds.width;
			int actualHeight = windowBounds.height;

			if (((actualWidth - 50) < TARGET_WIDTH)
					|| ((actualHeight - 50) < TARGET_HEIGHT)) {

				scale = Math.min((actualWidth - 50) / TARGET_WIDTH,
						(actualHeight - 50) / TARGET_HEIGHT);
			}
		} else {
			scale = 1;
		}

		width = scale * TARGET_WIDTH;
		height = scale * TARGET_HEIGHT;
		
		start();
		
		while(!window.isClosed()) {
			dt = getDeltaTime();
			update(dt);
			if (!testing) {
				draw();
			}
		}
		
		quit();
	}
	
	/**
	 * Creates window, initialises jog classes and sets starting values to variables.
	 */
	private void start() {
		lastFrameTime = (double)(Sys.getTime()) / Sys.getTimerResolution();

		if (!testing) {
			window.setIcon(ICONS);
			window.initialise(TITLE, (int)(width), (int)(height), 60, window.WindowMode.WINDOWED);
			System.out.println("Window Dimensions: " + window.width() + " by " + window.height() + ".");
			graphics.initialise();
			graphics.Font font = graphics.newBitmapFont("gfx" + File.separator
					+ "font.png", ("ABCDEFGHIJKLMNOPQRSTUVWXYZ " +
							"abcdefghijklmnopqrstuvwxyz1234567890.,_-!?()[]><#~:;/\\^'\"{}+=@@@@@@@@`"));
			graphics.setFont(font);
		}
		sceneStack = new java.util.Stack<scn.Scene>();
		setScene(new scn.Title(this));
		lastfps = ((Sys.getTime()* 1000) / Sys.getTimerResolution()); //set lastFPS to current Time
	}
	
	/**
	 * Updates input handling, the window and the current scene.
	 * @param dt the time elapsed since the last frame.
	 */
	private void update(double dt) {
		if (!testing) {
			audio.update();
			input.update(this);
		}
		
		updateFPS();
		
		if (!testing) {
			window.update();
		}
		
		currentScene.update(dt);
	}
	
	/**
	 * Calculates the time taken since the last tick in seconds as a double-precision floating point number.
	 * @return the time in seconds since the last frame.
	 */
	private double getDeltaTime() {
		double time = (double)(Sys.getTime()) / Sys.getTimerResolution();
	    double delta = (time - lastFrameTime);
	    lastFrameTime = time;
	    return delta;
	}
	
	/**
	 * Clears the graphical viewport and calls the draw function of the current scene.
	 */
	private void draw() {
		if (!testing) {
			graphics.clear();
			currentScene.draw();
		}
	}
	
	/**
	 * Closes the current scene, releases the audio resources and closes the window.
	 */
	public void quit() {
		currentScene.close();
		
		if (!testing) {
			window.dispose();
			audio.dispose();
		}
		
		System.exit(0);
	}
	
	/**
	 * 
	 * @param newScene
	 */
	public void setScene(scn.Scene newScene) {
		if (currentScene != null) currentScene.close();
		sceneStack.push(newScene);
		currentScene = sceneStack.peek();
		currentScene.start();
	}

	/**
	 * Closes the current scene and pops it from the stack.
	 */
	public void closeScene() {
		currentScene.close();
		sceneStack.pop();
		currentScene = sceneStack.peek();
	}
	
	/** 
	 * Updates the fps
	 */
	public void updateFPS()
	{
		long time = ((Sys.getTime()* 1000) / Sys.getTimerResolution()); //set lastFPS to current Time
		
		if (time - lastfps > 1000) {
			if (!testing) {
				window.setTitle("Bare Traffic Controller - FPS: " + fps);
			}
			
			fps = 0; //reset the FPS counter
			lastfps += 1000; //add one second
		}
		
		fps++;
	}
	
	public static double width() {
		return width;
	}
	
	public static double height() {
		return height;
	}
	
	public static double getScale() {
		return width / TARGET_WIDTH;
	}

	@Override
	public void mousePressed(int key, int x, int y) {
		currentScene.mousePressed(key, x, y);
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		currentScene.mouseReleased(key, x, y);
	}

	@Override
	public void keyPressed(int key) {
		currentScene.keyPressed(key);
	}

	@Override
	public void keyReleased(int key) {
		currentScene.keyReleased(key);
	}
	
	public Scene getCurrentScene(){
		return this.currentScene;
	}

}
