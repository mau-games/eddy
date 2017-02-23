package finder.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

public class PolygonTest {

	@Test
	public void testGetArea() {
		Polygon p = new Polygon();
		p.addPoint(new Point(2, 2));
		p.addPoint(new Point(4, 10));
		p.addPoint(new Point(9, 7));
		p.addPoint(new Point(11, 2));
		
		assertEquals(45.5, p.getArea(), 0);
	}

//	@Test
//	public void testUnion() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testOverlapsWithEqualPolygons() {
		Polygon p1 = new Polygon();
		p1.addPoint(new Point(2, 2));
		p1.addPoint(new Point(4, 10));
		p1.addPoint(new Point(9, 7));
		p1.addPoint(new Point(11, 2));
		
		Polygon p2 = new Polygon();
		p2.addPoint(new Point(2, 2));
		p2.addPoint(new Point(4, 10));
		p2.addPoint(new Point(9, 7));
		p2.addPoint(new Point(11, 2));
		
		assertTrue(p1.overlaps(p2));
	}

	@Test
	public void testOverlapsWithDisjointPolygons() {
		Polygon p1 = new Polygon();
		p1.addPoint(new Point(2, 2));
		p1.addPoint(new Point(4, 10));
		p1.addPoint(new Point(9, 7));
		p1.addPoint(new Point(11, 2));
		
		Polygon p2 = new Polygon();
		p2.addPoint(new Point(20, 20));
		p2.addPoint(new Point(40, 100));
		p2.addPoint(new Point(90, 70));
		p2.addPoint(new Point(110, 20));
		
		assertFalse(p1.overlaps(p2));
	}

	@Test
	public void testOverlapsWithOverlappingPolygons() {
		Polygon p1 = new Polygon();
		p1.addPoint(new Point(2, 2));
		p1.addPoint(new Point(4, 10));
		p1.addPoint(new Point(9, 7));
		p1.addPoint(new Point(11, 2));
		
		Polygon p2 = new Polygon();
		p2.addPoint(new Point(0, 0));
		p2.addPoint(new Point(1, 6));
		p2.addPoint(new Point(6, 5));
		p2.addPoint(new Point(6, 0));
		
		assertTrue(p1.overlaps(p2));
	}

	@Test
	public void testContainsPoint() {
		Polygon polygon = new Polygon();
		polygon.addPoint(new Point(2, 2));
		polygon.addPoint(new Point(4, 10));
		polygon.addPoint(new Point(9, 7));
		polygon.addPoint(new Point(11, 2));
		
		Point point = new Point(4, 4);
		
		assertTrue(polygon.contains(point));
	}

	@Test
	public void testContainsPointNot() {
		Polygon polygon = new Polygon();
		polygon.addPoint(new Point(2, 2));
		polygon.addPoint(new Point(4, 10));
		polygon.addPoint(new Point(9, 7));
		polygon.addPoint(new Point(11, 2));
		
		Point point = new Point(13, 13);
		
		assertFalse(polygon.contains(point));
	}
}
