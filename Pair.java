package cmsc420.command;

import cmsc420.structure.City;

public class Pair {

	City city;
	double distance;

	public Pair(City in, double ind) {
		city = in;
		distance = ind;
	}

	public City getCity() {
		return city;
	}

	public double getDistance() {
		return distance;
	}

	public void setCity(City a) {
		city = a;
	}

	public void setDistance(Double d) {
		distance = d;
	}

}
