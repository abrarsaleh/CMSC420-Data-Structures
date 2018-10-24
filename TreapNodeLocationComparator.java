package cmsc420.sortedmap;

import java.util.Comparator;

import cmsc420.structure.City;

/**
 * Compares two cities based on location of x and y coordinates. First compares
 * the x values of each {@link City}. If the x values are the same, then the y
 * values of each City are compared.
 * 
 * @author Ben Zoller
 * @editor Ruofei Du
 * @version 1.0, 23 Jan 2007
 */
public class TreapNodeLocationComparator<K, V> implements Comparator<TreapNode<K, V>> {

	@Override
	public int compare(TreapNode arg0, TreapNode arg1) {
		City one = (City) arg0.getKey();
		City two = (City) arg1.getKey();

		if (one.getY() < two.getY()) {
			return -1;
		} else if (one.getY() > two.getY()) {
			return 1;
		} else {
			/* one.getY() == two.getY() */
			if (one.getX() < two.getX()) {
				return -1;
			} else if (one.getX() > two.getX()) {
				return 1;
			} else {
				/* one.getX() == two.getX() */
				return 0;
			}
		}

	}
}