package cmsc420.structure;

public class CityAndPred {

	City city;
	City pred;

	public CityAndPred(City a, City preda) {
		city = a;
		pred = preda;
	}

	public City getCity() {
		return city;
	}

	public City getPred() {
		return pred;
	}

}
