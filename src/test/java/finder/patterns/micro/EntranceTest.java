package finder.patterns.micro;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import game.Room;
import org.junit.Test;

public class EntranceTest {

	@Test
	public void testEmptyMap() {
		Room room = Room.createRoomFromString("");
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(0, 0));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(0, entrances.size());
	}

	@Test
	public void test1x1() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/1x1.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(0, 0));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void test2x2() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/2x2.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(1, 1));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void test3x3() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/3x3.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(2, 2));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void test3x3NoRoom() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/3x3noroom.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(2, 2));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void test4x4() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/4x4.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(3, 3));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void test4x4NoRoom() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/4x4noroom.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(3, 3));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void test10x10Corners() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10corners.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(9, 9));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}

	@Test
	public void testBigGenerated() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/big-generated.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(15, 15));
		List<Pattern> entrances = Entrance.matches(room, boundary);

		assertEquals(1, entrances.size());
	}
}
