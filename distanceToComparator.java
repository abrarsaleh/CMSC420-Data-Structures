package cmsc420.structure;

import java.util.Comparator;

import cmsc420.command.Pair;

/**
 * Compares two cities based on location of x and y coordinates. First compares
 * the x values of each {@link City}. If the x values are the same, then the y
 * values of each City are compared.
 * 
 * @author Ben Zoller
 * @editor Ruofei Du
 * @version 1.0, 23 Jan 2007
 */
public class distanceToComparator implements Comparator<Pair> {

	int x;
	int y;

	public distanceToComparator(City start) {
		x = start.getX();
		y = start.getY();
	}

	@Override
	public int compare(final Pair one, final Pair two) {

		if (one.getDistance() - two.getDistance() == 0) {

			return one.getCity().getName().compareTo(two.getCity().getName());

		} else {

			return (int) (one.getDistance() - two.getDistance());
		}
	}
}