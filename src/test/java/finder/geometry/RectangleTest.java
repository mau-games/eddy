package finder.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

public class RectangleTest {

	@Test
	public void testGetArea() {
		Rectangle rectangle = new Rectangle(new Point(0, 0), new Point(2, 3));
		
		assertEquals(6, rectangle.getArea(), 0);
	}

	@Test
	public void testGetTopLeft() {
		Rectangle rectangle = new Rectangle(new Point(1, 1), new Point(2, 3));
		
		assertEquals(new Point(1, 1), rectangle.getTopLeft());
	}

	@Test
	public void testGetBottomRight() {
		Rectangle rectangle = new Rectangle(new Point(1, 1), new Point(2, 3));
		
		assertEquals(new Point(2, 3), rectangle.getBottomRight());
	}

	@Test
	public void testEqualsObject() {
		Rectangle r1 = new Rectangle(new Point(1, 1), new Point(2, 3));
		Rectangle r2 = new Rectangle(new Point(1, 1), new Point(2, 3));
		
		assertEquals(r1, r2);
	}

}
