
/**
 * @(#)Command.java        1.1 
 * 
 * 2014/09/09
 *
 * @author Ruofei Du, Ben Zoller (University of Maryland, College Park), 2014
 * 
 * All rights reserved. Permission is granted for use and modification in CMSC420 
 * at the University of Maryland.
 */
package cmsc420.command;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.exception.CityAlreadyMappedException;
import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.pmquadtree.PMQuadtree;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Gray;
import cmsc420.sortedmap.AdjacencyList;
import cmsc420.sortedmap.Treap;
import cmsc420.structure.ByLineComparator;
import cmsc420.structure.ByPointComparator;
import cmsc420.structure.ByPointComparator2;
import cmsc420.structure.City;
import cmsc420.structure.CityAndPred;
import cmsc420.structure.CityLocationComparator;
import cmsc420.structure.CityNameComparator;
import cmsc420.structure.CityNameComparator2;
import cmsc420.structure.Road;
import cmsc420.structure.RoadComparator2;
import cmsc420.structure.RoadComparator3;
import cmsc420.structure.Tuple;
import cmsc420.structure.TwoCities;
import cmsc420.structure.distanceToComparator;
import cmsc420.structure.prquadtree.InternalNode;
import cmsc420.structure.prquadtree.LeafNode;
import cmsc420.structure.prquadtree.Node;
import cmsc420.structure.prquadtree.PRQuadtree;
import cmsc420.utils.Canvas;

/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 * 
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities
	 * command)
	 */
	protected final TreeMap<String, City> citiesByName = new TreeMap<String, City>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});

	/**
	 * stores created cities sorted by their locations (used with listCities
	 * command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(new CityLocationComparator());

	protected final Treap<String, City> treapByName = new Treap<String, City>();

	protected final Treap<City, Tuple> treapByLocation = new Treap<City, Tuple>(new CityNameComparator2());

	protected final TreeMap<City, Integer> allMappedCitiesByName = new TreeMap<City, Integer>(new CityNameComparator());

	protected final TreeMap<City, Integer> allIsolatedCitiesByName = new TreeMap<City, Integer>(new Comparator<City>() {

		@Override
		public int compare(City o1, City o2) {
			return o2.getName().compareTo(o1.getName());
		}
	});

	/** stores mapped cities in a spatial data structure */
	protected final PRQuadtree prQuadtree = new PRQuadtree();

	protected final PMQuadtree pmQuadtree = new PMQuadtree();

	protected final AdjacencyList adjList = new AdjacencyList();
	// protected final Set<Road> roadSet = new TreeSet<Road>(new
	// RoadComparator());

	protected final TreeSet<Road> roadSet = new TreeSet<Road>(new RoadComparator2());

	protected final Set<Road> allMappedRoadsByName = new TreeSet<Road>(new RoadComparator2());

	protected final TreeMap<TwoCities, Double> roadDistances = new TreeMap<TwoCities, Double>();

	/** spatial width and height of the PR Quadtree */
	protected int spatialWidth, spatialHeight;

	/**
	 * Set the DOM Document tree to send the of processed commands to.
	 * 
	 * Creates the root results node.
	 * 
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 * 
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());

		String id = node.getAttribute("id");

		if (!(id.isEmpty())) {
			commandNode.setAttribute("id", id);
		}

		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode, final String attributeName,
			final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode, final String attributeName,
			final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command, final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private void addSuccessNode(final Element command, final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
	}

	private Element addSuccessNodeWithLink(final Element command, final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
		return success;
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		spatialWidth = Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight = Integer.parseInt(node.getAttribute("spatialHeight"));

		/* initialize canvas */
		Canvas.instance.setFrameSize(spatialWidth, spatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth : spatialHeight,
				(spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, Color.WHITE, true);
		Canvas.instance.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);

		/* set PR Quadtree range */
		prQuadtree.setRange(spatialWidth, spatialHeight);
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);
		final String color = processStringAttribute(node, "color", parametersNode);

		/* create the city */
		final City city = new City(name, x, y, radius, color, false);

		/*
		 * if(city.getX() < 0 || city.getY() < 0){
		 * addErrorNode("cityOutOfBounds", commandNode, parametersNode); return;
		 * }
		 */

		if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode, parametersNode);
			return;
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
			return;
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);
			treapByName.put(name, city);

			Tuple addTuple = new Tuple(city.getX(), city.getY());
			treapByLocation.put(city, addTuple);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a deleteCity command. Deletes a city from the dictionary. An
	 * error occurs if the city does not exist or is currently mapped.
	 * 
	 * @param node
	 *            deleteCity node being processed
	 */
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			/* city with name does not exist */
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
			return;
		} else {
			/* delete city */
			final Element outputNode = results.createElement("output");
			final City deletedCity = citiesByName.get(name);

			if (prQuadtree.contains(name)) {
				/* city is mapped */
				prQuadtree.remove(deletedCity);
				addCityNode(outputNode, "cityUnmapped", deletedCity);
			}

			citiesByName.remove(name);
			citiesByLocation.remove(deletedCity);
			treapByName.remove(deletedCity.getName());
			treapByLocation.remove(deletedCity);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		adjList.clear();
		allIsolatedCitiesByName.clear();
		allMappedCitiesByName.clear();
		allMappedRoadsByName.clear();
		citiesByLocation.clear();
		citiesByName.clear();
		pmQuadtree.clear();
		prQuadtree.clear();
		roadDistances.clear();
		roadSet.clear();
		treapByName.clear();
		treapByLocation.clear();

		/* clear canvas */
		Canvas.instance.clear();
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy", parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");

			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName, final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("x", Integer.toString(city.getX()));
		cityNode.setAttribute("y", Integer.toString(city.getY()));
		cityNode.setAttribute("radius", Integer.toString(city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	private void addRoadNode(final Element node, final Road road) {
		Element roadNode = results.createElement("road");
		roadNode.setAttribute("start", road.getStartCity().toString());
		roadNode.setAttribute("end", road.getEndCity().toString());
		node.appendChild(roadNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}

	private void addIsolatedCityNode(final Element node, final City city) {
		addCityNode(node, "isolatedCity", city);
	}

	/**
	 * Maps a city to the spatial map.
	 * 
	 * @param node
	 *            mapCity command node to be processed
	 */
	public void processMapCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
			return;
		} else if (prQuadtree.contains(name)) {
			addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
			return;
		}
		City city = new City(citiesByName.get(name), true);

		if (city.getX() < 0 || city.getX() > spatialWidth) {
			addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			return;
		}

		if (city.getY() < 0 || city.getY() > spatialHeight) {
			addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			return;
		}

		try {

			/* insert city into PR Quadtree */
			prQuadtree.add(city);

		} catch (CityAlreadyMappedException e) {
			addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
			return;
		} catch (CityOutOfBoundsException e) {
			addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			return;
		}
		allMappedCitiesByName.put(city, city.getRadius());
		allIsolatedCitiesByName.put(city, city.getRadius());

		Rectangle rect = new Rectangle(0, 0, spatialWidth, spatialHeight);

		pmQuadtree.add(city, rect);

		/* add city to canvas */
		Canvas.instance.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);

	}

	/**
	 * Removes a city from the spatial map.
	 * 
	 * @param node
	 *            unmapCity command node to be processed
	 */
	public void processUnmapCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
			return;
		} else if (!prQuadtree.contains(name)) {
			addErrorNode("cityNotMapped", commandNode, parametersNode);
			return;
		} else {
			City city = citiesByName.get(name);

			/* unmap the city in the PR Quadtree */
			prQuadtree.remove(city);

			/* remove city from canvas */
			Canvas.instance.removePoint(city.getName(), city.getX(), city.getY(), Color.BLACK);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		/* save canvas to '<name>.png' */
		Canvas.instance.save(name);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Prints out the structure of the PR Quadtree in a human-readable format.
	 * 
	 * @param node
	 *            printPRQuadtree command to be processed
	 */
	public void processPrintPRQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (prQuadtree.isEmpty()) {
			/* empty PR Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		} else {
			/* print PR Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			printPRQuadtreeHelper(prQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the PR Quadtree.
	 * 
	 * @param currentNode
	 *            PR Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PR Quadtree node
	 */
	private void printPRQuadtreeHelper(final Node currentNode, final Element xmlNode) {
		if (currentNode.getType() == Node.EMPTY) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == Node.LEAF) {
				/* leaf node */
				final LeafNode currentLeaf = (LeafNode) currentNode;
				final Element black = results.createElement("black");
				black.setAttribute("name", currentLeaf.getCity().getName());
				black.setAttribute("x", Integer.toString(currentLeaf.getCity().getX()));
				black.setAttribute("y", Integer.toString(currentLeaf.getCity().getY()));
				xmlNode.appendChild(black);
			} else {
				/* internal node */
				final InternalNode currentInternal = (InternalNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString(currentInternal.getCenterX()));
				gray.setAttribute("y", Integer.toString(currentInternal.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPRQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}

	public void processRangeRoads(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final ArrayList<Road> roadsInRange = new ArrayList<Road>();

		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}

		TreeSet<Road> roadSet2 = new TreeSet<Road>(new RoadComparator2());
		roadSet2.addAll(roadSet);

		Point2D pt = new Point2D.Double(x, y);
		// System.out.println("Radius: " +radius);
		for (Road r : roadSet2) {
			// System.out.println("Road: " + r.getStartCity() + " "
			// +r.getEndCity() +" "
			// +r.getRoad().ptSegDist(pt));

			if (r.getRoad().ptSegDist(pt) <= radius) {
				roadsInRange.add(r);
			}
		}

		Element roadList = results.createElement("roadList");

		Collections.sort(roadsInRange, new RoadComparator3());

		if (roadsInRange.size() == 0) {
			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
			return;
		}

		for (Road r : roadsInRange) {
			// System.out.println(r.getStartCity().getName() +" "
			// +r.getEndCity().getName());
			addRoadNode(roadList, r);
		}

		outputNode.appendChild(roadList);
		addSuccessNode(commandNode, parametersNode, outputNode);

		if (pathFile.compareTo("") != 0) {
			if (radius != 0) {
				Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
			}
			Canvas.instance.save(pathFile);
			if (radius != 0) {
				Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
			}
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(new CityNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get cities within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeCitiesHelper(point, radius, prQuadtree.getRoot(), citiesInRange);

		/* print out cities within range */
		if (citiesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
			return;
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("cityList");
			for (City city : citiesInRange) {
				addCityNode(cityListNode, city);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				if (radius != 0) {
					Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
				}
				Canvas.instance.save(pathFile);
				if (radius != 0) {
					Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
				}
			}
		}
	}

	/**
	 * Determines if any cities within the PR Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PR Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeCitiesHelper(final Point2D.Double point, final int radius, final Node node,
			final TreeSet<City> citiesInRange) {
		if (node.getType() == Node.LEAF) {
			final LeafNode leaf = (LeafNode) node;
			final double distance = point.distance(leaf.getCity().toPoint2D());
			if (distance <= radius) {
				/* city is in range */
				final City city = leaf.getCity();
				citiesInRange.add(city);
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final InternalNode internal = (InternalNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (prQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeCitiesHelper(point, radius, internal.getChild(i), citiesInRange);
				}
			}
		}
	}

	/**
	 * Finds the nearest city to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestCity(Element node) {

		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final Element x_label = results.createElement("x");
		final Element y_label = results.createElement("y");

		String x_value = node.getAttribute("x");
		String y_value = node.getAttribute("y");

		x_label.setAttribute("value", x_value);
		y_label.setAttribute("value", y_value);
		parametersNode.appendChild(x_label);
		parametersNode.appendChild(y_label);

		if (allMappedCitiesByName.isEmpty() || prQuadtree.isEmpty()) {
			addErrorNode("cityNotFound", commandNode, parametersNode);

			return;
		}

		double x = Double.parseDouble(node.getAttribute("x"));

		double y = Double.parseDouble(node.getAttribute("y"));

		double min = Double.MAX_VALUE;

		Point2D.Float point = new Point2D.Float((float) x, (float) y);

		PriorityQueue<cmsc420.pmquadtree.PMQuadtree.Node> pq = new PriorityQueue<cmsc420.pmquadtree.PMQuadtree.Node>(
				new ByPointComparator(point));

		pq.add(pmQuadtree.getRoot());

		cmsc420.pmquadtree.PMQuadtree.Node curr = pq.poll();

		City resultAdd = null;

		if (curr.nodeType().compareTo("WhiteNode") == 0) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		if (curr.nodeType().equals("BlackNode")) {
			Black nHolder = (Black) curr;
			if (!nHolder.getCity().CityIsIso()) {
				resultAdd = nHolder.getCity();
			}
		}
		boolean isoCheck;
		do {

			if (curr.nodeType().equals("GrayNode")) {
				Gray gHolder = (Gray) curr;

				pq.add(gHolder.getChild(0));
				pq.add(gHolder.getChild(1));
				pq.add(gHolder.getChild(2));
				pq.add(gHolder.getChild(3));

			}

			if (curr.nodeType().equals("BlackNode")) {

				Black bHolder = (Black) curr;

				if (bHolder.cityCounter == 1) {
					isoCheck = bHolder.getCity().CityIsIso();
					if (isoCheck == false) {
						double distance = distanceCalculator(bHolder.getCity().getX(), bHolder.getCity().getY(), x, y);
						if (distance < min) {
							min = distance;
							resultAdd = bHolder.getCity();
							continue;
						}
					}
				}

			}

			curr = pq.poll();
		} while (!pq.isEmpty());

		if (resultAdd == null) {

			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}

		addCityNode(outputNode, resultAdd);
		addSuccessNode(commandNode, parametersNode, outputNode);

	}

	public void processNearestIsolatedCity(Element node) {

		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final Element x_label = results.createElement("x");
		final Element y_label = results.createElement("y");

		String x_value = node.getAttribute("x");
		String y_value = node.getAttribute("y");

		x_label.setAttribute("value", x_value);
		y_label.setAttribute("value", y_value);
		parametersNode.appendChild(x_label);
		parametersNode.appendChild(y_label);

		if (allMappedCitiesByName.isEmpty() || prQuadtree.isEmpty()) {
			addErrorNode("cityNotFound", commandNode, parametersNode);

			return;
		}

		double x = Double.parseDouble(node.getAttribute("x"));

		double y = Double.parseDouble(node.getAttribute("y"));

		double min = Double.MAX_VALUE;

		Point2D.Float point = new Point2D.Float((float) x, (float) y);

		PriorityQueue<cmsc420.pmquadtree.PMQuadtree.Node> pq = new PriorityQueue<cmsc420.pmquadtree.PMQuadtree.Node>(
				new ByPointComparator2(point));

		pq.add(pmQuadtree.getRoot());

		cmsc420.pmquadtree.PMQuadtree.Node curr = pq.poll();

		City resultAdd = null;

		if (curr.nodeType().compareTo("WhiteNode") == 0) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		if (curr.nodeType().equals("BlackNode")) {
			Black nHolder = (Black) curr;
			if (nHolder.getCity().CityIsIso()) {
				resultAdd = nHolder.getCity();
			}
		}
		boolean isoCheck;
		do {

			if (curr.nodeType().equals("GrayNode")) {
				Gray gHolder = (Gray) curr;

				pq.add(gHolder.getChild(0));
				pq.add(gHolder.getChild(1));
				pq.add(gHolder.getChild(2));
				pq.add(gHolder.getChild(3));

			}

			if (curr.nodeType().equals("BlackNode")) {

				Black bHolder = (Black) curr;

				if (bHolder.cityCounter == 1) {
					isoCheck = bHolder.getCity().CityIsIso();
					if (isoCheck == true) {
						double distance = distanceCalculator(bHolder.getCity().getX(), bHolder.getCity().getY(), x, y);
						if (distance < min) {
							min = distance;
							resultAdd = bHolder.getCity();
						}
					}
				}

			}
			curr = pq.poll();
		} while (!pq.isEmpty());

		if (resultAdd == null) {

			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}

		addIsolatedCityNode(outputNode, resultAdd);
		addSuccessNode(commandNode, parametersNode, outputNode);

	}

	class QuadrantDistance implements Comparable<QuadrantDistance> {
		public Node quadtreeNode;
		private double distance;

		public QuadrantDistance(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				distance = Shape2DDistanceCalculator.distance(pt,
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				LeafNode leaf = (LeafNode) node;
				distance = pt.distance(leaf.getCity().pt);
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}

		@Override
		public int compareTo(QuadrantDistance qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
					if (quadtreeNode.getType() == Node.INTERNAL) {
						return -1;
					} else {
						return 1;
					}
				} else if (quadtreeNode.getType() == Node.LEAF) {
					// both are leaves
					return ((LeafNode) qd.quadtreeNode).getCity().getName()
							.compareTo(((LeafNode) quadtreeNode).getCity().getName());
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}

	public void processMapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		Element start_label = results.createElement("start");
		String start_value = node.getAttribute("start");

		start_label.setAttribute("value", start_value);

		Element end_label = results.createElement("end");
		String end_value = node.getAttribute("end");

		end_label.setAttribute("value", end_value);
		parametersNode.appendChild(start_label);
		parametersNode.appendChild(end_label);

		if (citiesByName.get(start_value) == null) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
			return;
		}

		if (citiesByName.get(end_value) == null) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
			return;
		}

		City startCity;
		City endCity;

		if (start_value.compareTo(end_value) < 0) {
			startCity = citiesByName.get(start_value);
			endCity = citiesByName.get(end_value);
		} else {
			endCity = citiesByName.get(start_value);
			startCity = citiesByName.get(end_value);
		}

		if (startCity.equals(endCity)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
			return;
		}

		if (allIsolatedCitiesByName.containsKey(citiesByName.get(start_value))
				|| allIsolatedCitiesByName.containsKey(citiesByName.get(end_value))) {
			addErrorNode("startOrEndIsIsolated", commandNode, parametersNode);
			return;
		}

		if (allMappedRoadsByName.contains(new Road(endCity, startCity))) {
			addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			// System.out.println("NOT MAPPED. START POINT: " +startCity + " END
			// POINT: "
			// +endCity);
			return;
		}

		if (allMappedRoadsByName.contains(new Road(startCity, endCity))) {
			addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			// System.out.println("NOT MAPPED. START POINT: " +startCity + " END
			// POINT: "
			// +endCity);
			return;
		}

		Road holder = new Road(startCity, endCity);
		Rectangle checker = new Rectangle(0, 0, spatialWidth, spatialHeight);

		if (!Inclusive2DIntersectionVerifier.intersects(holder.getSeg(), checker)) {
			addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			return;
		}

		// System.out.println("STARTING CITY: " +startCity +" AT X: "
		// +startCity.getX()
		// + " ENDING CITY: " +endCity);

		if (!allMappedCitiesByName.containsKey(startCity)) {
			startCity.setIsIsolated(false);
			City city = startCity;

			if (city.getX() < 0 || city.getX() > spatialWidth) {
			}

			if (city.getY() < 0 || city.getY() > spatialHeight) {
			} else {
				try {
					prQuadtree.add(city);
				} catch (CityAlreadyMappedException e) {
					addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
					return;
				} catch (CityOutOfBoundsException e) {
					// addErrorNode("roadOutOfBounds", commandNode,
					// parametersNode);
					return;
				}
				allMappedCitiesByName.put(city, city.getRadius());

				Rectangle rect = new Rectangle(0, 0, spatialWidth, spatialHeight);

				pmQuadtree.add(city, rect);

				/* add city to canvas */
				Canvas.instance.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
			}
		}

		if (!allMappedCitiesByName.containsKey(endCity)) {
			endCity.setIsIsolated(false);
			City city = endCity;

			if (city.getX() < 0 || city.getX() > spatialWidth) {

			}

			if (city.getY() < 0 || city.getY() > spatialHeight) {

			} else {

				try {
					prQuadtree.add(city);
				} catch (CityAlreadyMappedException e) {
					addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
					return;
				} catch (CityOutOfBoundsException e) {
					// addErrorNode("roadOutOfBounds", commandNode,
					// parametersNode);
					return;
				}
				allMappedCitiesByName.put(city, city.getRadius());

				Rectangle rect = new Rectangle(0, 0, spatialWidth, spatialHeight);

				pmQuadtree.add(city, rect);
				// adjList.add(startCity, endCity);

				/* add city to canvas */
				Canvas.instance.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
			}
		}

		Point2D.Float startPoint = new Point2D.Float(startCity.getX(), startCity.getY());
		Point2D.Float endPoint = new Point2D.Float(endCity.getX(), endCity.getY());

		Road toAdd = holder;

		Rectangle rect = new Rectangle(0, 0, spatialWidth, spatialHeight);

		roadSet.add(toAdd);

		TwoCities addTwo = new TwoCities(startCity, endCity);

		allMappedRoadsByName.add(new Road(startCity, endCity));

		double dist = distanceCalculator(toAdd);

		roadDistances.put(addTwo, dist);

		// System.out.println(toAdd.getStartCity().getName());
		// System.out.println(toAdd.getEndCity().getName());
		pmQuadtree.add(toAdd, rect);

		adjList.add(startCity, endCity);
		adjList.add(endCity, startCity);
		adjList.updateDists(roadDistances);

		Canvas.instance.addLine(startCity.getX(), startCity.getY(), endCity.getX(), endCity.getY(), Color.BLACK);

		Element roadCreated = results.createElement("roadCreated");
		roadCreated.setAttribute("start", start_value);
		roadCreated.setAttribute("end", end_value);

		outputNode.appendChild(roadCreated);

		addSuccessNode(commandNode, parametersNode, outputNode);

	}

	private double distanceCalculator(Road r1) {
		return Math.hypot(r1.seg.getX1() - r1.seg.getX2(), r1.seg.getY1() - r1.seg.getY2());
	}

	private double distanceCalculator(double x1, double y1, double x2, double y2) {
		return Math.hypot(x1 - x2, y1 - y2);
	}

	// Distance between a point and the closest point to it on a line segment
	private double distanceCalculator(int x, int y, Road searchRoad) {
		double deltaX = searchRoad.getEnd().getX() - searchRoad.getStart().getX();
		double deltaY = searchRoad.getEnd().getY() - searchRoad.getStart().getY();

		double u = ((x - searchRoad.getStart().getX()) * deltaX + (y - searchRoad.getStart().getY()) * deltaY)
				/ (deltaX * deltaX + deltaY * deltaY);

		double closestX;
		double closestY;

		if (u < 0) {
			closestX = searchRoad.getStart().getX();
			closestY = searchRoad.getStart().getY();
		} else if (u > 1) {
			closestX = searchRoad.getEnd().getX();
			closestY = searchRoad.getEnd().getY();
		} else {
			closestX = Math.round(searchRoad.getStart().getX() + u * deltaX);
			closestY = Math.round(searchRoad.getStart().getY() + u * deltaY);
		}

		return distanceCalculator(x, y, closestX, closestY);

		// Used
		// http://www.java2s.com/Code/Java/2D-Graphics-GUI/Returnsclosestpointonsegmenttopoint.htm
		// as reference
	}

	public void processPrintPMQuadtree(Element node) {

		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final Element pmqNode = results.createElement("quadtree");
		pmqNode.setAttribute("order", "3");

		if (pmQuadtree.getRoot().nodeType().equals("WhiteNode")) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		// pmqNode.setAttribute("order", value);

		pmqNode.appendChild(pmQuadtree.print(results));

		outputNode.appendChild(pmqNode);

		addSuccessNode(commandNode, parametersNode, outputNode);

	}

	public void processPrintTreap(Element node) {

		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final Element treapNode = results.createElement("treap");

		treapNode.setAttribute("cardinality", Integer.toString(treapByLocation.size()));

		if (treapByLocation.isEmpty() || treapByName.isEmpty() || treapByLocation.getRoot() == null
				|| treapByName.getRoot() == null) {
			addErrorNode("emptyTree", commandNode, parametersNode);
			return;
		}

		treapNode.appendChild(treapByLocation.printTreap(results));
		/*
		 * System.out.println("Equality Checks: ");
		 * System.out.println(treapByName.equals(citiesByName));
		 * System.out.println("-----"); System.out.println("Treap: " +
		 * treapByName.toString()); System.out.println("TreeMap: " +
		 * citiesByName.toString()); System.out.println("-----");
		 * System.out.println("Treap Hashcode: " + treapByLocation.hashCode());
		 * System.out.println("TreeMap Hashcode: "
		 * +citiesByLocation.hashCode()); System.out.println();
		 */
		outputNode.appendChild(treapNode);

		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	public void processShortestPath(Element node)
			throws IOException, TransformerException, ParserConfigurationException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final Element start = results.createElement("start");
		final Element end = results.createElement("end");

		City startCity = citiesByName.get(node.getAttribute("start"));
		City endCity = citiesByName.get(node.getAttribute("end"));

		Element saveMap = results.createElement("saveMap");

		start.setAttribute("value", node.getAttribute("start"));
		end.setAttribute("value", node.getAttribute("end"));
		parametersNode.appendChild(start);
		parametersNode.appendChild(end);

		if (startCity == null || !allMappedCitiesByName.containsKey(startCity)) {
			addErrorNode("nonExistentStart", commandNode, parametersNode);
			return;
		}

		if (startCity.getX() < 0 || startCity.getY() < 0 || startCity.getX() > spatialWidth
				|| startCity.getY() > spatialHeight) {
			addErrorNode("nonExistentStart", commandNode, parametersNode);
			return;
		}

		if (endCity == null || !allMappedCitiesByName.containsKey(endCity)) {
			addErrorNode("nonExistentEnd", commandNode, parametersNode);
			return;
		}

		if (endCity.getX() < 0 || endCity.getY() < 0 || endCity.getX() > spatialWidth
				|| endCity.getY() > spatialHeight) {
			addErrorNode("nonExistentEnd", commandNode, parametersNode);
			return;
		}

		CanvasPlus canvas = new CanvasPlus();
		boolean toSave = false;

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
			saveMap.setAttribute("value", pathFile);
		}

		Element saveHTML = results.createElement("saveMap");
		String pathFile2 = "";
		if (node.getAttribute("saveHTML").compareTo("") != 0) {
			pathFile2 = processStringAttribute(node, "saveHTML", parametersNode);
			saveHTML.setAttribute("value", pathFile);
		}

		if (pathFile.compareTo("") != 0 || pathFile2.compareTo("") != 0) {
			toSave = true;
			canvas.setFrameSize(spatialWidth, spatialWidth);
			canvas.addRectangle(0, 0, spatialWidth, spatialWidth, Color.BLACK, false);

			if (node.getAttribute("saveMap").compareTo("") != 0) {
				pathFile = processStringAttribute(node, "saveMap", parametersNode);
				saveMap.setAttribute("value", pathFile);
			}

		}

		Element holder = shortestPathHelper(startCity, endCity, canvas, toSave);

		if (holder.hasAttribute("isolatedCityInPath")) {
			addErrorNode("noPathExists", commandNode, parametersNode);
			return;
		}

		outputNode.appendChild(holder);

		if (pathFile.compareTo("") != 0) {

			if (node.getAttribute("saveMap").compareTo("") != 0) {
				canvas.save(pathFile);
			}

		}

		addSuccessNodeWithLink(commandNode, parametersNode, outputNode);

		// Element successNode = addSuccessNodeWithLink(commandNode,
		// parametersNode,
		// outputNode);
		/*
		 * if (node.getAttribute("saveHTML").compareTo("") != 0) {
		 * canvas.save(pathFile2); org.w3c.dom.Document shortestPathDoc =
		 * XmlUtility.getDocumentBuilder().newDocument(); org.w3c.dom.Node
		 * spNode = shortestPathDoc.importNode(successNode, true);
		 * shortestPathDoc.appendChild(spNode);
		 * XmlUtility.transform(shortestPathDoc, new File("shortestPath.xsl"),
		 * new File(pathFile2 + ".html")); }
		 */
		// canvas.dispose();
	}

	public Element shortestPathHelper(City start, City end, CanvasPlus canvas, boolean toSave) {

		final Element path = results.createElement("path");

		int hops = 0;

		double tDistance = 0;

		TreeMap<City, Double> distances = new TreeMap<City, Double>(new CityNameComparator());

		TreeMap<City, City> pred = new TreeMap<City, City>(new CityNameComparator());

		PriorityQueue<Pair> pq = new PriorityQueue<Pair>(new distanceToComparator(start));

		pred.put(start, null);

		if (toSave) {
			canvas.addPoint(start.getName(), start.getX(), start.getY(), Color.GREEN);
		}

		if (toSave) {
			canvas.addPoint(end.getName(), end.getX(), end.getY(), Color.RED);
		}

		pq.add(new Pair(start, 0));

		for (City c : allMappedCitiesByName.keySet()) {

			distances.put(c, Double.MAX_VALUE);
		}

		TreeSet<City> visitedSet = new TreeSet<City>(new CityNameComparator());

		distances.put(start, 0.0);

		while (!pq.isEmpty()) {

			Double newDist;

			Pair u;

			// System.out.println(u.getCity().getName() + " is adjacent to: ");

			u = pq.poll();

			if (visitedSet.contains(u.getCity())) {
				continue;
			}
			visitedSet.add(u.getCity());

			for (City v : adjList.getAdjacents(u.getCity())) {
				// System.out.println(v.getName());

				double distTo = Math.hypot(u.getCity().getX() - v.getX(), u.getCity().getY() - v.getY());

				if (distTo != -1) {

					newDist = u.getDistance() + distTo;

					if (distances.get(v) >= newDist) {
						if (distances.get(v).equals(newDist)) {

							if (pred.get(v).getName().compareTo(u.getCity().getName()) > 0) {
								continue;
							}
						} else {
							distances.put(v, newDist);

							pq.add(new Pair(v, newDist));
							pred.put(v, u.getCity());
						}
					}
				}

			}
		}

		City curr = end;
		City currPred;

		ArrayList<CityAndPred> sol = new ArrayList<CityAndPred>();

		// System.out.println("End is: " +end +", Start is: " +start);

		while (curr != start) {

			currPred = pred.get(curr);

			// System.out.println("Starts at: " +curr + " and Goes to: "
			// +currPred);

			sol.add(new CityAndPred(curr, currPred));
			hops++;

			if (curr == null || currPred == null || curr.CityIsIso()) {
				Element holder = results.createElement("holder");
				holder.setAttribute("isolatedCityInPath", "true");
				return holder;
			}

			double distance1 = Math.hypot(curr.getX() - currPred.getX(), curr.getY() - currPred.getY());

			tDistance = tDistance + distance1;

			curr = currPred;
		}

		CityAndPred previousRoad = null;

		for (int j = sol.size() - 1; j >= 0; j--) {

			if (toSave) {
				canvas.addPoint(sol.get(j).getCity().getName(), sol.get(j).getCity().getX(),
						sol.get(j).getCity().getY(), Color.BLUE);
			}

			if (previousRoad != null) {
				// double angle = angleCalc(previousRoad.getCity(),
				// previousRoad.getPred(),
				// sol.get(j).getCity());

				if (toSave) {
					canvas.addLine(sol.get(j).getCity().getX(), sol.get(j).getCity().getX(),
							sol.get(j).getPred().getX(), sol.get(j).getPred().getX(), Color.BLUE);
				}

				double angle = angleCalc(previousRoad.getPred(), previousRoad.getCity(), sol.get(j).getCity());

				// if(angle < 0) angle = angle*-1;

				// angle = 180 - angle;

				if (angle < 180 && angle >= 45) {
					// Turn right
					final Element turn = results.createElement("right");
					path.appendChild(turn);
					// System.out.println(angle +", turn right");
				}

				if (angle > -45 && angle < 45) {
					// Go straight
					final Element turn = results.createElement("straight");
					path.appendChild(turn);
					// System.out.println(angle +", go straight");
				}

				if (angle > -180 && angle < -45) {
					// Turn left
					final Element turn = results.createElement("left");
					path.appendChild(turn);
					// System.out.println(angle +", turn left");
				}

			}

			final Element road = results.createElement("road");
			road.setAttribute("start", sol.get(j).getPred().getName());
			road.setAttribute("end", sol.get(j).getCity().getName());
			path.appendChild(road);
			previousRoad = sol.get(j);
		}

		DecimalFormat df = new DecimalFormat("#.000");

		if (tDistance == 0) {
			path.setAttribute("length", "0.000");
		} else {
			path.setAttribute("length", df.format(tDistance));
		}
		path.setAttribute("hops", Integer.toString(hops));

		return path;
	}

	private double angleCalc(City start, City middle, City end) {

		Point2D.Double p1 = new Point2D.Double(start.getX(), start.getY());
		Point2D.Double p2 = new Point2D.Double(middle.getX(), middle.getY());
		Point2D.Double p3 = new Point2D.Double(end.getX(), end.getY());

		Arc2D.Double arc = new Arc2D.Double();

		arc.setArcByTangent(p1, p2, p3, 1);

		return arc.getAngleExtent();

	}

	public void processNearestRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final double x = processIntegerAttribute(node, "x", parametersNode);
		final double y = processIntegerAttribute(node, "y", parametersNode);

		// final Point2D.Float point = new Point2D.Float(x, y);

		double deltaX;
		double deltaY;
		Road result = null;
		double min = Double.MAX_VALUE;

		for (Road r : roadSet) {
			deltaX = r.getEnd().getX() - r.getStart().getX();
			deltaY = r.getEnd().getY() - r.getStart().getY();

			double u = ((x - r.getStart().getX()) * deltaX + (y - r.getStart().getY()) * deltaY)
					/ (deltaX * deltaX + deltaY * deltaY);

			double closestX;
			double closestY;

			if (u < 0) {
				closestX = r.getStart().getX();
				closestY = r.getStart().getY();
			} else if (u > 1) {
				closestX = r.getEnd().getX();
				closestY = r.getEnd().getY();
			} else {
				closestX = Math.round(r.getStart().getX() + u * deltaX);
				closestY = Math.round(r.getStart().getY() + u * deltaY);
			}

			double distance = distanceCalculator(x, y, closestX, closestY);
			if (distance < min) {
				min = distance;
				result = r;
			}
		}

		if (result == null) {
			addErrorNode("roadNotFound", commandNode, parametersNode);
		} else {

			final Element roadNode = results.createElement("road");
			roadNode.setAttribute("start", result.getStartCity().getName());
			roadNode.setAttribute("end", result.getEndCity().getName());
			outputNode.appendChild(roadNode);
			addSuccessNodeWithLink(commandNode, parametersNode, outputNode);
		}
	}

	public void processNearestCityToRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String start_label = processStringAttribute(node, "start", parametersNode);
		final String end_label = processStringAttribute(node, "end", parametersNode);

		City startCity = treapByName.get(start_label);

		City endCity = treapByName.get(end_label);

		if (startCity == null || endCity == null || startCity.CityIsIso() || endCity.CityIsIso()) {
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
			return;
		}

		if (!roadSet.contains(new Road(startCity, endCity)) && !roadSet.contains(new Road(endCity, startCity))) {
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
			if (allMappedCitiesByName.size() <= 2) {
				addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
			}
			return;

		}

		if (roadSet.size() == 1) {
			if (treapByName.containsKey(startCity.getName()) && treapByName.containsKey(endCity.getName())
					&& treapByName.size() == 2) {
				addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
				return;
			}
		}

		Road searchRoad = new Road(startCity, endCity);
		Double min = Double.MAX_VALUE;

		PriorityQueue<cmsc420.pmquadtree.PMQuadtree.Node> pq = new PriorityQueue<cmsc420.pmquadtree.PMQuadtree.Node>(
				new ByLineComparator(searchRoad));

		pq.add(pmQuadtree.getRoot());

		cmsc420.pmquadtree.PMQuadtree.Node curr = pq.poll();

		City resultAdd = null;

		if (curr.nodeType().compareTo("WhiteNode") == 0) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		if (curr.nodeType().equals("BlackNode")) {
			Black nHolder = (Black) curr;
			resultAdd = nHolder.getCity();
		}
		boolean isoCheck;
		do {

			if (curr.nodeType().equals("GrayNode")) {
				Gray gHolder = (Gray) curr;

				pq.add(gHolder.getChild(0));
				pq.add(gHolder.getChild(1));
				pq.add(gHolder.getChild(2));
				pq.add(gHolder.getChild(3));

			}

			if (curr.nodeType().equals("BlackNode")) {

				Black bHolder = (Black) curr;

				if (bHolder.cityCounter == 1) {
					double distance = distanceCalculator(bHolder.getCity().getX(), bHolder.getCity().getY(),
							searchRoad);
					if (distance < min) {

						if (bHolder.getCity().getName().compareTo(startCity.getName()) != 0
								&& bHolder.getCity().getName().compareTo(endCity.getName()) != 0) {
							min = distance;
							resultAdd = bHolder.getCity();
							continue;
						}
					}

				}

			}

			curr = pq.poll();
		} while (!pq.isEmpty());

		if (resultAdd == null) {

			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		addCityNode(outputNode, resultAdd);
		addSuccessNode(commandNode, parametersNode, outputNode);

	}

}
