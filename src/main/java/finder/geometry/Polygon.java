package finder.geometry;

/**
 * The Polygon class represents a geometric object consisting of zero
 * or more points.
 * 
 * @author Johan Holmberg
 */
public class Polygon extends Multipoint {
	
	/**
	 * Creates an instance of Polygon.
	 */
	public Polygon() {
		super();
	}
	
	/**
	 * Calculates the area of the current polygon object.
	 * 
	 * @return The area.
	 */
	public double getArea() {
		// TODO: Implement this (http://www.mathopenref.com/coordpolygonarea.html)
		return -1;
	}
	
	/**
	 * Calculates the squareness of the current polygon object. A completely
	 * smooth rectangle will yield a value of 1.0, whereas a completely
	 * irregular shape will yield a value of 0.
	 * 
	 * @return The squareness value.
	 */
	public double getSquareness() {
		// TODO: Implement this.
		return -1;
	}
	
	/**
	 * Calculates the smoothness of the current polygon object. A completely
	 * smooth object will yield a value of 1.0, whereas a completely irregular
	 * shape will yield a value of 0.
	 * 
	 * @return The smoothness value.
	 */
	public double getSmoothness() {
		// TODO: Implement this.
		return -1;
	}
	
	/**
	 * Calculates the roundness of the current polygon object. A circle will
	 * yield a value of 1.0, whereas a completely irregular shape will yield a
	 * value of 0.
	 * 
	 * <p>See https://en.wikipedia.org/wiki/Roundness_(object) for more info.
	 * 
	 * @return The roundness value.
	 */
	public double getRoundness() {
		// TODO: Implement this.
		return -1;
	}
}
