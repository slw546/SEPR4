package scn;

import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

/**
 * <h1>GameEnding</h1>
 * <p>Class that is run when the player leaves the shop. Represents the ending of the game</p>

 * @author Richard Kirby
 */

public class GameEnding extends Scene {
	
	/** Textbox used to display the ending information */
	private lib.TextBox textBox;
	
	/** The amount of beers that the player purchased from the shop */
	private int beerCount;
	
	/** The amount of food items the player purchased from the shop **/
	private int foodCount;
	
	/** whether the player purchased the bear plush from the shop or not **/
	private boolean hasBear;
	
	private int keyPressed;
	
	private double timer;
	
	/** Used for testing: value is associated with a specific drawn ending */
	private int currentEnding = 0;
	
	public GameEnding(Main main, int beerCount, int foodCount, boolean hasBear) {
		super(main);
		
		this.beerCount = beerCount;
		this.foodCount = foodCount;
		this.hasBear = hasBear;
	}
	
	@Override
	public void start() {
		timer = 0;
		textBox = new lib.TextBox(64, 96, window.width() - 128, window.height() - 96, 25);
		textBox.addText("You successfully ended the day without detection. The humans remain unaware of your true bear nature.");
		textBox.newline();
		textBox.delay(0.5);
		
		//Draw the bad ending of the game
		if (this.beerCount >= this.foodCount && !hasBear) {
			drawBadEnding();
		}
				
		//Draw the good ending of the game
		if (this.beerCount < this.foodCount && !hasBear) {
			drawGoodEnding();
		}
				
		//Draw the secret ending of the game
		if (hasBear) {
			drawSecretEnding();
		}

	}
	
	@Override
	public void update(double dt) {
		this.timer += dt;
		this.textBox.update(dt);
	}

	
	@Override
	public void draw() {
		graphics.setColour(0, 128, 0, 128);
		
		textBox.draw();
		
		int opacity = (int)(255 * Math.sin(timer));
		graphics.setColour(0, 128, 0, opacity);
		graphics.printCentred("Press any key to continue", 0, window.height() - 100, 1, window.width());
	}
	
	/**
	 * Draw the good ending of the game.
	 * This is for when the player purchased more food than alcohol from the shop
	 */
	public void drawGoodEnding() {
		this.currentEnding = 1; //Indicate that the good ending has occured
		
		if (!Main.testing) {
			graphics.setColour(0, 128, 0, 128);
		}
		
		textBox.addText("After a hard day at work and a brief visit at the local shop, you return home");
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("Having purchased enough food, you are able to feed your bear cubs Dimitry and Gustav");
		textBox.newline();
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("However, it won't be long before you have to risk your identity once more in order to support your family");
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("You relish this brief moment of happiness and comfort. Dimitry and Gustav play and you put up your feet to relax" );
		textBox.newline();
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("The end");
	}
	
	/**
	 * Draw the bad ending of the game.
	 * This is for when the player purchased more alcohol than food from the shop
	 */
	public void drawBadEnding() {
		this.currentEnding = 2; //Indicate that the bad ending has occured
		
		if (!Main.testing) {
			graphics.setColour(0, 128, 0, 128);
		}
		
		textBox.addText("You return home drunk");
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("Without enough food, you are only able to feed your bear cub Gustav");
		textBox.newline();
		textBox.newline();
		textBox.delay(2.0);
		textBox.addText("Tragically your youngest cub Dimitry passes away from malnutrition");
		textBox.newline();
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("Consequently, you fall into depression");
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("You never forgive yourself for what your alcohol problem caused" );
		textBox.newline();
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("The end");
	}
	
	/**
	 * Draw the secret ending of the game
	 * This is for when the player purchased the bear from the shop
	 */
	public void drawSecretEnding() {
		this.currentEnding = 3; //Indicate that the secret ending has occured
		
		if (!Main.testing) {
			graphics.setColour(0, 128, 0, 128);
		}
		
		textBox.addText("You return home");
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("You have no food and no beer");
		textBox.newline();
		textBox.delay(2.0);
		textBox.addText("You can't feed your alcohol problem nor your family");
		textBox.newline();
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("But hey...");
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("At least you have a bear plush......" );
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("It's one of a kind.");
		textBox.newline();
		textBox.newline();
		textBox.delay(1.5);
		textBox.addText("The end");
	}

	@Override
	public void keyPressed(int key) {
		this.keyPressed = key;	
	}
	
	@Override
	public void keyReleased(int key) {
		if (key == this.keyPressed) {
			main.closeScene();
			main.closeScene();
		}
	}
	
	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {
		if (key == input.MOUSE_LEFT) {
			main.closeScene();
			main.closeScene();
		}
	}
	
	public int getEnding() {
		return this.currentEnding;
	}
	
	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {}

}
