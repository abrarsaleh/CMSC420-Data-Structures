package cmsc420.sortedmap;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {
	@Override
	public int compare(final String s1, final String s2) {
		return s2.compareTo(s1);
	}
}
