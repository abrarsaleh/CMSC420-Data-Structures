package cmsc420.structure;

import java.util.Comparator;

/**
 * Compares two cities based on their names.
 * 
 * @author Ben Zoller
 * @version 1.0, 23 Jan 2007
 */
public class TwoCitiesRoadNameComparator implements Comparator<TwoCities> {
	@Override
	public int compare(final TwoCities c1, final TwoCities c2) {

		String r1s = c1.getA().getName();

		String r1e = c1.getB().getName();

		String r2s = c2.getA().getName();

		String r2e = c2.getB().getName();

		if (r1s.equals(r2s)) {

			if (r1e.equals(r2e)) {
				return 0;
			}

			return -r1e.compareTo(r2e);
		}

		return -r1s.compareTo(r2s);

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