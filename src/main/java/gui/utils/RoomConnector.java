package gui.utils;

import game.Room;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestConnection;

public class RoomConnector extends ShapeBrush
{
	public Room from;
	public Room to;
	public Point fromPos;
	public Point toPos;
	public Boolean bidirectional;
	
	private Room aux;
	//maybe it can have info about painting? this is in another part now (RoomEdgeLine)
	
	public RoomConnector()
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
			
			System.out.println("Rlease ROOM ROOM: " + releasedRoom.hashCode());
			
			//Send event
			EventRouter.getInstance().postEvent(new RequestConnection(null, -1, from, to, fromPos, toPos));
			
		}
	}
	
	
	public void InitDrawing()
	{
		
	}
	
	public void EndDrawing()
	{
		
	}
}
