package cmsc420.structure;

import java.awt.geom.Point2D;

import cmsc420.geom.Geometry2D;

/**
 * City class is an analogue to a real-world city in 2D space. Each city
 * contains a location ((x,y) coordinates), name, radius, and color.
 * <p>
 * Useful <code>java.awt.geom.Point2D</code> methods (such as distance()) can be
 * utilized by calling toPoint2D(), which creates a Point2D copy of this city's
 * location.
 * 
 * @author Ben Zoller
 * @editor Ruofei Du
 * @version 1.0, 19 Feb 2007
 * @revise 1.1, 11 Jun 2014
 */
public class City implements Comparable, Geometry2D {
	/** name of this city */
	protected String name;

	/** 2D coordinates of this city */
	public Point2D.Float pt;

	/** radius of this city */
	protected int radius;

	/** color of this city */
	protected String color;

	protected boolean isIsolated;

	/**
	 * Constructs a city.
	 * 
	 * @param name
	 *            name of the city
	 * @param x
	 *            X coordinate of the city
	 * @param y
	 *            Y coordinate of the city
	 * @param radius
	 *            radius of the city
	 * @param color
	 *            color of the city
	 */
	public City(final String name, final int x, final int y, final int radius, final String color, boolean in) {
		this.name = name;
		pt = new Point2D.Float(x, y);
		this.radius = radius;
		this.color = color;
		isIsolated = in;
	}

	public City(City other, boolean in) {
		name = other.name;
		pt = other.pt;
		radius = other.radius;
		color = other.color;
		isIsolated = in;
	}

	public void setIsIsolated(boolean in) {
		isIsolated = in;
	}

	/**
	 * Gets the name of this city.
	 * 
	 * @return name of this city
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the X coordinate of this city.
	 * 
	 * @return X coordinate of this city
	 */
	public int getX() {
		return (int) pt.x;
	}

	/**
	 * Gets the Y coordinate of this city.
	 * 
	 * @return Y coordinate of this city
	 */
	public int getY() {
		return (int) pt.y;
	}

	/**
	 * Gets the color of this city.
	 * 
	 * @return color of this city
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Gets the radius of this city.
	 * 
	 * @return radius of this city.
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Determines if this city is equal to another object. The result is true if
	 * and only if the object is not null and a City object that contains the
	 * same name, X and Y coordinates, radius, and color.
	 * 
	 * @param obj
	 *            the object to compare this city against
	 * @return <code>true</code> if cities are equal, <code>false</code>
	 *         otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			City c = (City) obj;
			return (pt.equals(c.pt) && (radius == c.radius) && color.equals(c.color));
		}
		return false;
	}

	public boolean CityIsIso() {
		return isIsolated;
	}

	/**
	 * Returns a hash code for this city.
	 * 
	 * @return hash code for this city
	 */
	@Override
	public int hashCode() {
		int hash = 12;
		hash = 37 * hash + name.hashCode();
		hash = 37 * hash + pt.hashCode();
		hash = 37 * hash + radius;
		hash = 37 * hash + color.hashCode();
		return hash;
	}

	/**
	 * Returns an (x,y) representation of the city. Important: casts the x and y
	 * coordinates to integers.
	 * 
	 * @return string representing the location of the city
	 */
	public String getLocationString() {
		final StringBuilder location = new StringBuilder();
		location.append("(");
		location.append(getX());
		location.append(",");
		location.append(getY());
		location.append(")");
		return location.toString();
	}

	/**
	 * Returns a Point2D instance representing the City's location.
	 * 
	 * @return location of this city
	 */
	public Point2D toPoint2D() {
		return new Point2D.Float(pt.x, pt.y);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int getType() {
		return 0;
	}

	public boolean equals(City other) {
		return this.getX() == other.getX() && this.getY() == other.getY();
	}

	@Override
	public int compareTo(Object obj) {
		if (this.equals(obj))
			return 0;

		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			City c = (City) obj;

			if (this.getName().equals(c.getName())) {
				return 0;
			}

			return this.getName().compareTo(c.getName());
		}

		throw new ClassCastException();

	}

}