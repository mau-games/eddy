package util.eventrouting.events;

import javafx.scene.image.Image;
import util.eventrouting.PCGEvent;

public class ChangeCursor extends PCGEvent 
{
	Image cursorImage = null;
	
	public ChangeCursor(String cursorPath)
	{
		if(cursorPath == null || cursorPath == "" )
		{
			cursorImage = null;
		}
		else
		{
			cursorImage= new Image(cursorPath);  //pass in the image path
		}
	}
	
	public Image getCursorImage()
	{
		return cursorImage;
	}
}
