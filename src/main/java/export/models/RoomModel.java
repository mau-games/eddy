package export.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import game.Room;
import game.Tile;

@XmlRootElement
public class RoomModel {

	private double entranceSafety;
	private Tile[] tiles;
	
	
	public RoomModel() {};
	
	public RoomModel(Room room)
	{
		tiles = room.getTileBasedMap();
		entranceSafety = room.getEntranceSafety();
	}
	
	@XmlElement
	public Tile[] getTiles() {
		return tiles;
	}

	public void setTiles(Tile[] tile) {
		this.tiles = tile;
	}
	
	@XmlElement
	public double getEntranceSafety() {
		return entranceSafety;
	}

	public void setEntranceSafety(double entranceSafety) {
		this.entranceSafety = entranceSafety;
	}


}
