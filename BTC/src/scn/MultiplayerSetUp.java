package scn;

import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;
import btc.Main;

public class MultiplayerSetUp extends Scene {
	
	
	//Places to draw things
	private final int HOST_BUTTON_W = 128;
	private final int HOST_BUTTON_H = 16;
	private final int HOST_BUTTON_X = window.width() / 2;
	private final int HOST_BUTTON_Y = window.height() / 2;
	
	//Textbox for flavour text about chosen game mode.
	private lib.TextBox textBox;

	//list of buttons
	private lib.ButtonText[] buttons;
	
	//flags
	private boolean host_pressed = false;

	protected MultiplayerSetUp(Main main) {
		super(main);
		//Textbox to write flavour text and instructions to.
		textBox = new lib.TextBox(128, 96, window.width() - 256, window.height() - 96, 32);
	}

	@Override
	public void mousePressed(int key, int x, int y) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		for (lib.ButtonText b : buttons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
	}

	@Override
	public void keyPressed(int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
	}

	@Override
	public void start() {
		//Text for the textbox to write to the screen
		textBox.addText("You are working in a busier workspace with other ACTOs.");
		textBox.addText("The risk is greater, but so to is the reward.");
		textBox.addText("Success could buy some comfort in the harsh winter months ahead, for you and your family.");
		textBox.addText("There are rumours that one of the controllers is very hairy with a gruff voice.");
		textBox.addText("Do not trust the others. If the humans discover your secret, you will but put in a zoo.");
		textBox.delay(0.5);
		textBox.addText("Or worse..");
		
		//Initialise buttons
		buttons = new lib.ButtonText[1];
		
		lib.ButtonText.Action hostGame = new lib.ButtonText.Action() {
			@Override
			public void action() {
				host_pressed = true;
			}
		};
		
		buttons[0] = new lib.ButtonText("Host LAN Game", hostGame, HOST_BUTTON_X,
				HOST_BUTTON_Y, HOST_BUTTON_W, HOST_BUTTON_H, 8, 6);
		
	}

	@Override
	public void update(double dt) {
		//update the textbox
		textBox.update(dt);
		
	}

	@Override
	public void draw() {
		//draw the textbox
		textBox.draw();
		//draw the buttons
		for (lib.ButtonText b : buttons) {
			b.draw();
		}
	}

	@Override
	public void close() {
	}

	@Override
	public void playSound(Sound sound) {
	}

}
