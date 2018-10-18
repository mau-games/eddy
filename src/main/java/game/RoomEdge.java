package game;

import javafx.beans.binding.Bindings;
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
	
	//TODO: TileSize needs to be a property too??
	//TODO: Is giving the init of the tile, check how to get the center .. I FIXED but there must be a better way haha
	public RoomEdge(Room from, Room to, Point fromPosition, Point toPosition)
	{
//		fX.bind(Bindings.add((fromPosition.getX() * from.localConfig.getWorldCanvas().tileSizeWidth) +
//							from.localConfig.getWorldCanvas().tileSizeWidth/2, 
//							from.localConfig.getWorldCanvas().xPosition));
//
//		fY.bind(Bindings.add(fromPosition.getY() * from.localConfig.getWorldCanvas().tileSizeHeight + 
//							from.localConfig.getWorldCanvas().tileSizeHeight/2, 
//							from.localConfig.getWorldCanvas().yPosition));
//		
//		tX.bind(Bindings.add(toPosition.getX() * to.localConfig.getWorldCanvas().tileSizeWidth + 
//							to.localConfig.getWorldCanvas().tileSizeWidth/2, 
//							to.localConfig.getWorldCanvas().xPosition));
//		
//		tY.bind(Bindings.add(toPosition.getY() * to.localConfig.getWorldCanvas().tileSizeHeight + 
//							to.localConfig.getWorldCanvas().tileSizeHeight/2, 
//							to.localConfig.getWorldCanvas().yPosition));
		
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
		
//		fX.add(from.localConfig.getWorldCanvas().xPosition);
//		fY.add(from.localConfig.getWorldCanvas().yPosition).add(fromPosition.getY() * from.localConfig.getWorldCanvas().tileSizeHeight);
//		tX.add(to.localConfig.getWorldCanvas().xPosition).add(toPosition.getX() * to.localConfig.getWorldCanvas().tileSizeWidth);
//		tY.add(to.localConfig.getWorldCanvas().yPosition).add(toPosition.getY() * to.localConfig.getWorldCanvas().tileSizeHeight);
		
		System.out.println("LineF pos: (" + fX.getValue() + "," + fY.getValue() + ")" );
//		graphicElement = new RoomEdgeLine(from.localConfig.getWorldCanvas().xPosition, 
//											from.localConfig.getWorldCanvas().yPosition, 
//											to.localConfig.getWorldCanvas().xPosition, 
//											to.localConfig.getWorldCanvas().yPosition);
//		
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
