package util.eventrouting.events;

import game.Room;
import game.WorldViewCanvas;
import util.eventrouting.PCGEvent;

public class FocusRoom extends PCGEvent
{
	/***
	 * The dungeon which you want to create the room in. 
	 * For later when we have several dungeons
	 */
	private Room focusedRoom;
	private WorldViewCanvas focusedRoomCanvas;
	
	public FocusRoom(Room payload, WorldViewCanvas roomCanvas)
	{
		this.focusedRoom = payload;
		this.focusedRoomCanvas = roomCanvas;
		
		setPayload(payload);
	}
	
	public Room getRoom()
	{
		return focusedRoom;
	}
	
	public WorldViewCanvas getRoomCanvas()
	{
		return focusedRoomCanvas;
	}
}
