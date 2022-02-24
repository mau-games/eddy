package gui.utils;

import game.Room;
import javafx.scene.Cursor;
import util.Point;

public class MoveElementBrush extends ShapeBrush
{
	Cursor cursorType;
	
	public MoveElementBrush()
	{
		cursorType = Cursor.MOVE;
	}

	@Override
	public void onEnteredRoom(Room enteredRoom) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickRoom(Room clickedRoom, Point clickPosition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReleaseRoom(Room releasedRoom, Point releasedPosition) {
		// TODO Auto-generated method stub
		
	}


}
