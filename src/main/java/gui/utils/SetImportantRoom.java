package gui.utils;

import game.Room;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.ImportantRoomEvent;
import util.eventrouting.events.RequestConnection;

public class SetImportantRoom extends InterRoomBrush
{
	public Room importantRoom;
	public Point importantRoomPos;
	
	private Room aux;
	private ImportantRoomEvent event;
	
	public SetImportantRoom(ImportantRoomEvent event)
	{
		this.event = event;
	}

	@Override
	public void onEnteredRoom(Room enteredRoom) 
	{
		aux = enteredRoom;	
	}

	@Override
	public void onClickRoom(Room clickedRoom, Point clickPosition) 
	{
		// TODO Auto-generated method stub
		if(aux != null && aux == clickedRoom)
		{
			this.importantRoom = clickedRoom;
			this.importantRoomPos = clickPosition;
			aux = null;
			
			System.out.println("IMPORTANT ROOM; ROOM: " + clickedRoom.hashCode());
			
			event.setPickedRoom(importantRoom);
			event.setRoomPos(importantRoomPos);
			
			EventRouter.getInstance().postEvent(event);
		}
	}

	@Override
	public void onReleaseRoom(Room releasedRoom, Point releasedPosition)
	{
		
	}
	
	
	public void InitDrawing()
	{
		
	}
	
	public void EndDrawing()
	{
		
	}
}
