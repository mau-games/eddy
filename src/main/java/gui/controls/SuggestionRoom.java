package gui.controls;

import game.Room;
import game.WorldViewCanvas.MouseEventH;
import gui.utils.DungeonDrawer;
import gui.utils.RoomConnector;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.FocusRoom;

/***
 * This class will hold the canvas and all relevant info about the maps that are suggested
 * to encapsulate the use of it and enhance it! :D 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class SuggestionRoom 
{
	private LabeledCanvas roomViewNode;
	private Room suggestedRoom;
	private Room originalRoom;
	private Node source;
	
	public SuggestionRoom()
	{
		roomViewNode = new LabeledCanvas();
		roomViewNode.setPrefSize(140, 140);
		roomViewNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
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
	            	System.out.println("Send event to apply suggestion");
	            	highlight(true);
//	            	roomView.getAppSuggestionsBtn().setDisable(false);
//
//					router.postEvent(new ApplySuggestion(0));
//					roomView.setSelectedMiniMap(roomView.suggestedRooms.get(0));
//
//					roomView.displayStats();
//					
//					roomView.getMap(0).setStyle("-fx-background-color:#fcdf3c;");
//					roomView.getMap(1).setStyle("-fx-background-color:#2c2f33;");
//					roomView.getMap(2).setStyle("-fx-background-color:#2c2f33;");
//					roomView.getMap(3).setStyle("-fx-background-color:#2c2f33;");
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
	public void resizeCanvasForRoom(Room original)
	{
		this.originalRoom = original;
		roomViewNode.setPrefSize(140, 140);
		
		float proportion = (float)(Math.min(original.getColCount(), original.getRowCount()))/(float)(Math.max(original.getColCount(), original.getRowCount()));
		
		if(original.getColCount() > 10)
		{
			roomViewNode.setPrefWidth(14.0 * original.getColCount());
		}
		
		if(original.getRowCount() > 10)
		{
			roomViewNode.setPrefHeight(14.0 * original.getRowCount());
		}
		
		System.out.println(roomViewNode.getHeight());
		System.out.println(roomViewNode.getPrefHeight());
		
		if(original.getRowCount() > original.getColCount())
		{
			roomViewNode.setPrefWidth(roomViewNode.getPrefHeight() * proportion);
		}
		else if(original.getColCount() > original.getRowCount())
		{
			roomViewNode.setPrefHeight(roomViewNode.getPrefWidth() * proportion);
		}

	}
	
	public LabeledCanvas getRoomCanvas()
	{
		return roomViewNode;
	}
	
	public String getStats() //This should be in room not here
	{
		return "THIS METHOD SHOULD NOT BE IN SUGGESTION ROOM";
	}
	

    /**
     * Highlights the control.
     * 
     * @param state True if highlighted, otherwise false.
     */
    private void highlight(boolean state) {
    	if (state) {
    		roomViewNode.setStyle("-fx-border-width: 2px; -fx-background-color:#fcdf3c;");
    	} else {
    		roomViewNode.setStyle("-fx-background-color:#2c2f33;");
    	}
    }
}
