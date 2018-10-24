package cmsc420.pmquadtree;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.geom.Geometry2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.structure.City;
import cmsc420.structure.Road;
import cmsc420.structure.RoadComparator3;

public class PMQuadtree {

	public abstract class Node {

		abstract Node add(Geometry2D g, Rectangle rect);

		abstract Node remove(Geometry2D g, Rectangle rect);

		public abstract String nodeType();

		abstract Element printNode(Document results);
	}

	White SingletonWhiteNode = new White();

	Node root = SingletonWhiteNode;

	public PMQuadtree() {
		root = SingletonWhiteNode;
	}

	public void add(Geometry2D g, Rectangle rect) {
		root = root.add(g, rect);
	}

	public void remove(Geometry2D g, Rectangle newRect) {
		root = root.remove(g, newRect);

	}

	public Node getRoot() {
		return root;
	}

	public void clear() {
		root = SingletonWhiteNode;
	}

	public class White extends Node {

		private White whiteNode;

		private White() {
		}

		public White getInstance() {
			if (whiteNode == null) {
				whiteNode = new White();

			}

			return whiteNode;
		}

		@Override
		Node add(Geometry2D g, Rectangle rect) {

			return new Black(g, rect);
		}

		@Override
		Node remove(Geometry2D g, Rectangle rect) {
			// TODO Auto-generated method stub
			return whiteNode;
		}

		@Override
		public String nodeType() {
			// TODO Auto-generated method stub
			return "WhiteNode";
		}

		@Override
		Element printNode(Document results) {
			final Element whiteNode = results.createElement("white");

			return whiteNode;
		}

	}

	public class Black extends Node {
		public ArrayList<Road> roadList = new ArrayList<Road>();
		public City city;

		public Rectangle bound;
		public int cityCounter = 0;

		public Black(Geometry2D g, Rectangle rect) {
			bound = rect;

			if (g.getType() == 0) {
				city = (City) g;
				cityCounter++;

				if (root.nodeType().equals("WhiteNode")) {
					root = this;
				}
			} else {
				roadList.add((Road) g);
			}

		}

		@Override
		Node add(Geometry2D g, Rectangle rect) {

			if (g.getType() == 1) {// An edge

				roadList.add((Road) g);

				return this;

			} else if (g.getType() == 0) {// Another city

				if (cityCounter == 0) {
					city = (City) g;
					cityCounter++;
					return this;
				} else {
					City c = (City) g;
					return partition(g, rect).add(c, rect);
				}

			}
			return null;
		}

		@Override
		Node remove(Geometry2D g, Rectangle rect) {
			roadList.remove(g);

			if (roadList.isEmpty())
				return SingletonWhiteNode;

			return this;
		}

		private Node partition(Geometry2D g, Rectangle rect) {
			Gray gray = new Gray(rect);
			gray.add(city, rect);
			for (int i = 0; i < roadList.size(); i++) {
				gray.add(roadList.get(i), rect);
			}

			return gray;
		}

		@Override
		public String nodeType() {
			// TODO Auto-generated method stub
			return "BlackNode";
		}

		@Override
		Element printNode(Document results) {
			final Element blackNode = results.createElement("black");

			int card = roadList.size();

			if (cityCounter == 1) {
				final Element cityNode;

				if (city.CityIsIso()) {
					cityNode = results.createElement("isolatedCity");
				} else {
					cityNode = results.createElement("city");
				}
				cityNode.setAttribute("name", city.getName());
				cityNode.setAttribute("x", Integer.toString(city.getX()));
				cityNode.setAttribute("y", Integer.toString(city.getY()));
				cityNode.setAttribute("radius", Integer.toString(city.getRadius()));
				cityNode.setAttribute("color", city.getColor());
				blackNode.appendChild(cityNode);
				card++;
			}

			blackNode.setAttribute("cardinality", Integer.toString(card));

			Collections.sort(roadList, new RoadComparator3());

			for (Road g : roadList) {
				if (g != null) {

					final Element roadNode = results.createElement("road");

					roadNode.setAttribute("start", g.getStartCity().getName());
					roadNode.setAttribute("end", g.getEndCity().getName());
					blackNode.appendChild(roadNode);
				}
			}

			return blackNode;
		}

		public City getCity() {
			return city;
		}
	}

	public class Gray extends Node {
		public Node[] kids = new Node[4];
		public Rectangle[] regions = new Rectangle[4];
		public int centerx;
		public int centery;
		public int graydim;

		public Gray(Rectangle rect) {

			int x = (int) rect.getX();
			int y = (int) rect.getY();

			int dimension = (int) rect.getHeight() / 2;

			graydim = (int) rect.getHeight();

			centerx = (int) rect.getX() + dimension;
			centery = (int) rect.getY() + dimension;

			Rectangle rect1 = new Rectangle(x, y + dimension, dimension, dimension);

			Rectangle rect2 = new Rectangle(x + dimension, y + dimension, dimension, dimension);

			Rectangle rect3 = new Rectangle(x, y, dimension, dimension);

			Rectangle rect4 = new Rectangle(x + dimension, y, dimension, dimension);

			regions[0] = rect1;
			regions[1] = rect2;
			regions[2] = rect3;
			regions[3] = rect4;

			kids[0] = SingletonWhiteNode;
			kids[1] = SingletonWhiteNode;
			kids[2] = SingletonWhiteNode;
			kids[3] = SingletonWhiteNode;

		}

		@Override
		Node add(Geometry2D g, Rectangle rect) {

			if (g.getType() == 0) {

				City city = (City) g;

				int x = (int) rect.getX();
				int y = (int) rect.getY();
				int dimension = (int) rect.getHeight();

				Point2D.Float pointToCheck = new Point2D.Float(city.getX(), city.getY());

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[1])) {

					Point2D.Float secondKidBotLeft = new Point2D.Float(x + dimension / 2, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) secondKidBotLeft.getX(), (int) secondKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[1] = kids[1].add(city, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[3])) {

					Point2D.Float fourthKidBotLeft = new Point2D.Float(x + dimension / 2, y);

					Rectangle newRect = new Rectangle((int) fourthKidBotLeft.getX(), (int) fourthKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[3] = kids[3].add(city, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[0])) {

					Point2D.Float firstKidBotLeft = new Point2D.Float(x, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) firstKidBotLeft.getX(), (int) firstKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[0] = kids[0].add(city, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[2])) {

					Point2D.Float thirdKidBotLeft = new Point2D.Float(x, y);

					Rectangle newRect = new Rectangle((int) thirdKidBotLeft.getX(), (int) thirdKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[2] = kids[2].add(city, newRect);

				}

				return this;

			} else if (g.getType() == 1) {

				Road holdRoad = (Road) g;

				Road seg = new Road(holdRoad);

				int x = (int) rect.getX();
				int y = (int) rect.getY();
				int dimension = (int) rect.getHeight();

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[1])) {

					Point2D.Float secondKidBotLeft = new Point2D.Float(x + dimension / 2, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) secondKidBotLeft.getX(), (int) secondKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[1] = kids[1].add(seg, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[3])) {

					Point2D.Float fourthKidBotLeft = new Point2D.Float(x + dimension / 2, y);

					Rectangle newRect = new Rectangle((int) fourthKidBotLeft.getX(), (int) fourthKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[3] = kids[3].add(seg, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[0])) {

					Point2D.Float firstKidBotLeft = new Point2D.Float(x, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) firstKidBotLeft.getX(), (int) firstKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[0] = kids[0].add(seg, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[2])) {

					Point2D.Float thirdKidBotLeft = new Point2D.Float(x, y);

					Rectangle newRect = new Rectangle((int) thirdKidBotLeft.getX(), (int) thirdKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[2] = kids[2].add(seg, newRect);
				}

				return this;

			}
			return null;
		}

		@Override
		Node remove(Geometry2D g, Rectangle rect) {

			if (g.getType() == 0) {

				City city = (City) g;

				int x = (int) rect.getX();
				int y = (int) rect.getY();
				int dimension = (int) rect.getHeight();

				Point2D.Float pointToCheck = new Point2D.Float(city.getX(), city.getY());

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[1])) {

					Point2D.Float secondKidBotLeft = new Point2D.Float(x + dimension / 2, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) secondKidBotLeft.getX(), (int) secondKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[1] = kids[1].add(city, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[3])) {

					Point2D.Float fourthKidBotLeft = new Point2D.Float(x + dimension / 2, y);

					Rectangle newRect = new Rectangle((int) fourthKidBotLeft.getX(), (int) fourthKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[3] = kids[3].add(city, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[0])) {

					Point2D.Float firstKidBotLeft = new Point2D.Float(x, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) firstKidBotLeft.getX(), (int) firstKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[0] = kids[0].add(city, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(pointToCheck, regions[2])) {

					Point2D.Float thirdKidBotLeft = new Point2D.Float(x, y);

					Rectangle newRect = new Rectangle((int) thirdKidBotLeft.getX(), (int) thirdKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[2] = kids[2].add(city, newRect);

				}

			} else if (g.getType() == 1) {
				Road seg = new Road((Line2D) g);

				int x = (int) rect.getX();
				int y = (int) rect.getY();
				int dimension = (int) rect.getHeight();

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[1])) {

					Point2D.Float secondKidBotLeft = new Point2D.Float(x + dimension / 2, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) secondKidBotLeft.getX(), (int) secondKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[1] = kids[1].remove(seg, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[3])) {

					Point2D.Float fourthKidBotLeft = new Point2D.Float(x + dimension / 2, y);

					Rectangle newRect = new Rectangle((int) fourthKidBotLeft.getX(), (int) fourthKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[3] = kids[3].remove(seg, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[0])) {

					Point2D.Float firstKidBotLeft = new Point2D.Float(x, y + dimension / 2);

					Rectangle newRect = new Rectangle((int) firstKidBotLeft.getX(), (int) firstKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[0] = kids[0].remove(seg, newRect);

				}

				if (Inclusive2DIntersectionVerifier.intersects(seg.getRoad(), regions[2])) {

					Point2D.Float thirdKidBotLeft = new Point2D.Float(x, y);

					Rectangle newRect = new Rectangle((int) thirdKidBotLeft.getX(), (int) thirdKidBotLeft.getY(),
							dimension / 2, dimension / 2);

					kids[2] = kids[2].remove(seg, newRect);
				}

			}

			int[] counter = new int[2];

			for (int i = 0; i < 4; i++) {
				// System.out.println("Kid " +i +" is a" +kids[i].nodeType());

				if (kids[i].nodeType().compareTo("WhiteNode") == 0) {
					counter[0]++;
				}

				if (kids[i].nodeType().compareTo("BlackNode") == 0) {
					counter[1]++;
				}
			}

			if (counter[0] == 4) {
				return SingletonWhiteNode;
			}

			if (counter[0] == 3 && counter[1] == 1) {
				for (int i = 0; i < 4; i++) {
					if (kids[i].nodeType().compareTo("BlackNode") == 0) {
						Black holder = (Black) kids[i];
						return holder;
					}
				}

			}

			return this;
		}

		@Override
		public String nodeType() {
			// TODO Auto-generated method stub
			return "GrayNode";
		}

		@Override
		Element printNode(Document results) {
			final Element grayNode = results.createElement("gray");

			grayNode.setAttribute("x", Integer.toString(centerx));
			grayNode.setAttribute("y", Integer.toString(centery));

			for (int i = 0; i < 4; i++) {

				// System.out.println("KID " +i +": " +kids[i].nodeType());

				grayNode.appendChild(kids[i].printNode(results));
			}

			return grayNode;
		}

		public Node getChild(int i) {
			return kids[i];
		}

	}

	public Element print(Document results) {
		return root.printNode(results);

	}

}
