package export.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

import game.Room;
import util.Point;

public class DungeonModel {

	@XmlElement
	ArrayList<Room> rooms;
	@XmlElement
	Room initialRoom;
	@XmlElement
	Point initialPos;
	
	public DungeonModel(){}
	
	public DungeonModel(ArrayList<Room> rooms, Room initialRoom, Point initialPos)
	{
		this.rooms = rooms;
		this.initialRoom = initialRoom;
		this.initialPos = initialPos;
	}

	public Room getInitialRoom() {
		return initialRoom;
	}

	public void setInitialRoom(Room initialRoom) {
		this.initialRoom = initialRoom;
	}

	public Point getInitialPos() {
		return initialPos;
	}

	public void setInitialPos(Point initialPos) {
		this.initialPos = initialPos;
	}
	
	public ArrayList<Room> getRooms() {
		return rooms;
	}

	public void setRooms(ArrayList<Room> rooms) {
		this.rooms = rooms;
	}
	
	
}
