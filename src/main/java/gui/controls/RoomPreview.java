package gui.controls;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.Room;
import game.WorldViewCanvas.MouseEventH;
import gui.utils.DungeonDrawer;
import gui.utils.RoomConnectorBrush;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.FocusRoom;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.intraview.DungeonPreviewSelected;

/***
 * This class will hold the canvas and all relevant info about the maps that are suggested
 * to encapsulate the use of it and enhance it! :D 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class RoomPreview 
{
	private LabeledCanvas roomViewNode;
	private Room previewOwner;
	private Node source;
	private boolean selected = false;
	
	//Super workaround which I have to do unless someone knows how can i do this thing
	private RoomPreview self;
	
	public RoomPreview(Room previewOwner)
	{
		roomViewNode = new LabeledCanvas();
		roomViewNode.setPrefSize(140, 140);
		roomViewNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
		this.previewOwner = previewOwner;
		resizeCanvas();
		
		self = this;
		selected = false;
	}
	
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			//1) Mouse enters the canvas of the room --> I can fire the event here, no?
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	highlight(true);
	            	System.out.println("MOUSE ENTERED CANVAS");
	            }

	        });
			
			//2) mouse is moved around the map
			source.setOnMouseMoved(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            }
	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {	            	
	            	EventRouter.getInstance().postEvent(new DungeonPreviewSelected(self.previewOwner));
	            	selected = true;
	            	highlight(true);
	            	
	            }
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            }
	        });
			
			source.setOnMouseExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	highlight(false);
	            }

	        });
		}
	}
	
	//TODO: Change magic numbers
	//Force me! 
	public void resizeCanvas() //ReINIT
	{
		selected = false;
		roomViewNode.setPrefSize(180,180);
		
		float proportion = (float)(Math.min(previewOwner.getColCount(), previewOwner.getRowCount()))/(float)(Math.max(previewOwner.getColCount(), previewOwner.getRowCount()));
		
		if(previewOwner.getColCount() > 10)
		{
			roomViewNode.setPrefWidth(18.0 * previewOwner.getColCount());
		}
		
		if(previewOwner.getRowCount() > 10)
		{
			roomViewNode.setPrefHeight(18.0 * previewOwner.getRowCount());
		}
		
		if(previewOwner.getRowCount() > previewOwner.getColCount())
		{
			roomViewNode.setPrefWidth(roomViewNode.getPrefHeight() * proportion);
		}
		else if(previewOwner.getColCount() > previewOwner.getRowCount())
		{
			roomViewNode.setPrefHeight(roomViewNode.getPrefWidth() * proportion);
		}
		
		roomViewNode.setMaxHeight(roomViewNode.getPrefHeight());
		roomViewNode.setMinHeight(roomViewNode.getPrefHeight());
		roomViewNode.setMaxWidth(roomViewNode.getPrefWidth());
		roomViewNode.setMaxWidth(roomViewNode.getPrefWidth());
	}
	
	public LabeledCanvas getRoomCanvas()
	{
		return roomViewNode;
	}
	
	public String getStats() //This should be in room not here
	{
		return "THIS METHOD SHOULD NOT BE IN SUGGESTION ROOM";
	}
	
	public Room getPreviewRoom()
	{
		return this.previewOwner;
	}
	
	public void setSelected(Boolean value)
	{
		selected = value;
		highlight(value);
	}

    /**
     * Highlights the control.
     * 
     * @param state True if highlighted, otherwise false.
     */
    private void highlight(boolean state)
    {
    	if(selected)
    	{
    		roomViewNode.setStyle("-fx-border-width: 2px; -fx-border-color: #fcdf3c;");
    	}
    	else
    	{
    		if (state) {
        		roomViewNode.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
        	} else {
        		roomViewNode.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
        	}
    	}
    }
}
