package util.eventrouting;

import game.Room;
import util.Point;

public class ImportantRoomEvent extends PCGEvent 
{
	protected Room pickedRoom;
	protected Point pos;
	
	public ImportantRoomEvent(Room pickedRoom, Point pos)
	{
		this.pickedRoom = pickedRoom;
		this.pos = pos;
	}
	
	public Room getPickedRoom()
	{
		return pickedRoom;
	}
	
	public Point getRoomPos()
	{
		return pos;
	}
	
	public void setPickedRoom(Room room)
	{
		this.pickedRoom = room;
	}
	
	public void setRoomPos(Point pos)
	{
		this.pos = pos;
	}
	

}
