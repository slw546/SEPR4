package lib;

import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.graphics.Quad;

public class SpriteAnimation {

	private Image image;		 // the animation sequence
	private Quad[] quads;	         // the rectangle to be drawn from the animation bitmap
	private int frameCount;		 // number of frames in animation
	private int currentFrame;	 // the current frame
	private double framePeriod;	 // milliseconds between each frame (1000/fps)

	private double spriteWidth;	 // the width of the sprite to calculate the cut out rectangle
	private double spriteHeight; // the height of the sprite
	
	private boolean hasFinished; // Flag which is set to mark when all frames have been drawn

	private int x;				 // the X coordinate of the object (top left of the image)
	private int y;				 // the Y coordinate of the object (top left of the image)
	private double gameTime;	 // tracks how long a frame has been shown for. Updated by the parent scene update(dt)
	private double imageWidth, imageHeight;
	private boolean isLooping;
	private boolean verbose;
	
	/**
	 * <h1>Sprite Animation</h1>
	 * <p>Creates an animation class</p>
	 * @param image image from which the quads are taken.
	 * @param x the x position to draw the animation.
	 * @param y the y position to draw the animation.
	 * @param fps how many animation frames to draw per second.
	 * @param frameCount how many frames the animation comprises.
	 * @param framesWide how many frames wide the image is
	 * @param framesHigh how many frames high the image is
	 * @param looping whether to loop the animation
	 * @param verbose whether to output information
	 */
	public SpriteAnimation(Image image, int x, int y, int fps,
			int frameCount, int framesWide, int framesHigh, boolean looping,
			boolean verbose){
		this.image = image;
		imageWidth = image.width();
		if (verbose) {
			System.out.println("-----");
		}
		imageHeight = image.height();
		if (verbose) {
			System.out.println("-----");
		}
		this.x = x;
		this.y = y;
		this.frameCount = frameCount;
		currentFrame = 0;
		spriteWidth = imageWidth / framesWide;
		spriteHeight = imageHeight / framesHigh;
		if (verbose) {
			System.out.println("Image Dimensions: " + imageWidth + ", " + imageHeight);
			System.out.println("Frame Dimensions: " + spriteWidth + ", " + spriteHeight);
		}
		framePeriod = 1.0/fps;
		gameTime = 0;
		quads = new Quad[frameCount];
		for (int n = 0; n < frameCount; n ++) {
			int i = n % framesWide;
			int j = n / framesWide;
			quads[n] = graphics.newQuad(i * spriteWidth, j * spriteHeight,
					spriteWidth, spriteHeight, imageWidth, imageHeight);
		}
		isLooping = looping;
		this.verbose = verbose;
		hasFinished = false;
	}
	
	/**
	 * <h1>Sprite Animation</h1>
	 * <p>Creates an animation class</p>
	 * @param imageFilepath the filepath at which the image is.  
	 * @param x the x position to draw the animation.
	 * @param y the y position to draw the animation.
	 * @param fps how many animation frames to draw per second.
	 * @param frameCount how many frames the animation comprises.
	 * @param framesWide how many frames wide the image is
	 * @param framesHigh how many frames high the image is
	 * @param looping whether to loop the animation
	 * @param verbose whether to output information
	 */
	public SpriteAnimation(String imageFilepath, int x, int y, int fps, int frameCount,
			int framesWide, int framesHigh, boolean looping, boolean verbose){
		new SpriteAnimation(graphics.newImage(imageFilepath), x, y, fps, frameCount,
				framesWide, framesHigh, looping, verbose);
	}
	
	/**
	 * updates the timer and changes the frame if necessary
	 * @param dt time in seconds since last update
	 */
	public void update(double dt) {
		if (hasFinished) return;
		
		gameTime += dt;
		if (gameTime > framePeriod) { // frame period exceeded
			gameTime = 0; //reset timer
			if (verbose) {
				System.out.print(currentFrame + ", ");
			}
			currentFrame++; //increment frame
			if (currentFrame >= frameCount) {
				if (!isLooping) {
					hasFinished = true;
				} else {
					currentFrame = 0;
				}
			}
		}
	}
	
	/**
	 * draws the animation
	 */
	public void draw() {
		if (hasFinished) return;
		graphics.drawq(image, quads[currentFrame], x, y);
	}
	
	/**
	 * 
	 * @return whether the animation has finished
	 */
	public boolean hasFinished() {
		return hasFinished;
	}

}