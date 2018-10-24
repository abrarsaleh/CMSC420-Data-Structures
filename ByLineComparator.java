package cmsc420.structure;

import java.awt.geom.Line2D;
import java.util.Comparator;

import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Node;
import cmsc420.structure.City;
import cmsc420.structure.Road;

public class ByLineComparator implements Comparator<Node> {

	Line2D seg;
	Road road;

	public ByLineComparator(Road road) {
		seg = road.getSeg();
		this.road = road;
	}

	@Override
	public int compare(Node o1, Node o2) {

		if (o2.nodeType().equals("GrayNode")) {
			if (o1.nodeType().equals("GrayNode")) {
				return 0;
			}

			if (o1.nodeType().equals("BlackNode")) {
				return 1;
			}

			if (o1.nodeType().equals("WhiteNode")) {
				return 1;
			}
		}

		if (o2.nodeType().equals("BlackNode")) {
			if (o1.nodeType().equals("GrayNode")) {
				return -1;
			}

			if (o1.nodeType().equals("BlackNode")) {
				Black b1 = (Black) o1;
				Black b2 = (Black) o2;

				if (b1.getCity() == null) {// Not a city but a BlackNode with
											// just a road
					if (b2.getCity() == null) {
						return 0;
					}
					return 1;
				}

				if (b2.getCity() == null) {// Not a city but a BlackNode with
											// just a road
					return -1;
				}

				City c1 = b1.getCity();
				City c2 = b2.getCity();

				double value1 = distanceCalculator(c1.getX(), c1.getY(), road);
				double value2 = distanceCalculator(c2.getX(), c2.getY(), road);

				double result = 0;

				if (value1 < value2)
					result = -1;
				if (value2 < value1)
					result = 1;
				if (value1 == value2)
					result = 0;

				if (result == 0) {

					int n1 = c2.getName().compareTo(c1.getName());

					if (n1 == 0) {
						return -c2.getName().compareTo(c1.getName());
					} else {
						return -n1;
					}
				}

				return (int) result;
			}

			if (o1.nodeType().equals("WhiteNode")) {

				return 1;
			}
		}

		if (o2.nodeType().equals("WhiteNode")) {
			if (o1.nodeType().equals("GrayNode")) {
				return -1;
			}

			if (o1.nodeType().equals("BlackNode")) {
				return -1;
			}

			if (o1.nodeType().equals("WhiteNode")) {
				return 0;
			}
		}
		/*
		 * Point2D.Float first_value = o1.pt; Point2D.Float second_value =
		 * o2.pt;
		 * 
		 * double value1 = Command.howCloseToPoint(first_value, point); double
		 * value2 = Command.howCloseToPoint(second_value, point);
		 * 
		 * 
		 * int finalValue = Double.compare(value1, value2);
		 * 
		 * if(finalValue == 0){ if(o2.getName().length() !=
		 * o1.getName().length()){ return o2.getName().compareTo(o1.getName());
		 * } return o2.getName().length() - o1.getName().length(); }
		 * 
		 * return finalValue;
		 */
		return 0;
	}

	private double distanceCalculator(double x1, double y1, double x2, double y2) {
		return Math.hypot(x1 - x2, y1 - y2);
	}

	// Distance between a point and the closest point to it on a line segment
	private double distanceCalculator(int x, int y, Road searchRoad) {
		double deltaX = searchRoad.getEnd().getX() - searchRoad.getStart().getX();
		double deltaY = searchRoad.getEnd().getY() - searchRoad.getStart().getY();

		double u = ((x - searchRoad.getStart().getX()) * deltaX + (y - searchRoad.getStart().getY()) * deltaY)
				/ (deltaX * deltaX + deltaY * deltaY);

		double closestX;
		double closestY;

		if (u < 0) {
			closestX = searchRoad.getStart().getX();
			closestY = searchRoad.getStart().getY();
		} else if (u > 1) {
			closestX = searchRoad.getEnd().getX();
			closestY = searchRoad.getEnd().getY();
		} else {
			closestX = Math.round(searchRoad.getStart().getX() + u * deltaX);
			closestY = Math.round(searchRoad.getStart().getY() + u * deltaY);
		}

		return distanceCalculator(x, y, closestX, closestY);

		// Used
		// http://www.java2s.com/Code/Java/2D-Graphics-GUI/Returnsclosestpointonsegmenttopoint.htm
		// as reference
	}

}
