package finder.patterns.micro;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import game.Room;

public class RoomTest {

	@Test
	public void testEmptyMap() {
		Room room = Room.createRoomFromString("");
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(0, 0));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test1x1() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/1x1.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(0, 0));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test2x2() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/2x2.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(1, 1));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test3x3() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/3x3.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(2, 2));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(1, rooms.size());
	}

	@Test
	public void test3x3NoRoom() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/3x3noroom.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(2, 2));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test4x4() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/4x4.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(3, 3));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(1, rooms.size());
	}

	@Test
	public void test4x4NoRoom() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/4x4noroom.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(3, 3));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test10x10Corners() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10corners.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(9, 9));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(4, rooms.size());
	}

	@Test
	public void test10x10Features() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(9, 9));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(4, rooms.size());
	}

	@Test
	public void test10x10FeaturesUsingNullBoundary() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		List<Pattern> rooms = Chamber.matches(room, null);
		
		assertEquals(4, rooms.size());
	}

	@Test
	public void test10x10FeaturesUsingBoundariesFindingARoom() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(5, 4));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(1, rooms.size());
	}

	@Test
	public void test10x10FeaturesUsingBoundariesNotFindingARoom() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(4, 4), new Point(7, 7));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test10x10FeaturesUsingBoundariesTooBig() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/10x10features.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(10, 10));
		List<Pattern> rooms = Chamber.matches(room, boundary);
		
		assertEquals(0, rooms.size());
	}

	@Test
	public void test16x16Generated() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/16x16generated.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(15, 15));
		List<Pattern> rooms = Chamber.matches(room, boundary);

		assertEquals(2, rooms.size());
	}

	@Test
	public void testBigGenerated() throws IOException {
		Room room = Room.createRoomFromString(IOUtils.toString(
				this.getClass().getResourceAsStream("/maps/rooms/big-generated.map"),
				"UTF-8"));
		Rectangle boundary = new Rectangle(new Point(0, 0), new Point(15, 15));
		List<Pattern> rooms = Chamber.matches(room, boundary);

		assertEquals(2, rooms.size());
	}

}
