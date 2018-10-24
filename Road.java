package cmsc420.structure;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import cmsc420.geom.Geometry2D;

public class Road implements Geometry2D {

	public Line2D.Double seg;
	City start;
	City end;

	public Road(Point2D.Float start, Point2D.Float end) {

		seg = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());
	}

	public Road(Line2D in) {
		seg = new Line2D.Double(in.getP1(), in.getP2());
	}

	public Road(Road g) {
		seg = g.seg;
		start = g.start;
		end = g.end;
	}

	public Road(City a, City b) {
		seg = new Line2D.Double(a.toPoint2D(), b.toPoint2D());
		start = a;
		end = b;
	}

	public Line2D getSeg() {
		return seg;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 1;
	}

	public Point2D getStart() {
		return seg.getP1();
	}

	public Point2D getEnd() {
		return seg.getP2();
	}

	public Line2D getRoad() {
		return seg;
	}

	public City getStartCity() {
		return start;
	}

	public City getEndCity() {
		return end;
	}

	public void setStartCity(City s) {
		start = s;
	}

	public void setEndCity(City e) {
		end = e;
	}

}
