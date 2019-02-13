package export.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import game.Dungeon;
import game.Room;
import util.Point;

@XmlRootElement
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

	@XmlElement
	public RoomModel getInitialRoom() {
		return initialRoom;
	}

	public void setInitialRoom(RoomModel initialRoom) {
		this.initialRoom = initialRoom;
	}

	@XmlElement
	public Point getInitialPos() {
		return initialPos;
	}

	public void setInitialPos(Point initialPos) {
		this.initialPos = initialPos;
	}
	
	@XmlElement
	public ArrayList<RoomModel> getRooms() {
		return rooms;
	}

	public void setRooms(ArrayList<RoomModel> rooms) {
		this.rooms = rooms;
	}
	
	
}
