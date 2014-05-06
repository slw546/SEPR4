package scn;

import java.io.File;

import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import btc.Main;

/**
 * <h1>Shop</h1>
 * <p>The Class that is run when the Game is successfully finished/won. represents the in-game shop.</p>
 * @author Richard Kirby
 */

public class Shop extends Scene {
	
	/** The money that the bear earned during his job */
	private int money;
	
	/** The cost of a beer from the shop */
	private int beerCost;
	
	/**The cost of an item of food from the shop */
	private int foodCost;
	
	/**The cost of the bear toy from the shop */
	private int bearCost;
	
	/** The total number of beers the player has purchased */
	private int totalBeers;
	
	/**The total number of food items the player has purchased */
	private int totalFood;
	
	/** The number of bears, the player has purchased - should only ever be 1 at most */
	private int bearCount;
	
	private int keyPressed;
	
	/** Image of the bar/shop wihout speech bubble and with bear */
	private Image barNoBubble;
	
	/** Image of the bar/shop wih speech bubble and with bear */
	private Image barBubble;
	
	/** Image of the bar/shop wih speech bubble and without bear */
	private Image barBubbleNoBear;
	
	/** Image of the bar/shop wihout bear and without speech bubble */
	private Image barNoBubbleNoBear;
	
	/** Image of the beer icon */
	private Image beer;
	
	/** Image of the food icon */
	private Image food;
	
	/** Image of the bear Icon */
	private Image bearPlush;
	
	/** The scale of the game window in respect to the screen/monitor size
	 *  - Used to scale graphics to the player's screen size */
	private final double scale = Main.getScale();
	
	/** Used only for testing - acts as the mouse's x-coord */
	private int testingX;
	
	/** Used only for testing - acts as the mouse's y-coord */
	private int testingY;
	
	/**
	 * Constructor for the shop
	 * @param main - The instance of Game 
	 * @param score - The score the player achieved upon completing the insatnce of Game
	 */
	public Shop(Main main, int score) {
		super(main);
		this.money = score;
		this.beerCost = 150;
		this.foodCost = 300;
		this.bearCost = 1500;
	}
	
	@Override
	public void start() {
		/*
		 * This constructs the images used within the scene
		 * The constructor itself sets the images default locations to be drawn on screen
		 * It also scales the images if the screen size is less than the target screen size
		 */
		barNoBubble = graphics.newImage("gfx" + File.separator + "barGuy.png", 225, 225, scale);
		barBubble = graphics.newImage("gfx" + File.separator + "barGuySpeechBubble.png", 225, 225, scale);
		barNoBubbleNoBear = graphics.newImage("gfx" + File.separator + "barGuyNoBear.png", 225, 225, scale);
		barBubbleNoBear = graphics.newImage("gfx" + File.separator + "barGuySpeechBubbleNoBear.png", 225, 225, scale);
		beer = graphics.newImage("gfx" + File.separator + "beerSprite.png", 825, 350, scale);
		food = graphics.newImage("gfx" + File.separator + "burgerSprite.png", 825, 550, scale);
		bearPlush = graphics.newImage("gfx" + File.separator + "bearSprite.png", 1050, 350, scale);	
	}

	@Override
	public void update(double dt) {}

	@Override
	public void draw() {
		//Print the necessay information to the screen
		graphics.printScaled("Shop", 560, 50, 4, scale);
		graphics.printScaled("Name : Albeart Einstein", 360, 150, 3, scale);
		graphics.printScaled("Money : `" + this.money, 360, 200, 3, scale);
		graphics.printScaled("Beer `" + this.beerCost, 825, 325, 2, scale);
		graphics.printScaled("Food `" + this.foodCost, 825, 525, 2, scale);
		graphics.printScaled("Bear `" + this.bearCost, 1050, 325, 2, scale);
		
		//Draw the items for sale to the screen
		graphics.drawScaled(beer, beer.getX(), beer.getY(), scale);
		graphics.drawScaled(food, food.getX(), food.getY(), scale);
		graphics.drawScaled(bearPlush, bearPlush.getX(), bearPlush.getY(), scale);

		
		/*
		 * The below if branches dictate the image of the bar that is drawn to the screen
		 * Note : The beer, food and bear images are drawn immediately to the screen as they remain the same throughout the scene
		 * The image of the shop has 4 different version
		 * 1 - A bar with a speech bubble and with the bear item still available to buy
		 * In this case, the player's mouse cursor is over the bar image and they have not purchased the bear item
		 * 2 - A bar image with speech bubble (for when the player's mouse cursor is over the bar image)
		 * and the bear item is not available to buy. In this case, the bear is no longer available to buy and the player's
		 * mouse cursor is over the bar image
		 * 3 - A bar image without a speech bubble and with the bear item available to buy
		 * In this case, the player has already purchased the bear item and their cursor is not over an image
		 * 4 - A bar image without a speech bubble and with the bear item no longer available to buy
		 * In this case, the player has already purchased the bear item and their mouse cursor is not over an image
		 */
		if (MouseIsOverShop() && !checkForPurchase()) {
			//image to display if the bear has not been purchased
			if (!hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubble, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble
			}
			//image to display if the bear has been purchased
			if (hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble
			}
			//Print the barman's dialogue
			graphics.setColour(0, 0, 0); //Set the color to black
			graphics.printScaled("Hey Al.", 375, 300, 2, scale);
			graphics.printScaled("What can", 375, 335, 2, scale);
			graphics.printScaled("I get ya?", 375, 370, 2, scale);
		}
		
		if (checkForPurchase() && beerSelected()) { //dialogue to print if the player has enough money for a beer
			//image to display if the bear has not been purchased
			if (!hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubble, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble
			}
			//image to display if the bear has been purchased
			if (hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble 
			}
			//Print the barman's dialogue
			graphics.setColour(0, 0, 0); //Set the color to black
			graphics.printScaled("A delicious", 373, 300, 2, scale);
			graphics.printScaled("Grizzly", 400, 340, 2, scale);
			graphics.printScaled("beer!", 415, 380, 2, scale);
		}
		
		if (checkForPurchase() && foodSelected()) { //dialogue to print if the player has enough money for food
			//image to display if the bear has not been purchased
			if (!hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubble, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble
			}
			//image to display if the bear has been purchased
			if (hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble 
			}
			//Print the barman's dialogue
			graphics.setColour(0, 0, 0); //Set the color to black
			graphics.printScaled("Our classic", 373, 310, 2, scale);
			graphics.printScaled("bearger!", 400, 350, 2, scale);
		}
		
		if (checkForPurchase() && bearSelected()) { //dialogue to print if the player has enough money for the bear
			//image to display if the bear has not been purchased
			if (!hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubble, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble
			}
			//image to display if the bear has been purchased
			if (hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble 
			}
			//Print the barman's dialogue
			graphics.setColour(0, 0, 0); //Set the color to black
			graphics.printScaled("One of a", 393, 300, 2, scale);
			graphics.printScaled("Kind", 420, 340, 2, scale);
			graphics.printScaled("bear plush!", 388, 380, 2, scale);
		}
		
		//The following prints when the player does not have enough money for a selected beer or food item
		else if (checkForPurchase() == false && (beerSelected() || foodSelected() || (bearSelected() && !hasBoughtBear()))) {
			//image to display if the bear has not been purchased
			if (!hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubble, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble
			}
			//image to display if the bear has been purchased
			if (hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble 
			}	
			//Print the barman's dialogue
			graphics.setColour(0, 0, 0); //Set the color to black
			graphics.printScaled("Sorry Al.", 373, 300, 2, scale);
			graphics.printScaled("You don't", 373, 325, 2, scale);
			graphics.printScaled("have enough", 373, 350, 2, scale);
			graphics.printScaled("money.", 373, 375, 2, scale);
		}
		
		//The following prints when the player selects the bear item and it has already been purchased
		else if (bearSelected() && hasBoughtBear()) {
			graphics.setColour(255, 255, 255); //Set the color to white
			graphics.drawScaled(barBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image to add speech bubble 
			graphics.setColour(0, 0, 0); //Set the color to black
			graphics.printScaled("Sorry Al.", 373, 300, 2, scale);
			graphics.printScaled("I only", 373, 325, 2, scale);
			graphics.printScaled("had one of", 373, 350, 2, scale);
			graphics.printScaled("those.", 373, 375, 2, scale);
		}
		
		//The following switches the image back to that without the speech bubble when an item and the shop are not selected
		else if (!checkForPurchase() && !MouseIsOverShop()) {
			//default image if the bear has not been purchased
			if (!hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barNoBubble, barBubble.getX(), barBubble.getY(), scale); //switch image back to that without speech bubble and with bear
			}
			//default image if the bear has already been purchased
			if (hasBoughtBear()) {
				graphics.setColour(255, 255, 255); //Set the color to white
				graphics.drawScaled(barNoBubbleNoBear, barBubble.getX(), barBubble.getY(), scale); //switch image back to that without speech bubble, without bear
			}
			
		}
		
		graphics.setColour(255, 255, 255); //change colour to white
		
		//Only allow the player to leave the shop scene/finish the game when they have purchased at least one item
		if (this.totalBeers > 0 || this.totalFood > 0 || this.bearCount > 0) {
			//Display prompt to inform the player that they can now leave
			graphics.printScaled("PRESS ANY KEY TO RETURN HOME", 300, 800, 2, scale);
		}
		graphics.setColour(0, 0, 0); //change colour to black
		super.draw();
	}
	
	/**
	 * Accessor for the player's total money
	 * @return the player's money
	 */
	public int getMoney() {
		return this.money;
	}
	
	/**
	 * Accessor for the cost of a beer item
	 * @return the integer value of the beerCost variable
	 */
	public int beerPrice() {
		return this.beerCost;
	}
	
	/**
	 * Accessor for the cost of a food item
	 * @return the integer value of the foodCost variable
	 */
	public int foodPrice() {
		return this.foodCost;
	}
	
	/**
	 * Accessor for the cost of a bear item
	 * @return the integer value of the bearCost variable
	 */
	public int bearPrice() {
		return this.bearCost;
	}
	
	/**
	 * Accessor for the total amount of beer items purchased from the shop
	 * @return the value of the totalBeers integer variable
	 */
	public int getBeerCount() {
		return this.totalBeers;
	}
	
	/**
	 * Accessor for the total amount of food items purchased from the shop
	 * @return the value of the totalFood integer variable
	 */
	public int getFoodCount() {
		return this.totalFood;
	}
	
	/**
	 * Accessor for the total amount of bear items purchased from the shop
	 * @return the value of the bearCount integer variable (should only ever be 1)
	 */
	public int getBearCount() {
		return this.bearCount;
	}
	
	/**
	 * Accessor for the bar image
	 * Note: There are 4 different bar images but all have the same dimensions.
	 * This accessor is merely used to get the attributes of a bar image: which it is does not matter
	 * @return The barBubble image
	 */
	public Image getBarImage() {
		return this.barBubble;
	}
	
	/**
	 * Accessor for the beer image
	 * @return The beer image
	 */
	public Image getBeerImage() {
		return this.beer;
	}
	
	/**
	 * Accessor for the food image
	 * @return The food image
	 */
	public Image getFoodImage() {
		return this.food;
	}
	
	/**
	 * Accessor for the bear image
	 * @return The bear image
	 */
	public Image getBearImage() {
		return this.bearPlush;
	}
	
	/**
	 * Accessor for the mouse's x-coordinate
	 * @return The mouse's x-xoordinate
	 */
	public int mouseX() {
		if (!Main.testing) {
			return input.mouseX();
		} else {
			//only when testing
			return this.testingX;
		}
	}
	
	/**
	 * Accessor for the mouse's y-coordinate
	 * @return The mouse's y-xoordinate
	 */
	public int mouseY() {
		if (!Main.testing) {
			return input.mouseY();
		} else {
			//only when testing
			return this.testingY;
		}
	}
	
	/**
	 * This method checks that the
	 * boundaries of the mouse cursor are over a purchasable item.
	 * Also ensures that the player has sufficient funds to purchase the item selected
	 * @return true when mouse is over an item and the player has enough money
	 */
	public boolean checkForPurchase() {
		if (beerSelected()) {
			if (this.money >= this.beerCost) {
				return true;
			}
		}
		if (foodSelected()) {
			if (this.money >= this.foodCost) {
				return true;
			}
		}
		if (bearSelected()) {
			if (this.money >= this.bearCost && this.bearCount < 1) {
				return true;
			}
		}
		return false; //The player does not have the money for each item
	}
	
	/**
	 * Determines if the player's mouse cursor is over the beer item
	 * @return true if mouse is within the bounds of the beer icon
	 */
	public boolean beerSelected() {
		if ((mouseX() >= beer.getX()) && (mouseX() <= (beer.getX() + beer.scaledWidth(scale)))
				&& (mouseY() >= beer.getY()) && (mouseY() <= (beer.getY() + beer.scaledHeight(scale)))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Determines if the player's mouse cursor is over the food item
	 * @return true if mouse is within the bounds of the food icon
	 */
	public boolean foodSelected() {
		if (mouseX() >= food.getX() && mouseX() <= (food.getX() + food.scaledWidth(scale))
				&& mouseY() >= food.getY() && mouseY() <= (food.getY() + food.scaledHeight(scale))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Determines if the player's mouse cursor is over the bear item
	 * @return true if mouse is within the bounds of the bear icon
	 */
	public boolean bearSelected() {
		if (mouseX() >= bearPlush.getX() && mouseX() <= (bearPlush.getX() + bearPlush.scaledWidth(scale))
				&& mouseY() >= bearPlush.getY() && mouseY() <= (bearPlush.getY() + bearPlush.scaledHeight(scale))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Determines if the player's mouse cursor is over the shop
	 * @return true if mouse is within the bounds of the shop
	 */
	public boolean MouseIsOverShop() {
		if (mouseX() >= barBubble.getX() && mouseX() <= (barBubble.getX() + barBubble.scaledWidth(scale))
				&& mouseY() >= barBubble.getY() && mouseY() <= (barBubble.getY() + barBubble.scaledHeight(scale))) {
			return true;
		}
		else {
			return false; 
		}
	}
	
	/**
	 * Check to determine if the player has bought the unique bear item
	 * @return true if the bear item has been bought
	 */
	public boolean hasBoughtBear() {
		if (this.bearCount == 1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Closes the shop scene.
	 * Creates and sets the ending scene.
	 */
	public void leaveShop() {
    	main.closeScene(); //close the shop scene
    	main.setScene(new GameEnding(main, this.totalBeers, this.totalFood, hasBoughtBear())); //go to the game ending scene
    }
	
	@Override
	public void keyPressed(int key) {
		this.keyPressed = key;	
	}
	
	@Override
	public void keyReleased(int key) {
		//Only allow the user to leave the shop once they have purchased at least one item
		if (key == this.keyPressed && (this.bearCount > 0 || this.totalFood > 0) || this.totalBeers > 0) {
			leaveShop(); //close the shop scene and head to final scene
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) {
		if (Main.testing) {
			this.testingX = x; //Only while testing
			this.testingY = y; //Only while testing
		}
		
		if (key == input.MOUSE_LEFT && this.checkForPurchase() && this.beerSelected()) {
			this.money -= this.beerCost; //the player has purchased a beer
			this.totalBeers += 1; //increment to beer count
		} 
		else if (key == input.MOUSE_LEFT && this.checkForPurchase() && this.foodSelected()) {
			this.money -= this.foodCost; //The player has purchased food
			this.totalFood += 1; //increment the food count
		}
		else if (key == input.MOUSE_LEFT && this.checkForPurchase() && this.bearSelected()) {
			this.money -= this.bearCost; //The player has purchased the bear
			this.bearCount = 1; //set bear count to 1
		}
	}

	@Override
	public void mouseReleased(int key, int x, int y) {}


	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {}

}

