package cmsc420.structure;

import java.util.Comparator;

/**
 * Compares two cities based on their names.
 * 
 * @author Ben Zoller
 * @version 1.0, 23 Jan 2007
 */
public class CityAndPredComparator implements Comparator<CityAndPred> {

	@Override
	public int compare(CityAndPred o1, CityAndPred o2) {

		if (o1.getPred().getName().equals(o2.getPred().getName())) {
			return o1.getCity().getName().compareTo(o2.getCity().getName());
		} else {
			return o1.getPred().getName().compareTo(o2.getPred().getName());
		}

	}
}