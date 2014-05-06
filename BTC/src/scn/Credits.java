package scn;

import java.io.File;

import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

public class Credits extends Scene {

	/**
	 * Default speed to scroll the credits
	 */
	private final static int SCROLL_SPEED = 64;
	
	private final static int SCROLL_LENGTH = 3000;

	private float speed;

	/**
	 * The position to print the credits text at. Initially offscreen
	 */
	private double scrollPosition;

	/**
	 * Music to play during the credits
	 */
	private audio.Music music;

	/**
	 * Constructor
	 * @param main the main containing the scene
	 */
	public Credits(Main main) {
		super(main);
	}

	/**
	 * INPUT HANDLERS
	 */
	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {}

	@Override
	public void keyPressed(int key) {}

	@Override
	/**
	 * exit to the title screen if escape is pressed
	 */
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
	}

	/**
	 * Init musis, and the credits text to be offscreen
	 */
	@Override
	public void start() {
		speed = 1f;
		scrollPosition = -window.height();
		
		if (!Main.testing) { 
			music = audio.newMusic("sfx" + File.separator + "piano.ogg");
			music.play();
		}
	}

	@Override
	/**
	 * update the credits's scroll position
	 * hurry the credits movement if certain keys pressed
	 */
	public void update(double dt) {
		boolean hurried = input.isKeyDown(input.KEY_SPACE) || input.isMouseDown(input.MOUSE_LEFT);
		speed = hurried ? 4f : 1f;
		scrollPosition += SCROLL_SPEED * dt * speed;
		if (scrollPosition > SCROLL_LENGTH) scrollPosition = -window.height();
	}

	@Override
	/**
	 * print the credits based on the current scroll position
	 */
	public void draw() {
		int gap = 64;
		int currentHeight = 0;
		graphics.setColour(0, 128, 0);
		graphics.push();
		graphics.translate(0, scrollPosition);
		currentHeight += gap;
		graphics.printCentred("Bear Traffic Controller", 0, currentHeight, 3, window.width());
		currentHeight += gap * 2;

		graphics.printCentred("Internal Infrastructure Strategist", 0, currentHeight, 2, window.width());
		graphics.printCentred("__________________________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("_________________________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Josh Adams", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;

		graphics.printCentred("Director of Marketing", 0, currentHeight, 2, window.width());
		graphics.printCentred("_____________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("____________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Gareth Handley", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Project Director", 0, currentHeight, 2, window.width());
		graphics.printCentred("_______________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("______________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Sanjit Samaddar", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Senior Functionality Analyst", 0, currentHeight, 2, window.width());
		graphics.printCentred("____________________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("___________________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Alex Stewart", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Principal Solutions Facilitator", 0, currentHeight, 2, window.width());
		graphics.printCentred("_______________________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("______________________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Huw Taylor", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Creative Consultant for Competative Computer Communiucations", 0, currentHeight, 2, window.width());
		graphics.printCentred("____________________________________________________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("___________________________________________________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Stephen Webb", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Music", 0, currentHeight, 2, window.width());
		graphics.printCentred("_____", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("____", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Gypsy Shoegazer", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Beep SFX", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap / 2;
		graphics.printCentred("Kevin MacLeod", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Partners in Rhyme", 2*window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("FreeSound", window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap * 2;

		graphics.printCentred("External Libraries", 0, currentHeight, 2, window.width());
		graphics.printCentred("__________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("_________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("LWJGL", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Slick2D", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("JOG", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap * 2;

		currentHeight += gap * 4;
		
		graphics.printCentred("Special Thanks", 0, currentHeight, 2, window.width());
		graphics.printCentred("______________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("_____________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("SEPR Team GOA", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("for their modifications in stage 3.", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		graphics.printCentred("http://www.bullshitjob.com/title/", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("for their consultation in internal role development.", 0, currentHeight, 2, window.width());
		
		currentHeight += gap * 4;
		
		graphics.printCentred("Very few bears were harmed in", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("the making of this game.", 0, currentHeight, 2, window.width());
		currentHeight += gap * 2;
		
		currentHeight += gap * 4;
		graphics.printCentred("Bear Traffic Controller", 0, currentHeight, 2, window.width());
		graphics.pop();
	}

	@Override
	public void close() {
		if (!Main.testing) {
			music.stop();
		}
	}

	@Override
	public void playSound(Sound sound) {}

}
