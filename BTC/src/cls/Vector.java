package cls;

import java.io.Serializable;

import btc.Main;

/**
 * Simplified 3D vector class with basic operations
 * @author Huw Taylor
 */
public class Vector implements Serializable {
	
	/**
	 * serialVersionUID used to check consistency between host and reciever
	 * Required for network communication of objects
	 */
	private static final long serialVersionUID = -8060185066672703558L;
	private double x, y, z;
	
	/**
	 * Constructor for a vector
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Getter for X
	 * @return X
	 */
	public double x() {
		return x;
	}
	/**
	 * Getter for Y
	 * @return Y
	 */
	public double y() {
		return y;
	}
	/**
	 * Getter for Z
	 * @return Z
	 */
	public double z() {
		return z;
	}
	
	/**
	 * Calculates the magnitude of the vector
	 * @return the magnitude of the vector
	 */
	public double magnitude() {
		return Math.sqrt((x*x) + (y*y) + (z*z));
	}
	
	public double magnitudeSquared() {
		return (x*x) + (y*y) + (z*z);
	}
	
	/**
	 * Normalises the vector
	 * @return a normalised vector
	 */
	public Vector normalise() {
		return this.scaleBy(1/magnitude());
	}
	
	/**
	 * Scales the vector by a given scalar
	 * @param n the scalar to scale by
	 * @return a new scaled vector
	 */
	public Vector scaleBy(double n) {
		return new Vector(x * n, y * n, z * n);
	}
	
	/**
	 * Adds two vectors together
	 * @param v a vector to be added
	 * @return the sum of the vectors
	 */
	public Vector add(Vector v) {
		return new Vector(x + v.x(), y + v.y(), z + v.z());
	}
	
	/**
	 * Subtracts two vectors
	 * @param v a vector to be subtracted
	 * @return the result of the subtractions
	 */
	public Vector sub(Vector v) {
		return new Vector(x - v.x(), y - v.y(), z - v.z());
	}
	
	/**
	 * Gets the angle between this vector and a specified vector
	 * @param v the vector to find the angle to
	 * @return the angle between this vector and another
	 */
	public double angleBetween(Vector v) {
		double a = Math.acos( (x*v.x + y*v.y + z*v.z) / (magnitude() * v.magnitude()));
		if (v.y < y) a *= -1;
		return a;
	}
	
	/**
	 * Sets the x value of the vector
	 * @param x the x value to be set
	 */
	public void setX(double x){
		this.x = x;
	}
	
	/**
	 * Sets the y value of the vector
	 * @param y the y value to be set
	 */
	public void setY(double y){
		this.y = y;
	}
	
	/**
	 * Sets the z value of the vector
	 * @param z the z value to be set
	 */
	public void setZ(double z){
		this.z = z;
	}
	
	/**
	 * Maps between a position on the target screen and the actual screen
	 */
	public Vector remapPosition() {
		return new Vector((Main.width() / Main.TARGET_WIDTH) * this.x,
				(Main.height() / Main.TARGET_HEIGHT) * this.y, z);
	}

	/**
	 * Checks a vector for equality with this vector.
	 * @param o the object to be tested for equality
	 * @return a boolean result of the equality test.
	 */
	@Override
	public boolean equals(Object o) {
		if (o.getClass() != Vector.class) { 
			return false;
		} else {
			Vector v = (Vector) o;
			return (x == v.x()) && (y == v.y()) && (z == v.z());
		}
	}
	
	/**
	 * Outputs the vectors's details in a readable format.
	 * @return the textual representation of the vector
	 */
	@Override
	public String toString() {
		return ("x: " + x + " | y: " + y + " | z: " + z);
	}

}
