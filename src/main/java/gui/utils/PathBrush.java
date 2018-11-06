package gui.utils;

import game.Room;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestPathFinding;

public class PathBrush extends InterRoomBrush
{
	public Room from;
	public Room to;
	public Point fromPos;
	public Point toPos;
	
	private Room aux;
	//maybe it can have info about painting? this is in another part now (RoomEdgeLine)
	
	public PathBrush()
	{
		
	}

	@Override
	public void onEnteredRoom(Room enteredRoom) 
	{
		aux = enteredRoom;
		
	}

	@Override
	public void onClickRoom(Room clickedRoom, Point clickPosition) 
	{
		this.from = clickedRoom;
		this.fromPos = clickPosition;
	}

	@Override
	public void onReleaseRoom(Room releasedRoom, Point releasedPosition)
	{
		this.to = releasedRoom;
		this.toPos = releasedPosition;

		if(from != null && to != null && fromPos != null && toPos != null)
		{
			EventRouter.getInstance().postEvent(new RequestPathFinding(null, -1, from, to, fromPos, toPos));	
			from = null;
			to = null;
			toPos = null;
			fromPos = null;
		}
	}
}
