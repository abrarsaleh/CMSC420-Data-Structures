package cmsc420.structure;

public class TwoCities implements Comparable<TwoCities> {
	City a;
	City b;

	public TwoCities(City l, City p) {
		a = l;
		b = p;
	}

	public City getA() {
		return a;
	}

	public City getB() {
		return b;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o instanceof TwoCities))
			return false;

		TwoCities other = (TwoCities) o;

		if (a.equals(other.a) && b.equals(other.b))
			return true;

		return false;
	}

	@Override
	public int compareTo(TwoCities other) {

		if (other == null) {
			return 0;
		}

		if (other.a == null && other.b == null) {
			if (this.a == null && this.b == null) {
				return 0;
			} else
				return 1;

		} else {
			if (this.a == null && this.b == null)
				return -1;
		}

		String r1s = this.getA().getName();

		String r1e = this.getB().getName();

		String r2s = other.getA().getName();

		String r2e = other.getB().getName();

		if (r2s == null && r2e == null) {
			if (r1s == null && r1e == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (r1s == null && r1e == null) {
			return -1;
		}

		if (r1s == null && r2s == null && r2e == null && r1e == null) {
			return 0;
		}

		if (r1s.compareTo(r2s) == 0) {

			if (r1e.compareTo(r2e) == 0) {

				return 0;
			}

			return r1e.compareTo(r2e);
		}

		return r1s.compareTo(r2s);

	}

	@Override
	public int hashCode() {
		return a.hashCode();
	}
}
