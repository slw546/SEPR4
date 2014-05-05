package scn;

import thr.ClientThread;
import thr.HostThread;
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
	private final int HOST_BUTTON_Y = window.height() / 2 + 60;
	
	private final int CLIENT_BUTTON_W = 128;
	private final int CLIENT_BUTTON_H = 16;
	private final int CLIENT_BUTTON_X = window.width() /2;
	private final int CLIENT_BUTTON_Y = window.height() / 2 + 90;
	
	private final int START_BUTTON_W = 128;
	private final int START_BUTTON_H = 16;
	private final int START_BUTTON_X = window.width() /2;
	private final int START_BUTTON_Y = window.height() / 2 + 120;
	
	//Network threads
	private HostThread host;
	private ClientThread client;
	
	private String address = "localhost";
	private int port = 4444;

	//enum for state of connection
	public enum networkStates {
			NO_CONNECTION, 
			WAITING_FOR_CONNECTION,
			ATTEMPTING_CONNECTION,
			CONNECTION_ESTABLISHED, 
			CONNECTION_LOST
	}
	
	//enum for errors thrown by network thread
	public enum errorCauses {
		UNKNOWN_HOST, //host address incorrect - connection not established
		NO_HOST_RUNNING, //the client tried to connect before a host had been started.
		SOCKET_IO_UNAVAILABLE, //unable to get IO streams of socket during set up - connection not established
		CLASS_NOT_FOUND, //unknown class sent via object stream
		IO_ERROR_ON_SEND, //unable to send something - connection lost
		IO_ERROR_ON_RECIEVE, //failed to recieve something - connection lost
		CLOSED_BY_YOU, //player closed the game scene with the escape key
		CLASS_CAST_EXCEPTION, //synchronisation failed, or someone exited voluntarily.
		SOCKET_TIMEOUT //an attempt to read the socket failed; nothing was recieved for 5 seconds.
	}
	
	//current state, variable to hold an error from network thread
	private networkStates state;
	private errorCauses error;
	
	//game scene
	private MultiplayerGame game;
	
	//Textbox for flavour text about chosen game mode.
	private lib.TextBox textBox;
	
	//Textfield for text input
	private lib.TextInput textInput;

	//list of buttons
	private lib.ButtonText[] buttons;
	
	//flags
	private boolean host_active = false;
	private boolean startOrdered = false;

	protected MultiplayerSetUp(Main main) {
		super(main);
		//Textbox to write flavour text and instructions to.
		textBox = new lib.TextBox(128, 96, window.width() - 256, window.height() - 96, 32);
		lib.TextInput.Accept ip = new lib.TextInput.Accept() {
			@Override
			public boolean acceptString(String string) {
				return string.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || string.equals("localhost");
			}
		};
		textInput = new lib.TextInput(CLIENT_BUTTON_X - 140, CLIENT_BUTTON_Y - 4, 132, 24, ip, "127.0.0.1");
		state = networkStates.NO_CONNECTION;
	}
	
	protected MultiplayerSetUp(Main main, networkStates state, errorCauses cause){
		super(main);
		this.state = state;
		this.error = cause;
	}

	@Override
	public void mousePressed(int key, int x, int y) {
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		for (lib.ButtonText b : buttons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
		textInput.mouseReleased(key, x, y);
	}

	@Override
	public void keyPressed(int key) {
		textInput.keyPressed(key);
	}

	@Override
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
		//if a host or client is running, kill them as well
		if (host != null){
			host.escapeThread();
		} else if (client != null){
			client.escapeThread();
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
				
				if (host_active){
					//If a host thread is active, have it carry out actions
					//to start client game
					host.setGameScene(game);
					host.setPlaying(true);
				}
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
		//Create and start a host thread
		host = new HostThread(port, this, main);
		//create a game scene to be used when the game starts
		game = new MultiplayerGame(main, MultiplayerGame.MultiplayerRole.HOST, host, this);
		
		host.start();
		host_active = true;
		state = networkStates.WAITING_FOR_CONNECTION;
		textInput.active = false;
	}
	
	public void startGame(){
		if (startOrdered){
			main.setScene(game);
			//prevent another instance of the game opening
			//if the lobby update loop runs again before the scene switch
			startOrdered = false;
		}
	}
	
	
	private void createClient(){
		if (!textInput.isValid()) return;
		setAddress(textInput.getText());
		//hide buttons
		buttons[0].setAvailability(false);
		buttons[1].setAvailability(false);

		//create and start a client thread
		client = new ClientThread(address, port, this, main);
		//create a game scene to be used when the game starts
		game = new MultiplayerGame(main, MultiplayerGame.MultiplayerRole.CLIENT, client, this);
		client.setGameScene(game);
		
		client.start();
		state = networkStates.NO_CONNECTION;
	}

	@Override
	public void update(double dt) {
		//update the textbox
		textBox.update(dt);
		textInput.update(dt);
		
		//reset buttons and text input if connection lost or no connection
		if (state == networkStates.CONNECTION_LOST || state == networkStates.NO_CONNECTION){
			buttons[0].setAvailability(true);
			buttons[1].setAvailability(true);
			buttons[2].setAvailability(false);
			textInput.active = true;
		}
		
		//make start game button available once connection established, but only for the host.
		if (state == networkStates.CONNECTION_ESTABLISHED && host_active){
			buttons[2].setAvailability(true);
		}
		
		startGame();
	}

	@Override
	public void draw() {
		//draw the textbox
		textBox.draw();
		textInput.draw();
		
		graphics.setColour(0, 128, 0);
		switch (state){
		case WAITING_FOR_CONNECTION:
			graphics.printCentred("Waiting for a player to join", 0, window.height()/2-30, 1, window.width());
			graphics.printCentred("Hosting at: " + address + ".", 
					0, window.height()/2-10, 1, window.width());
			break;
		case ATTEMPTING_CONNECTION:
			graphics.print("Connecting..", window.width()/2-60, window.height()/2-30);
			break;
		case CONNECTION_ESTABLISHED:
			graphics.print("Connection established", window.width()/2-60, window.height()/2-30);
			if (host_active){
				graphics.print("Hosting at: " + address + " Port: " + port, 
						window.width()/2-120, window.height()/2-10);
				graphics.print("You are the ACTO for Syrrilicovich, on the Left of the airspace.", 
						window.width()/2-180, window.height()/2-50);
			} else {
				graphics.print("Waiting for the host to start the game.",
						window.width()/2-120, window.height()/2-10);
				graphics.print("You are the ACTO for Chkalovsky, on the Right of the airspace.", 
						window.width()/2-180, window.height()/2-50);
			}
			break;
		case CONNECTION_LOST:
			graphics.print("Connection lost", window.width()/2-60, window.height()/2-60);
			printError();
			break;
		default:
			break;
		}
		
		//draw the buttons
		for (lib.ButtonText b : buttons) {
			b.draw();
		}
	}
	
	private void printError(){
		switch(error){
		case UNKNOWN_HOST:
			graphics.print("Error: Unknown Host. Check host address and port. ", 
					window.width()/2-60, window.height()/2-30);
			break;
		case NO_HOST_RUNNING:
			graphics.print("Error: Host unresponsive or not running.",
					window.width()/2-60, window.height()/2-30 );
			break;
		case SOCKET_IO_UNAVAILABLE:
			graphics.print("Error: Unable to set up IO streams.", 
					window.width()/2-60, window.height()/2-30);
			break;
		case CLASS_NOT_FOUND:
			graphics.print("Error: Unknown class recieved.",
					window.width()/2-60, window.height()/2-30);
			break;
		case IO_ERROR_ON_SEND:
			graphics.print("Error: IO send failed. Connection lost.",
					window.width()/2-60, window.height()/2-30);
			break;
		case IO_ERROR_ON_RECIEVE:
			graphics.print("Error: IO recieve failed. Connection lost. ",
					window.width()/2-60, window.height()/2-30);
			break;
		case CLOSED_BY_YOU:
			graphics.print("Connection closed", window.width()/2-60,
					window.height()/2-30);
			break;
		case CLASS_CAST_EXCEPTION:
			graphics.print("Error: Game quit or Synchronisation failed",
					window.width()/2-60, window.height()/2-30);
			break;
		case SOCKET_TIMEOUT:
			graphics.print("Error: Connection timed out.",
					window.width()/2-60, window.height()/2-30);
			break;
		}
	}

	@Override
	public void close() {
	}

	@Override
	public void playSound(Sound sound) {
	}
	
	//Getters and Setters
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setNetworkState(networkStates state){
		this.state = state;
	}
	
	public void setErrorCause(errorCauses cause){
		this.error = cause;
	}
	
	public void setStartOrdered(boolean order){
		this.startOrdered = order;
	}

}
