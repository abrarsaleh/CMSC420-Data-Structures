package cmsc420.structure;

import java.util.Comparator;

/**
 * Compares two roads based on their names.
 * 
 * @author Ben Zoller
 * @version 1.0, 23 Jan 2007
 */
public class RoadComparator implements Comparator<Road> {
	@Override
	public int compare(final Road r1, final Road r2) {

		String r1s = r1.getStartCity().getName();

		String r1e = r1.getEndCity().getName();

		String r2s = r2.getStartCity().getName();

		String r2e = r2.getEndCity().getName();

		if (r1e.equals(r2e)) {

			return -r1s.compareTo(r2s);
		}

		return -r1e.compareTo(r2e);

		/*
		 * double distance1 = Math.hypot(r1.seg.getX1() - r1.seg.getX2(),
		 * r1.seg.getY1() - r1.seg.getY2());
		 * 
		 * double distance2 = Math.hypot(r2.seg.getX1() - r2.seg.getX2(),
		 * r2.seg.getY1() - r2.seg.getY2());
		 * 
		 * return (int) (distance1 - distance2);
		 */
	}
}