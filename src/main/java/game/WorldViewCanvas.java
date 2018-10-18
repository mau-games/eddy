package game;


import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnector;
import javafx.beans.binding.Bindings;
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
	public float viewSizeHeight;
	public float viewSizeWidth;
	private double dragAnchorX;
	private double dragAnchorY;
//	public double tileSizeWidth; //TODO: public just to test
//	public double tileSizeHeight;
	private Node source;
	//Border canvas
	private Canvas borderCanvas;
	
	public DoubleProperty xPosition; //TODO: public just to test
	public DoubleProperty yPosition;
	
	public DoubleProperty tileSizeWidth; //TODO: public just to test
	public DoubleProperty tileSizeHeight;

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
		tileSizeHeight = new SimpleDoubleProperty();
		tileSizeWidth = new SimpleDoubleProperty();
		xPosition = new SimpleDoubleProperty();
		yPosition = new SimpleDoubleProperty();
		
		//Crazy binding but it gives exactly the position we need and now it is updated  automatically
		xPosition.bind(Bindings.selectDouble(worldGraphicNode.boundsInParentProperty(), "minX").
				add(Bindings.divide( Bindings.selectDouble(worldGraphicNode.boundsInParentProperty(), "width"), 2).subtract(Bindings.divide(worldGraphicNode.widthProperty(), 2.0))));
		yPosition.bind(Bindings.selectDouble(worldGraphicNode.boundsInParentProperty(), "minY").
				add(Bindings.divide( Bindings.selectDouble(worldGraphicNode.boundsInParentProperty(), "height"), 2).subtract(Bindings.divide(worldGraphicNode.heightProperty(), 2.0))));

		
		borderCanvas = new Canvas(viewSizeHeight, viewSizeHeight);
		worldGraphicNode.getChildren().add(borderCanvas);
		borderCanvas.setVisible(false);
		borderCanvas.setMouseTransparent(true);

	}
	
	public void setParent()
	{
		
		System.out.println("XPOSITION: " + xPosition.get() +", YPOSITION: " + yPosition.get());
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
		tileSizeWidth.set(viewSizeWidth/ owner.getColCount());
		tileSizeHeight.set(viewSizeHeight/ owner.getRowCount());
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
		    			source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
		    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 
	            	}
	            	else
	            	{
	            		currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
		            	drawBorder();
	            	}
	            }

	        });
			
			source.setOnMouseDragOver(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnector)
	            	{
	            		currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
		            	drawBorder();
	            	}

	            }

	        });
			
			source.setOnMouseDragReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
	            	
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
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));            	
	            	drawBorder();
	            }
	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {

	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
	            	
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
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
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
	    			Point.castToGeometry(currentBrushPosition) , 
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
