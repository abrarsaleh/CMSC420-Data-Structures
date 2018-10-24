package cmsc420.structure;

import java.awt.geom.Point2D;
import java.util.Comparator;

import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Node;

public class ByPointComparator implements Comparator<Node> {

	Point2D.Float point;

	public ByPointComparator(Point2D.Float inPoint) {
		point = inPoint;
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

				double value1 = Point2D.distance(point.getX(), point.getY(), c1.getX(), c1.getY());
				double value2 = Point2D.distance(point.getX(), point.getY(), c2.getX(), c2.getY());

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
						return c2.getName().compareTo(c1.getName());
					} else {
						return n1;
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

}
