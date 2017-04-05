package finder.patterns.micro;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import game.Map;
import org.junit.Test;

public class EnemyTest {

	@Test
	public void testEmptyMap() {
		Map map = Map.fromString("");
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(0, 0));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test1x1() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/1x1.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(0, 0));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test2x2() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/2x2.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(1, 1));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test3x3() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/3x3.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(2, 2));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test3x3NoRoom() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/3x3noroom.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(2, 2));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test4x4() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/4x4.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(3, 3));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test4x4NoRoom() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/4x4noroom.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(3, 3));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test10x10Corners() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10corners.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(9, 9));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(2, enemies.size());
	}

	@Test
	public void test10x10Features() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(9, 9));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(1, enemies.size());
	}

	@Test
	public void test10x10FeaturesUsingNullBoundary() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		List<Pattern> enemies = Enemy.matches(map, null);

		assertEquals(1, enemies.size());
	}

	@Test
	public void test10x10FeaturesUsingBoundariesFindingAnEnemy() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(1, 1), new Point(8, 8));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(1, enemies.size());
	}

	@Test
	public void test10x10FeaturesUsingBoundariesNotFindingAnEnemy() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(2, 2), new Point(4, 5));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test10x10FeaturesUsingBoundariesTooBig() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(10, 10));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(0, enemies.size());
	}

	@Test
	public void test16x16Generated() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/16x16generated.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(15, 15));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(8, enemies.size());
	}

	@Test
	public void testBigGenerated() throws IOException {
		Map map = Map.fromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/big-generated.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(15, 15));
		List<Pattern> enemies = Enemy.matches(map, boundary);

		assertEquals(34, enemies.size());
	}
}
