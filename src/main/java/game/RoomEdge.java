package game;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import util.Point;

//TODO: This connects 2 centers but I actually want the position of the door
//TODO: I need to use the doors
public class RoomEdge 
{
	public RoomEdgeLine graphicElement;
	
	//This can be a struct
	public Room from;
	public Room to;
	public Point fromPosition; //this pos actually is the one of the door
	public Point toPosition; //this pos actually is the one of the door
	
	DoubleProperty fX = new SimpleDoubleProperty();
	DoubleProperty fY = new SimpleDoubleProperty();
	DoubleProperty tX = new SimpleDoubleProperty();
	DoubleProperty tY = new SimpleDoubleProperty();
	
	public boolean rendered = false;
	
	public RoomEdge(Room from, Room to, Point fromPosition, Point toPosition)
	{
		fX.add(from.localConfig.getWorldCanvas().xPosition).add(fromPosition.getX()).multiply(from.localConfig.getWorldCanvas().tileSizeWidth);
		fY.add(from.localConfig.getWorldCanvas().yPosition).add(fromPosition.getY()).multiply(from.localConfig.getWorldCanvas().tileSizeHeight);
		tX.add(to.localConfig.getWorldCanvas().xPosition).add(toPosition.getX()).multiply(to.localConfig.getWorldCanvas().tileSizeWidth);
		tY.add(to.localConfig.getWorldCanvas().yPosition).add(toPosition.getY()).multiply(to.localConfig.getWorldCanvas().tileSizeHeight);
		
		graphicElement = new RoomEdgeLine(from.localConfig.getWorldCanvas().xPosition, 
				from.localConfig.getWorldCanvas().yPosition, 
				to.localConfig.getWorldCanvas().xPosition, 
				to.localConfig.getWorldCanvas().yPosition);
		
//		graphicElement = new RoomEdgeLine(fX, fY, tX, tY);
		
		this.from = from;
		this.to = to;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
	}
	
	public String print()
	{
		return "Room edge between Room: " + from.hashCode() + " at (" + fromPosition.getX() + "," + fromPosition.getY() + ") and Room: " + to.hashCode() + " at (" + toPosition.getX() + "," + toPosition.getY() + ")";
	}
}
