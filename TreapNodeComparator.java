package cmsc420.sortedmap;

import java.util.Comparator;

public class TreapNodeComparator<K, V> implements Comparator<TreapNode<K, V>> {

	@Override
	public int compare(TreapNode o1, TreapNode o2) {

		if (o1.equals(o2)) {
			return 0;
		}

		return -1;

		/*
		 * K k1 = (K) o1.getKey(); K k2 = (K) o2.getKey(); V v1 = (V)
		 * o1.getValue(); V v2 = (V) o2.getValue();
		 * 
		 */

	}
}
