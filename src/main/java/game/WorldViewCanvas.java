package game;

import gui.controls.LabeledCanvas;
import gui.views.WorldViewController.MouseEventH;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import util.Point;

public class WorldViewCanvas 
{
	private LabeledCanvas worldGraphicNode;
	
	//We should add a few values to make our life easier
	private boolean rendered;
	private float viewSizeHeight;
	private float viewSizeWidth;
	private Point position;
	private Node source;
	private double dragAnchorX;
	private double dragAnchorY;
	
	public WorldViewCanvas()
	{
		worldGraphicNode = new LabeledCanvas();
		worldGraphicNode.setText("");
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_PRESSED, new MouseEventH());
		rendered = false;
		viewSizeHeight = 0;
		viewSizeWidth = 0;
		position = new Point();
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
	}
	
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {

	            	
//	            	System.out.println(source.getLayoutBounds());
//	            	System.out.println("parent: " + source.getParent().getLayoutBounds());
//	            	System.out.println("EVENT X: " +  (event.getX()));
//	            	System.out.println("getTranslateX: " +  (source.getTranslateX()));
//	            	System.out.println("SCENE X: " + event.getSceneX());

	            	
//	            	if(outSideParentBounds(source.getLayoutBounds(),source.getTranslateX() + 10, source.getTranslateY() + 10))
//	            	{
//	            		event.consume();
//	            		return;
//	            	}
//	            		
	            	setPosition(event.getX() + source.getTranslateX() - dragAnchorX, event.getY() + source.getTranslateY() - dragAnchorY);
	    			source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
	    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 

	            }

	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {

	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
	    		
	            }
	        });
			
		}
	}
}
