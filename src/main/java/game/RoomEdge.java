package game;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import util.Point;

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
		
		fX.bind(Bindings.add(Bindings.add(
				Bindings.multiply(fromPosition.getX(), from.localConfig.getWorldCanvas().tileSizeWidth), 
				Bindings.divide(from.localConfig.getWorldCanvas().tileSizeWidth, 2)), 
				from.localConfig.getWorldCanvas().xPosition));
		
		fY.bind(Bindings.add(Bindings.add(
				Bindings.multiply(fromPosition.getY(), from.localConfig.getWorldCanvas().tileSizeHeight), 
				Bindings.divide(from.localConfig.getWorldCanvas().tileSizeHeight, 2)), 
				from.localConfig.getWorldCanvas().yPosition));
		
		tX.bind(Bindings.add(Bindings.add(
				Bindings.multiply(toPosition.getX(), to.localConfig.getWorldCanvas().tileSizeWidth), 
				Bindings.divide(to.localConfig.getWorldCanvas().tileSizeWidth, 2)), 
				to.localConfig.getWorldCanvas().xPosition));
		
		tY.bind(Bindings.add(Bindings.add(
				Bindings.multiply(toPosition.getY(), to.localConfig.getWorldCanvas().tileSizeHeight), 
				Bindings.divide(to.localConfig.getWorldCanvas().tileSizeHeight, 2)), 
				to.localConfig.getWorldCanvas().yPosition));
		
		System.out.println("LineF pos: (" + fX.getValue() + "," + fY.getValue() + ")" );

		graphicElement = new RoomEdgeLine(fX, fY, tX, tY);
		
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
