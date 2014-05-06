package scn;

import thr.NetworkThread;
import cls.Vector;
import lib.SpriteAnimation;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.window;
import btc.Main;

public class MultiGameOver extends Scene {
	/**
	 * Text box to write the details of the game failure
	 */
	private lib.TextBox textBox;
	
	/**
	 * Whether or not the player won.
	 */
	private int winner = 0;
	
	/**
	 * The two crashed aircraft, passed to the scene by the scene
	 * in which they crashed. Used to position the explosion, and
	 * provide graphical feedback of how and where the player failed.
	 */
	//private Aircraft crashedAircraft1;
	//private Aircraft crashedAircraft2;
	private int finalScore;
	
	private MultiplayerSetUp lobby;
	private NetworkThread networkThread;
	
	/**
	 * A random number of deaths caused by the crash
	 */
	private int deaths;
	
	/**
	 * The position of the crash - the vector midpoint of the positions
	 * of the two crashed aircraft
	 */
	private Vector crash;
	/**
	 * A sprite animation to handle the frame by frame drawing of the explosion
	 */
	private SpriteAnimation explosionAnim;
	/**
	 * The explosion image to use for the animation
	 */
	private Image explosion;
	
	private int keyPressed;
	
	/**
	 * Timer to allow for explosion and aircraft to be shown for a period, followed by the text box.
	 */
	private double timer;
	
	/**
	 * Constructor for the Game Over scene
	 * @param main the main containing the scene
	 * @param aircraft1 one of the aircraft involved in the crash
	 * @param aircraft2 the second aircraft involved in the crash
	 */
	public MultiGameOver(Main main,int score, int winner, MultiplayerSetUp lobby, NetworkThread netThread) {
		super(main);
		//crashedAircraft1 = aircraft1;
		//crashedAircraft2 = aircraft2;
		finalScore = score;
		//crash = new Vector(aircraft1.position().x(), aircraft2.position().y(), 0);
		int framesAcross = 8;
		int framesDown = 4;
		this.networkThread = netThread;
		this.lobby = lobby;
	}
	
	/**
	 * Initialises the random number of deaths, timer,
	 * and text box with strings to be written about the game failure
	 */
	@Override
	public void start() {
		/*if (!Main.testing) {
			playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		}*/
		switch (winner){
		case 0:
			deaths = (int)( Math.random() * 500) + 300;
			timer = 0;
			textBox = new lib.TextBox(64, 96, window.width() - 128, window.height() - 96, 32);
			textBox.addText("Your airspace has been taken over");
			textBox.delay(0.4);
			textBox.addText("It seems the other ATCO has succeeded in controlling and landing more planes than you");
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("The inquery into your incompetance will lead to humanity discovering your true bear nature.");
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Your failure to pass as a human, will gnaw at you and you will have to revert to your drinking problem to attempt to cope.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("You earned `" + String.valueOf(finalScore) + ", a pitiful amount that will barely feed your family for " + String.valueOf((finalScore/20)) + " days.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("With no income, there is no way your family can survive the fast-approaching winter months.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Your wife, Kseniya, leaves you, taking your children Dmitriy and Gustav with her. You spiral into decline until your problems drive you to take up a new, false identity, that you might once again direct air traffic.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Game Over.");
			break;
		case 1:
			deaths = (int)( Math.random() * 500) + 300;
			timer = 0;
			textBox = new lib.TextBox(64, 96, window.width() - 128, window.height() - 96, 32);
			textBox.addText("You took over the opposing airspace");
			textBox.delay(0.4);
			textBox.addText("It seems you succeeded in controlling and landing more planes than the other ACTO");
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("You have been commended for your competance and offered a promotion");
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Your success passing as a human will improve the quality of you and your family's life");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("You earned `" + String.valueOf(finalScore) + ". Your promotion will help feed your family for " + String.valueOf((finalScore*20)) + " days.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("You will be comfortable in the fast approaching winter months.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Keep up the good work, ACTO.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Game Over.");
			break;
		case 2:
			deaths = (int)( Math.random() * 500) + 300;
			timer = 0;
			textBox = new lib.TextBox(64, 96, window.width() - 128, window.height() - 96, 32);
			textBox.addText("You somehow drew with the opponent ACTO.");
			textBox.delay(0.4);
			textBox.addText("It seems neither of you succeeded in controlling and landing more planes than the other ACTO");
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("The airport expects better results. You are at risk of losing your job.");
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Your failure to pass as a human, will gnaw at you and you will have to revert to your drinking problem to attempt to cope.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("You earned `" + String.valueOf(finalScore) + ", a pitiful amount that will barely feed your family for " + String.valueOf((finalScore/20)) + " days.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("If you lose your job, there is no way your family can survive the fast-approaching winter months.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Your wife, Kseniya, will leave you, taking your children Dmitriy and Gustav with her. You will spiral into decline until your problems drive you to take up a new, false identity, that you might once again direct air traffic.");
			textBox.newline();
			textBox.newline();
			textBox.delay(0.8);
			textBox.addText("Game Over.");
			break;
		}
		
	}

	@Override
	/**
	 * If before explosion has finished, update the explosion
	 * otherwise, update text box instead
	 */
	public void update(double dt) {
			timer += dt;
			textBox.update(dt);
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {
		main.closeScene();
    	lobby.setNetworkState(MultiplayerSetUp.networkStates.NO_CONNECTION);
    	networkThread.endGame();
	}

	@Override
	/**
	 * Tracks if any keys are pressed when the game over screen begins
	 * Prevents the scene instantly ending due to a key press from previous scene
	 */
	public void keyPressed(int key) {
		keyPressed = key;
	}

	/**
	 * Ends the scene if any key is released , ie. press any key to continue
	 */
	@Override
	public void keyReleased(int key) {
		if (key == keyPressed) {
	    	lobby.setNetworkState(MultiplayerSetUp.networkStates.NO_CONNECTION);
	    	networkThread.endGame();
	    	main.closeScene();
		}
	}

	@Override
	/**
	 * Draws game over
	 * If explosion has finished, draw the textbox
	 * Otherwise, draw the aircraft and explosion
	 */
	public void draw() {
	/*	graphics.setColour(0, 128, 0);
		graphics.printCentred(crashedAircraft1.name() + 
				" crashed into " + crashedAircraft2.name() + ".", 0, 32, 2, window.width());
		if (explosionAnim.hasFinished()) {
			textBox.draw();
		} else {
			crashedAircraft1.draw((int) crashedAircraft1.position().z());
			crashedAircraft2.draw((int) crashedAircraft1.position().z());
			Vector midPoint = crash.add(crashedAircraft2.position()).scaleBy(0.5);
			double radius = 20;
			graphics.setColour(128,0,0);
			graphics.circle(false, midPoint.x(), midPoint.y(), radius);
			explosionAnim.draw();
		}*/
		textBox.draw();
		int opacity = (int)(255 * Math.sin(timer));
		graphics.setColour(0, 128, 0, opacity);
		graphics.printCentred("Press any key to continue", 0, window.height() - 100, 1, window.width());
	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}