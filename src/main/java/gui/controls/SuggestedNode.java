package gui.controls;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.MapContainer;
import game.Room;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import util.eventrouting.EventRouter;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.RequestRoomView;

public class SuggestedNode
{
	private LabeledCanvas graphicNode;
	private MapContainer suggestedRoomContainer;
	private Room originalRoom;
	private Node source;
	private boolean ready = false;
	private String configFrom = "";
	
	public SuggestedNode(LabeledCanvas node)
	{
		this.graphicNode = node;
		this.graphicNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
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
	            	if(ready)
	    			{
	            		//We store the information about the selected suggestions
	    				ActionLogger.getInstance().storeAction(ActionType.CLICK,
																View.SUGGESTION, 
																TargetPane.SUGGESTION_PANE, 
																false,
																configFrom,
																suggestedRoomContainer.getMap().toString(),
																originalRoom.toString()
																);
	            		
	    				originalRoom.applySuggestion(suggestedRoomContainer.getMap());
	    				suggestedRoomContainer.setMap(originalRoom);
	    				EventRouter.getInstance().postEvent(new RequestRoomView(suggestedRoomContainer, 0, 0, null));
	    			}	
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
	
	public void resizeCanvasForRoom(Room original) //ReINIT
	{
		this.originalRoom = original;
		graphicNode.setPrefSize(490, 490);
		graphicNode.setMinSize(490, 490);
		graphicNode.setMaxSize(490, 490);
		
		float proportion = (float)(Math.min(original.getColCount(), original.getRowCount()))/(float)(Math.max(original.getColCount(), original.getRowCount()));
		
		if(original.getRowCount() > original.getColCount())
		{
			graphicNode.setPrefWidth(graphicNode.getPrefHeight() * proportion);
			graphicNode.setMinWidth(graphicNode.getPrefHeight() * proportion);
			graphicNode.setMaxWidth(graphicNode.getPrefHeight() * proportion);
		}
		else if(original.getColCount() > original.getRowCount())
		{
			graphicNode.setPrefHeight(graphicNode.getPrefWidth() * proportion);
			graphicNode.setMinHeight(graphicNode.getPrefWidth() * proportion);
			graphicNode.setMaxHeight(graphicNode.getPrefWidth() * proportion);
		}
	}
	
	public LabeledCanvas getGraphicNode()
	{
		return graphicNode;
	}

	public MapContainer getSuggestedRoom()
	{
		return this.suggestedRoomContainer;
	}
	
	public Room getOriginalRoom()
	{
		return this.originalRoom;
	}
	
	public void setSuggestedRoomContainer(MapContainer suggestedRoomContainer)
	{
		this.suggestedRoomContainer = suggestedRoomContainer;
	}
	
	public void setOriginalRoom(Room originalRoom)
	{
		this.originalRoom = originalRoom;
	}
	
	public void setReadiness(boolean value) {ready = value;}

	public void setFileName(String value) {configFrom = value;}
	

    /**
     * Highlights the control.
     * 
     * @param state True if highlighted, otherwise false.
     */
    private void highlight(boolean state)
    {
		if (state) {
    		graphicNode.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
    	} else {
    		graphicNode.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
    	}
    }
}