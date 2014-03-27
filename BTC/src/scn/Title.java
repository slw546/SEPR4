package scn;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import lib.ButtonText;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.window;
import btc.Main;

public class Title extends Scene {

	/**
	 * A URL to the Bear Traffic Controller Website
	 * Webpage contains explanation of game's controls and goal
	 */
	private final static String HELP_URL = "https://docs.google.com/document/d/1dfYelEmZgKHHfhHCVX3ipiUNdfW-zlbzc0BSpwG8Rvs/edit?usp=sharing";

	/**
	 * The 'beep' played as the radar makes a sweep
	 */
	private audio.Sound beep;

	/**
	 * A List of buttons, to hold declared buttons in the scene
	 */
	private lib.ButtonText[] buttons;

	/**
	 * Holds the angle to draw the radar sweep at.
	 * Also used to play the beep sound as the radar sweeps the BTC title string
	 * Updated regularly during Title's update()
	 */
	private double angle;

	/**
	 * Constructor for the Title Scene
	 * @param main the main holding the scene
	 */
	public Title(Main main) {
		super(main);
	}
	/**
	 * Initialises anything which needs to be initialised, such as buttons and sound effects
	 * Runs only at start of scene
	 */
	@Override
	public void start() {
		if (!Main.testing) {
			beep = audio.newSoundEffect("sfx" + File.separator + "beep.ogg");
			beep.setVolume(0.2f);
		}
		
		buttons = new lib.ButtonText[7];
		
		// Temporary Multiplayer Game button
		lib.ButtonText.Action runMPGame = new lib.ButtonText.Action() {
			
			@Override
			public void action() {
				//main.setScene(new MultiplayerGame(main, MultiplayerGame.Type.HOST));
			}
		};

		
		// Multiplayer Set up button
		lib.ButtonText.Action runMPSetup = new lib.ButtonText.Action() {
			
			@Override
			public void action() {
				main.setScene(new MultiplayerSetUp(main));
			}
		};

		// Start Game button (assessment 3 game)
		lib.ButtonText.Action runGame = new lib.ButtonText.Action() {
			@Override
			public void action() {
				main.setScene(new DifficultySelect(main, DifficultySelect.CREATE_MAIN));
			}
		};
		
		//Demo button (original game)
		lib.ButtonText.Action runDemo = new lib.ButtonText.Action() {
			@Override
			public void action() {
				main.setScene(new DifficultySelect(main, DifficultySelect.CREATE_DEMO));
			}
		};

		buttons[0] = new lib.ButtonText("Play Game", runGame, window.height(),
				window.height()/2 + 96, window.width() - window.height(), 24, 8, 6);
		
		
		buttons[1] = new lib.ButtonText("Play Demo", runDemo, window.height(),
				window.height()/2 + 126, window.width() - window.height(), 24, 8, 6);
		
		buttons[5] = new lib.ButtonText("Play Multiplayer", runMPSetup, window.height(),
				window.height()/2 + 66, window.width() - window.height(), 24, 8, 6);
		
		buttons[6] = new lib.ButtonText("Play Multiplayer Game (Temp)", runMPGame, window.height(),
				window.height()/2 + 36, window.width() - window.height(), 24, 8, 6);
		buttons[6].setAvailability(false);
		
		/* Game Button
		lib.ButtonText.Action play = new lib.ButtonText.Action() {
			@Override
			public void action() {
				_main.setScene(new Game(main));
			}
		};

		buttons[1] = new lib.ButtonText("Play Full Game", play, window.height(),
				window.height()/2 + 126, window.width() - window.height(), 24, 8, 6);
		buttons[1].setAvailability(false);*/

		// Credits Button
		lib.ButtonText.Action credits = new lib.ButtonText.Action() {
			@Override
			public void action() {
				main.setScene(new Credits(main));
			}
		};

		buttons[2] = new lib.ButtonText("Credits", credits, window.height(),
				window.height()/2 + 156, window.width() - window.height(), 24, 8, 6);

		// Help Button
		lib.ButtonText.Action help = new lib.ButtonText.Action() {
			@Override
			public void action() {
				try {
					Desktop.getDesktop().browse(new URI(HELP_URL));
				} catch (IOException e) {
					e.printStackTrace();
				}
				catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		};

		buttons[3] = new lib.ButtonText("Help      (Opens in Browser)",
				help, window.height(), window.height()/2 + 186,
				window.width() - window.height(), 24, 8, 6);
		
		// Exit Button
		lib.ButtonText.Action exit = new lib.ButtonText.Action() {
			@Override
			public void action() {
				main.quit();
			}
		};
		
		buttons[4] = new lib.ButtonText("Exit", exit, window.height(), window.height()/2 + 216,
				window.width() - window.height(), 24, 8, 6);

		angle = 0;
	}

	/**
	 * Updates all objects in the title scene
	 * Called by Main class
	 * @param dt the delta time since the last update
	 */
	@Override
	public void update(double dt) {
		angle += dt * (3d / 4d); //increase the angle of the radar sweep

		//Check the angle of the radar sweep;
		//If approaching the BTC title string, play the beep
		double beepTimer = (angle * 4) + (Math.PI * 4 / 5); 
		beepTimer %= (2 * Math.PI);
		if ( beepTimer <= 0.1 ) {
			playSound(beep);
		}
	}

	/**
	 * Handles mouse down input
	 * Unused
	 */
	@Override
	public void mousePressed(int key, int x, int y) {}

	/**
	 * Handles mouse release events
	 * Causes a button to act if clicked by any mouse key
	 */
	@Override
	public void mouseReleased(int key, int mx, int my) {
		for (lib.ButtonText b : buttons) {
			if (b != null) {
				if (b.isMouseOver(mx, my)) {
					b.act();
				}
			}
		}

	}

	/**
	 * Keyboard input methods
	 * Unused - no keyboard interaction in this scene
	 */
	@Override
	public void keyPressed(int key) {}

	@Override
	public void keyReleased(int key) {}

	/**
	 * Handles drawing of the scene
	 * Calls drawRadar() and drawMenu() to draw elements of the scene
	 * Called regularly by Main
	 */
	@Override
	public void draw() {
		if (!Main.testing) {
			drawRadar();
			drawMenu();
		}
	}
	/**
	 * Draws the radar arc and title string
	 */
	private void drawRadar() {
		// Radar
		// set of circles for radar 'screen'
		graphics.setColour(0, 128, 0);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/2 - 32, 100);
		graphics.setColour(0, 128, 0, 32);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/3, 100);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/4 - 16, 100);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/9, 100);
		graphics.circle(false, window.height()/2, window.height()/2, 2, 100);
		graphics.setColour(0, 128, 0);
		// sweep of radar
		double radarAngle = (angle * 4) % (2 * Math.PI);
		int w = (int)( Math.cos(radarAngle) * (window.height()/2 - 32) );
		int h = (int)( Math.sin(radarAngle) * (window.height()/2 - 32) );
		graphics.line(window.height()/2, window.height()/2, window.height()/2 + w, window.height()/2 + h);
		graphics.setColour(0, 128, 0, 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -8 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -7 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -6 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -5 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -4 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -3 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -2 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -1 * Math.PI / 8);

		// Title
		String title = "Bare Traffic Controller";
		int titleLength = title.length();
		// fades title string's characters over time
		// characters brighten when the sweep passes over them
		double a = radarAngle + (Math.PI * 4 / 5);
		for (int i = 0; i < titleLength; i++) {
			a -= Math.PI / 32;
			double opacity = a %= (2 * Math.PI);
			opacity *= 256 / (2 * Math.PI);
			opacity = 256 - opacity;
			opacity %= 256;
			graphics.setColour(0, 128, 0, opacity);
			int xPos = (window.height() / 2) - ((titleLength * 14) / 2);
			int yPos = (window.height() / 3);
			graphics.print(title.substring(i, i+1), xPos + i * 14, yPos, 1.8);
		}
	}

	/**
	 * Draws menu boxes, boxes around buttons, and so on
	 */
	private void drawMenu() {
		// Draw Extras e.g. Date, Time, Credits
		graphics.setColour(0, 128, 0);
		graphics.line(window.height(), 16, window.height(), window.height() - 16);
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
		java.text.DateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
		java.util.Date date = new java.util.Date();
		graphics.print(dateFormat.format(date), window.height() + 8, 20);
		graphics.print(timeFormat.format(date), window.height() + 8, 36);
		graphics.line(window.height(), 48, window.width() - 16, 48);
		graphics.print("ORIGINAL CREATORS:", window.height() + 8, 56);
		graphics.print("TEAM FLR", window.height() + 8, 72);
		graphics.print("PRESENTED BY:", window.height() + 8, 88);
		graphics.print("TEAM GOA", window.height() + 8, 104);

		// Draw Buttons
		for (lib.ButtonText b : buttons) {
			if(b != null) {
				b.draw();
			}
		}

		graphics.setColour(0, 128, 0);
		graphics.line(window.height(), window.height()/2 + 30, window.width() - 16, window.height()/2 + 30);
		graphics.line(window.height(), window.height()/2 + 60, window.width() - 16, window.height()/2 + 60);
		graphics.line(window.height(), window.height()/2 + 90, window.width() - 16, window.height()/2 + 90);
		graphics.line(window.height(), window.height()/2 + 120, window.width() - 16, window.height()/2 + 120);
		graphics.line(window.height(), window.height()/2 + 150, window.width() - 16, window.height()/2 + 150);
		graphics.line(window.height(), window.height()/2 + 180, window.width() - 16, window.height()/2 + 180);
		graphics.line(window.height(), window.height()/2 + 210, window.width() - 16, window.height()/2 + 210);
		graphics.line(window.height(), window.height()/2 + 240, window.width() - 16, window.height()/2 + 240);
	}

	@Override
	/**
	 * cleanly exits the title scene
	 */
	public void close() {}

	@Override
	/**
	 * Plays a requested sound
	 */
	public void playSound(Sound sound) {
		if (!Main.testing) {
			sound.stop();
			sound.play();
		}
	}

}
