package export.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import game.Dungeon;
import game.Room;
import util.Point;

@XmlRootElement(name="DungeonModel")
public class DungeonModel {

	ArrayList<RoomModel> rooms;
	RoomModel initialRoom;
	Point initialPos;
	
	public DungeonModel(){}
	
	public DungeonModel(Dungeon dungeon)
	{
		rooms = dungeon.getRoomModels();
		this.initialRoom = dungeon.getInitialRoom().getModel();
		this.initialPos = dungeon.getInitialPosition();
	}

	@XmlElement(name="InitialRoom")
	public RoomModel getInitialRoom() {
		return initialRoom;
	}

	public void setInitialRoom(RoomModel initialRoom) {
		this.initialRoom = initialRoom;
	}

	@XmlElement(name="InitialPos")
	public Point getInitialPos() {
		return initialPos;
	}

	public void setInitialPos(Point initialPos) {
		this.initialPos = initialPos;
	}
	
	@XmlElement(name="Rooms")
	public ArrayList<RoomModel> getRooms() {
		return rooms;
	}

	public void setRooms(ArrayList<RoomModel> rooms) {
		this.rooms = rooms;
	}
	
	
}
