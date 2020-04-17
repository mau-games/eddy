package gui.utils;

import game.Room;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestConnection;

public class RoomConnectorBrush extends InterRoomBrush
{
	public Room from;
	public Room to;
	public Point fromPos;
	public Point toPos;
	public Boolean bidirectional;
	
	private Room aux;
	//maybe it can have info about painting? this is in another part now (RoomEdgeLine)
	
	public RoomConnectorBrush()
	{
		
	}

	@Override
	public void onEnteredRoom(Room enteredRoom) 
	{
		aux = enteredRoom;
		
		System.out.println("EMTERING ROOM: " + enteredRoom.hashCode());
		
	}

	@Override
	public void onClickRoom(Room clickedRoom, Point clickPosition) 
	{
		// TODO Auto-generated method stub
		if(aux != null && aux == clickedRoom)
		{
			this.from = clickedRoom;
			this.fromPos = clickPosition;
			aux = null;
			
			System.out.println("CLICK ROOM ROOM: " + clickedRoom.hashCode());
		}
	}

	@Override
	public void onReleaseRoom(Room releasedRoom, Point releasedPosition)
	{
		// TODO Auto-generated method stub
		if(aux != null && aux == releasedRoom)
		{
			this.to = releasedRoom;
			this.toPos = releasedPosition;
			aux = null;
			
			System.out.println("Release ROOM ROOM: " + releasedRoom.hashCode());
			
			//Send event
			if(releasedRoom.equals(from))
			{
				from = null;
				to = null;
				toPos = null;
				fromPos = null;
			}
			else if(from != null && to != null && fromPos != null && toPos != null)
			{
				EventRouter.getInstance().postEvent(new RequestConnection(null, -1, from, to, fromPos, toPos));	
				from = null;
				to = null;
				toPos = null;
				fromPos = null;
			}
			
			
		}
	}


	public void InitDrawing()
	{
		
	}
	
	public void EndDrawing()
	{
		
	}
}
