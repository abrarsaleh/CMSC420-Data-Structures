package cmsc420.sortedmap;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import cmsc420.structure.City;
import cmsc420.structure.CityNameComparator;
import cmsc420.structure.Road;
import cmsc420.structure.RoadSetComparator;
import cmsc420.structure.TwoCities;

public class AdjacencyList {

	TreeMap<City, ArrayList<City>> adjList;

	TreeSet<Road> roadSet;

	TreeMap<TwoCities, Double> distMap;

	public AdjacencyList(TreeSet<Road> toEnter, TreeMap<TwoCities, Double> distances) {
		adjList = new TreeMap<City, ArrayList<City>>(new CityNameComparator());
		roadSet = toEnter;
		distMap = distances;

		for (City c : adjList.keySet()) {

			adjList.put(c, new ArrayList<City>());

		}
	}

	public AdjacencyList() {
		roadSet = new TreeSet<Road>(new RoadSetComparator());
		distMap = new TreeMap<TwoCities, Double>();
		adjList = new TreeMap<City, ArrayList<City>>(new CityNameComparator());

		for (City c : adjList.keySet()) {

			adjList.put(c, new ArrayList<City>());

		}

	}

	public void updateRoads(TreeSet<Road> roadSetb) {
		roadSet = roadSetb;
	}

	public void updateDists(TreeMap<TwoCities, Double> distMapb) {
		distMap = distMapb;
	}

	public void add(City start, City toAdd) {

		if (roadSet == null || roadSet.isEmpty()) {
			roadSet = new TreeSet<Road>(new RoadSetComparator());
		}
		ArrayList<City> holder;

		roadSet.add(new Road(start, toAdd));
		roadSet.add(new Road(toAdd, start));

		if (adjList == null || adjList.isEmpty()) {
			adjList = new TreeMap<City, ArrayList<City>>(new CityNameComparator());
			holder = new ArrayList<City>();
			holder.add(toAdd);
			adjList.put(start, holder);

		} else {
			holder = adjList.get(start);

			if (holder == null || holder.isEmpty()) {
				holder = new ArrayList<City>();
				holder.add(toAdd);
				adjList.put(start, holder);
			} else {

				holder = adjList.get(start);
				holder.add(toAdd);
				adjList.put(start, holder);
			}

		}

	}

	public ArrayList<City> getAdjacents(City city) {
		if (adjList == null || adjList.get(city) == null) {
			adjList = new TreeMap<City, ArrayList<City>>(new CityNameComparator());
			return new ArrayList<City>();
		}
		return adjList.get(city);

	}

	public boolean isAdjacent(City a, City b) {

		return roadSet.contains(new Road(a, b)) || roadSet.contains(new Road(b, a));
	}

	public double distanceBetween(City a, City b) {

		TwoCities paira = new TwoCities(a, b);
		TwoCities pairb = new TwoCities(b, a);

		if (distMap.get(paira) == null) {
			return distMap.get(pairb);
		}

		return distMap.get(paira);
	}

	public void clear() {
		adjList = null;
		roadSet = null;
		distMap = null;
	}

}
