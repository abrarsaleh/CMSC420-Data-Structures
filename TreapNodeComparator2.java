package cmsc420.sortedmap;

import java.util.Comparator;

import cmsc420.structure.City;

public class TreapNodeComparator2<K, V> implements Comparator<TreapNode<K, V>> {

	@Override
	public int compare(TreapNode o1, TreapNode o2) {

		if (o1.equals(o2)) {
			return 0;
		}

		// If the Keys are cities
		if (o1.getKey() instanceof City && o2.getKey() instanceof City) {

			City a = (City) o1.getKey();

			City b = (City) o2.getKey();
			return -a.getName().compareTo(b.getName());
		}

		// If the Values are cities
		if (o1.getValue() instanceof City && o2.getValue() instanceof City) {

			City a = (City) o1.getValue();

			City b = (City) o2.getValue();
			return -a.getName().compareTo(b.getName());
		}

		return 0;

		/*
		 * K k1 = (K) o1.getKey(); K k2 = (K) o2.getKey(); V v1 = (V)
		 * o1.getValue(); V v2 = (V) o2.getValue();
		 * 
		 */

	}
}
