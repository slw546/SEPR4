package scn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import thr.Client;
import thr.ClientThread;
import thr.Host;
import thr.HostThread;
import lib.ButtonText;
import lib.jog.graphics;
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
	
	private final int CLIENT_BUTTON_W = 128;
	private final int CLIENT_BUTTON_H = 16;
	private final int CLIENT_BUTTON_X = window.width() /2;
	private final int CLIENT_BUTTON_Y = window.height() / 2 + 30;
	
	private final int START_BUTTON_W = 128;
	private final int START_BUTTON_H = 16;
	private final int START_BUTTON_X = window.width() /2;
	private final int START_BUTTON_Y = window.height() / 2 + 60;
	
	private HostThread host;
	private ClientThread client;
	
	//Textbox for flavour text about chosen game mode.
	private lib.TextBox textBox;
	
	//Textfield for text input

	//list of buttons
	private lib.ButtonText[] buttons;
	
	//flags
	private boolean host_active = false;
	private boolean client_active = false;
	
	private boolean connection_established = false;

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
		buttons = new lib.ButtonText[3];
		
		lib.ButtonText.Action hostGame = new lib.ButtonText.Action() {
			@Override
			public void action() {
				createHost();
			}
		};
		
		lib.ButtonText.Action startClient = new lib.ButtonText.Action(){
			@Override
			public void action() {
				createClient();
				}
		};
		
		lib.ButtonText.Action startGame = new lib.ButtonText.Action(){
			@Override
			public void action() {
				MultiplayerGame game = new MultiplayerGame(main);
				
				if (host_active){
					host.setGameScene(game);
					host.setPlaying(true);
				}
				
				main.setScene(game);
			}
		};
		
		buttons[0] = new lib.ButtonText("Host LAN Game", hostGame, HOST_BUTTON_X,
				HOST_BUTTON_Y, HOST_BUTTON_W, HOST_BUTTON_H, 8, 6);
		
		buttons[1] = new lib.ButtonText("Start Client", startClient, CLIENT_BUTTON_X, 
				CLIENT_BUTTON_Y, CLIENT_BUTTON_W, CLIENT_BUTTON_H, 8, 6);
		
		buttons[2] = new lib.ButtonText("Start Game", startGame, START_BUTTON_X,
				START_BUTTON_Y, START_BUTTON_W, START_BUTTON_H, 8, 6);
		
		buttons[2].setAvailability(false);
		
	}
	
	private void createHost(){
		//Hide buttons
		buttons[0].setAvailability(false);
		buttons[1].setAvailability(false);
		buttons[2].setAvailability(true);
		//Create and start a host thread
		host = new HostThread(4445, this);
		host.start();
		host_active = true;
	}
	
	private void createClient(){
		//hide buttons
		buttons[0].setAvailability(false);
		buttons[1].setAvailability(false);
		//create and start a hosting thread
		client = new ClientThread("localhost", 4445, this);
		client.start();
		client_active = true;
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
		if(connection_established){
			graphics.print("Connection established", window.width()/2-60, window.height()/2-60);
		}
	}

	@Override
	public void close() {
	}

	@Override
	public void playSound(Sound sound) {
	}
	
	
	//Getters and Setters

	public void setConnection_established(boolean connection_established) {
		this.connection_established = connection_established;
	}

}
