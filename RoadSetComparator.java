package cmsc420.structure;

import java.util.Comparator;

/**
 * Compares two cities based on their names.
 * 
 * @author Ben Zoller
 * @version 1.0, 23 Jan 2007
 */
public class RoadSetComparator implements Comparator<Road> {

	@Override
	public int compare(Road c1, Road c2) {
		String a1 = c1.getStartCity().getName();
		String a2 = c1.getEndCity().getName();
		String b1 = c2.getStartCity().getName();
		String b2 = c2.getEndCity().getName();

		int val1 = a1.compareTo(b1);

		if (val1 == 0) {

			int val2 = a2.compareTo(b2);

			if (val2 == 0)
				return -1;

			return val2;
		}
		return val1;
	}

}