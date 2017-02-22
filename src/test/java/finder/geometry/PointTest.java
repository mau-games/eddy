package finder.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

public class PointTest {

	@Test
	public void testGetX() {
		Point p = new Point(1, 2);
		
		assertEquals(1, p.getX());
	}

	@Test
	public void testSetX() {
		Point p = new Point(1, 2);
		p.setX(4);
		
		assertEquals(4, p.getX());
	}

	@Test
	public void testGetY() {
		Point p = new Point(1, 2);
		
		assertEquals(2, p.getY());
	}

	@Test
	public void testSetY() {
		Point p = new Point(1, 2);
		p.setY(4);
		
		assertEquals(4, p.getY());
	}

	@Test
	public void testEqualsObject() {
		Point p1 = new Point(1, 2);
		Point p2 = new Point(1, 2);
		
		assertEquals(p1, p2);
	}

}
