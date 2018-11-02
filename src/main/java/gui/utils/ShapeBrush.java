package gui.utils;

import game.Room;
import util.Point;

public abstract class ShapeBrush 
{
	public ShapeBrush()
	{
		
	}
	
	public abstract void onEnteredRoom(Room enteredRoom);
	public abstract void onClickRoom(Room clickedRoom, Point clickPosition);
	public abstract void onReleaseRoom(Room releasedRoom, Point releasedPosition);
}
