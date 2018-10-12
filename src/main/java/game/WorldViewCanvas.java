package game;


import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnector;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RegisterDoorPosition;
import util.eventrouting.events.RegisterRoom;

public class WorldViewCanvas 
{
	private LabeledCanvas worldGraphicNode;
	private Room owner;
	
	//We should add a few values to make our life easier
	private boolean rendered;
	private float viewSizeHeight;
	private float viewSizeWidth;
	private double dragAnchorX;
	private double dragAnchorY;
	public double tileSizeWidth; //TODO: public just to test
	public double tileSizeHeight;
	private Node source;
	//Border canvas
	private Canvas borderCanvas;
	
	public DoubleProperty xPosition; //TODO: public just to test
	public DoubleProperty yPosition;
	private Point currentBrushPosition = new Point();
	
	public WorldViewCanvas(Room owner)
	{
		this.owner = owner;
		worldGraphicNode = new LabeledCanvas();
		worldGraphicNode.setText("");
		
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventDrag());
		
		worldGraphicNode.setOnDragDetected(new EventHandler<MouseEvent>() 
		{
            @Override
            public void handle(MouseEvent event) 
            {
            	worldGraphicNode.startFullDrag();
            }
        });
		
		rendered = false;
		viewSizeHeight = 0;
		viewSizeWidth = 0;
		tileSizeHeight = 0;
		tileSizeWidth = 0;
		xPosition = new SimpleDoubleProperty();
		yPosition = new SimpleDoubleProperty();
		
		borderCanvas = new Canvas(viewSizeHeight, viewSizeHeight);
		worldGraphicNode.getChildren().add(borderCanvas);
		borderCanvas.setVisible(false);
		borderCanvas.setMouseTransparent(true);

	}
	
	public LabeledCanvas getCanvas() { return worldGraphicNode; }
	
	public void setRendered(boolean value)
	{
		rendered = value;

	}
	
	public boolean getRendered() { return rendered; }
	
	public void setViewSizeHeight(float value)
	{
		viewSizeWidth = value;
		applyViewSize();
	}
	
	public void setViewSizeWidth(float value)
	{
		viewSizeHeight = value;
		applyViewSize();
	}
	
	public void setViewSize(float viewWidth, float viewHeight)
	{
		viewSizeWidth = viewWidth;
		viewSizeHeight = viewHeight;
		applyViewSize();
	}
	
	private void applyViewSize()
	{
		//Change size of the room canvas
		worldGraphicNode.setMinSize(viewSizeWidth, viewSizeHeight);
		worldGraphicNode.setMaxSize(viewSizeWidth, viewSizeHeight); //THIS IS PART OF THE SOLUTION
		worldGraphicNode.setPrefSize(viewSizeWidth, viewSizeHeight);
		
		//Change the size of the border canvas (where you can place doors)
		borderCanvas.setWidth(viewSizeWidth);
		borderCanvas.setHeight(viewSizeHeight);
		
		//Update the size of the tiles in the graphic display
		tileSizeWidth = viewSizeWidth/ owner.getColCount();
		tileSizeHeight = viewSizeHeight/ owner.getRowCount();
	}
	
	public void setPosition(double next_x, double next_y)
	{
//		worldGraphicNode.setTranslateX(next_x);
//		worldGraphicNode.setTranslateX(next_y);
		
		xPosition.set((worldGraphicNode.getBoundsInParent().getMinX() + worldGraphicNode.getBoundsInParent().getWidth() / 2) - (worldGraphicNode.getWidth() / 2));
		yPosition.set((worldGraphicNode.getBoundsInParent().getMinY() + worldGraphicNode.getBoundsInParent().getHeight() / 2) - (worldGraphicNode.getHeight() / 2));
	}
	
	public class MouseEventDrag implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnMouseDragEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnector)
	            	{
		            	borderCanvas.setVisible(true);
		            	drawBorder();
	            	}
	            	else
	            	{
	            		highlight(true);
	            	}

	            	DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);
	            }
	        });

			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	
	            	if(DungeonDrawer.getInstance().getBrush() instanceof MoveElementBrush)
	            	{
		            	setPosition(event.getX() + source.getTranslateX() - dragAnchorX, event.getY() + source.getTranslateY() - dragAnchorY);
		            	System.out.println("ROOM X: " + (event.getX() + source.getTranslateX() - dragAnchorX));
		    			source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
		    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 
	            	}
	            	else
	            	{
	            		currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
		            	drawBorder();
	            	}
	            }

	        });
			
			source.setOnMouseDragOver(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	
	            	if(DungeonDrawer.getInstance().getBrush() instanceof MoveElementBrush)
	            	{
//		            	setPosition(event.getX() + source.getTranslateX() - dragAnchorX, event.getY() + source.getTranslateY() - dragAnchorY);
//		    			source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
//		    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 
//		    			System.out.println("ROOM X: " + (event.getX() + source.getTranslateX() - dragAnchorX));
	            	}
	            	else
	            	{
	            		currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
		            	drawBorder();
	            	}
	            }

	        });
			
			source.setOnMouseDragReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
	            	
	            	if(owner.isPointInBorder(currentBrushPosition))
	            	{
	            		DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
	            	}
	            }

	        });
			
			source.setOnMouseDragExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	borderCanvas.setVisible(false);
	            }

	        });
			
		}
	}
	
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			//1) Mouse enters the canvas of the room
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnector)
	            	{
		            	borderCanvas.setVisible(true);
		            	drawBorder();
	            	}
	            	else
	            	{
	            		highlight(true);
	            	}

	            	DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);	            	
	            }

	        });
			
			//2) mouse is moved around the map
			source.setOnMouseMoved(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));	            	
	            	drawBorder();
	            }
	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {

	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
	            	
	            	if(owner.isPointInBorder(currentBrushPosition))
	            	{
		            	DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
	            	}

	            }
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
	            }
	        });
			
			source.setOnMouseExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	borderCanvas.setVisible(false);
	            	highlight(false);
	            }

	        });
		}
	}
	
	private synchronized void drawBorder() 
    {
		if(borderCanvas.isVisible())
		{
			borderCanvas.getGraphicsContext2D().clearRect(0, 0, borderCanvas.getWidth(), borderCanvas.getHeight());
	    	MapRenderer.getInstance().drawRoomBorders(borderCanvas.getGraphicsContext2D(), 
	    			owner.matrix, 
	    			owner.borders, 
	    			new finder.geometry.Point(currentBrushPosition.getX(), currentBrushPosition.getY()) , 
	    			Color.WHITE);
		}
	}
	
    /**
     * Highlights the control.
     * 
     * @param state True if highlighted, otherwise false.
     */
    private void highlight(boolean state) {
    	if (state) {
    		worldGraphicNode.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9");
    	} else {
    		worldGraphicNode.setStyle("-fx-border-width: 0px");
    	}
    }
}
