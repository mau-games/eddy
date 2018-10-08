package game;


import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import gui.views.WorldViewController.MouseEventH;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
	private Point position;
	private Node source;
	private double dragAnchorX;
	private double dragAnchorY;
	
	//Border canvas
	private Canvas borderCanvas;
	
	public WorldViewCanvas(Room owner)
	{
		this.owner = owner;
		worldGraphicNode = new LabeledCanvas();
		worldGraphicNode.setText("");
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_MOVED, new MouseEventH());
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_PRESSED, new MouseEventH());
		rendered = false;
		viewSizeHeight = 0;
		viewSizeWidth = 0;
		position = new Point();
		
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
	
	public void setPosition(double next_x, double next_y)
	{
//		worldGraphicNode.setTranslateX(next_x);
//		worldGraphicNode.setTranslateX(next_y);
		position.setX((int)worldGraphicNode.getLayoutX());
		position.setY((int)worldGraphicNode.getLayoutY());
	}
	
	private void applyViewSize()
	{
		worldGraphicNode.setMinSize(viewSizeWidth, viewSizeHeight);
		worldGraphicNode.setMaxSize(viewSizeWidth, viewSizeHeight); //THIS IS PART OF THE SOLUTION
		worldGraphicNode.setPrefSize(viewSizeWidth, viewSizeHeight);
		borderCanvas.setWidth(viewSizeWidth);
		borderCanvas.setHeight(viewSizeHeight);
	}
	
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	//FAST TEST
	            	if(event.isControlDown())
	            		return;
	            	
	            	setPosition(event.getX() + source.getTranslateX() - dragAnchorX, event.getY() + source.getTranslateY() - dragAnchorY);
	    			source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
	    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 

	            }

	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {

	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
//	            	
//	            	System.out.println("Position in X: " + event.getX());
//	            	System.out.println("Position in Y: " + event.getY());
	            	Point p =  new Point((int)( event.getX() / (viewSizeWidth/ owner.getColCount())), (int)( event.getY() / (viewSizeHeight/ owner.getRowCount())));
	            	System.out.println("POSITION IN MATRIX: (" + p.getX() + "," + p.getY() + ")");

//	            	
	            	EventRouter.getInstance().postEvent(new RegisterDoorPosition(p, owner));
	            }
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	Point p =  new Point((int)( event.getX() / (viewSizeWidth/ owner.getColCount())), (int)( event.getY() / (viewSizeHeight/ owner.getRowCount())));
	            	System.out.println("POSITION IN MATRIX: (" + p.getX() + "," + p.getY() + ")");

	            	EventRouter.getInstance().postEvent(new RegisterDoorPosition(p, owner));
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
	            	EventRouter.getInstance().postEvent(new RegisterRoom(owner));
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
