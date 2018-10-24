package cmsc420.structure;

public class Tuple {

	int x;
	int y;

	public Tuple(int inX, int inY) {
		x = inX;
		y = inY;
	}

	public Tuple(Tuple other) {
		x = other.x;
		y = other.y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
