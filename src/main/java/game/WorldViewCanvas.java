package game;


import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
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
	
	public DoubleProperty xPosition;
	public DoubleProperty yPosition;
	
	//Door pos test
	private Point doorpos;
	
	public WorldViewCanvas(Room owner)
	{
		this.owner = owner;
		worldGraphicNode = new LabeledCanvas();
		worldGraphicNode.setText("");
		
		
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventDrag());
//		worldGraphicNode.addEventFilter(MouseEvent.DRAG_DETECTED, new MouseEventDrag());
//		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_MOVED, new MouseEventH()); //need to check
//		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_PRESSED, new MouseEventH()); //need to check
		
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
		
		doorpos = new Point(0,0);

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
	            	borderCanvas.setVisible(true);
	            	drawBorder();
	            	DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);
	            }
	            
	            private synchronized void drawBorder() 
	            {
	            	borderCanvas.getGraphicsContext2D().clearRect(0, 0, borderCanvas.getWidth(), borderCanvas.getHeight());
	            	MapRenderer.getInstance().drawRoomBorders(borderCanvas.getGraphicsContext2D(), owner.matrix, owner.borders, Color.WHITE);
	        	}
	        });

			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	
	            	if(DungeonDrawer.getInstance().getBrush() instanceof MoveElementBrush)
	            	{
		            	setPosition(event.getX() + source.getTranslateX() - dragAnchorX, event.getY() + source.getTranslateY() - dragAnchorY);
		    			source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
		    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 
	            	}
	            }

	        });
			
			source.setOnMouseDragReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	Point p =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
	            	doorpos = new Point((int)event.getX(), (int)event.getY());
	            	
	            	DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, p);
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

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {

	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
//	            	
//	            	System.out.println("Position in X: " + event.getX());
//	            	System.out.println("Position in Y: " + event.getY());
	            	Point p =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
//	            	System.out.println("POSITION IN MATRIX: (" + p.getX() + "," + p.getY() + ")");
	            	doorpos = new Point((int)event.getX(), (int)event.getY());
	            	
	            	DungeonDrawer.getInstance().getBrush().onClickRoom(owner, p);
//	            	source.startFullDrag();
//	            	
//	            	EventRouter.getInstance().postEvent(new RegisterDoorPosition(p, owner));
	            }
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
//	            	System.out.println("HOW MANY MEEE!?");
	            	Point p =  new Point((int)( event.getX() / tileSizeWidth), (int)( event.getY() / tileSizeHeight ));
//	            	System.out.println("POSITION IN MATRIX: (" + p.getX() + "," + p.getY() + ")");
	            	doorpos = new Point((int)event.getX(), (int)event.getY());

//	            	DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, p);
	            	
//	            	EventRouter.getInstance().postEvent(new RegisterDoorPosition(p, owner));
	            }
	        });
			
			source.setOnMouseExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	
	            	borderCanvas.setVisible(false);
	            }

	        });
			
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	
	            	borderCanvas.setVisible(true);
	            	drawBorder();
//	            	EventRouter.getInstance().postEvent(new RegisterRoom(owner));
//	            	System.out.println("MOUSE ENTERED: " + owner.hashCode());
	            	DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);
	            }
	            
	            private synchronized void drawBorder() 
	            {
	            	borderCanvas.getGraphicsContext2D().clearRect(0, 0, borderCanvas.getWidth(), borderCanvas.getHeight());
	            	MapRenderer.getInstance().drawRoomBorders(borderCanvas.getGraphicsContext2D(), owner.matrix, owner.borders, Color.WHITE);
	            
	        	}

	        });
			
		}
	}
}
