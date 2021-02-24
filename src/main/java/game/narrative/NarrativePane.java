package game.narrative;

import game.Dungeon;
import game.Room;
import game.RoomEdge;
import game.WorldViewCanvas;
import gui.controls.NarrativeShape;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

public class NarrativePane extends Pane
{
	private GrammarGraph owner;

	private Node source;
	double anchorX;
	double anchorY;

	//Arbitrary values
	private double internalMaxScale = 2.0;
	private double maxScale = 1.7;
	private double minScale = 0.6; //Less than this value and the scale starts to mess the translations
	private double currentScale = 1.0;

	public NarrativePane(GrammarGraph owner)
	{
		this.owner = owner;
		this.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());
		this.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseEventWorldPane());
//		setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		currentScale = getScaleX();
	}
	
	public void addVisualNarrativeNode(GrammarNode grammarNode)
	{
		NarrativeShape narShape = grammarNode.getNarrativeShape();
		
		if(!narShape.getRendered())
		{
			Platform.runLater(() -> {
//				narShape.getCanvas().draw(MapRenderer.getInstance().renderMap(room));
				narShape.setRendered(true);
			});
		}
		
		getChildren().add(narShape);
	}
	
	public void removeVisualRoom(Room room)
	{
		getChildren().remove(room.localConfig.getWorldCanvas().getCanvas());
	}
	
	public void addVisualConnector(RoomEdge roomEdge)
	{
		getChildren().add(roomEdge.graphicElement);
		roomEdge.graphicElement.toBack();
	}
	
	public void removeVisualConnector(RoomEdge roomEdge)
	{
		getChildren().remove(roomEdge.graphicElement);
	}
	
	public void renderAll()
	{
		//Simply clear all children! FIXME
		getChildren().clear();

		for(GrammarNode node : owner.nodes)
		{
			NarrativeShape narShape = node.getNarrativeShape();
			if(!narShape.getRendered())
			{
//				narShape.getCanvas().draw(MapRenderer.getInstance().renderMap(room));
				narShape.setRendered(true);
			}

			if(!getChildren().contains(narShape))
				getChildren().add(narShape);

			//Now get connections!
//			getChildren().addAll(node.getNarrativeShapeConnections());
			getChildren().addAll(node.recreateConnectionsBasedPosition());


		}
	}
	
	//EVENT
	public class MouseEventWorldPane implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnScroll(new EventHandler<ScrollEvent>()
			{

				@Override
				public void handle(ScrollEvent event) {
					// TODO Auto-generated method stub
					
        			Scale newScale = new Scale();
        	        newScale.setPivotX(event.getX());
        	        newScale.setPivotY(event.getY());
        	        newScale.setX( getScaleX() + (0.001 * event.getDeltaY()) );
        	        newScale.setY( getScaleY() + (0.001 * event.getDeltaY()) );
        	       
        	        internalScale(newScale);
        			event.consume();
				}

			});
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event)
	            {
	            	if(event.getTarget() == source && event.isMiddleButtonDown()) //TODO: WORK IN PROGRESS
	            	{
	            		setLayoutX(event.getX() + getLayoutX() - anchorX);
	            		setLayoutY(event.getY() + getLayoutY() - anchorY);
		    			event.consume();
	            	}
	            	
	            	testBounds();
	            }
	            
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            }

	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	anchorX = event.getX();
	    			anchorY = event.getY();
	            }
	        });
			
		}
	}
	
	private void internalScale(Scale value)
	{
		double auxScale = currentScale;
		auxScale += (value.getX() - 1.0);
		
		if(auxScale < minScale)
			return;
		
		currentScale = auxScale;
		getTransforms().add(value);
	}
	
	public void tryScale(Scale value)
	{
		double auxScale = currentScale;
		auxScale += (value.getX() - 1.0);
		
		if(auxScale > maxScale || auxScale < minScale)
			return;
		
		currentScale = auxScale;
		getTransforms().add(value);
		
	}
	
	public void resetScale()
	{
		getTransforms().clear();
		currentScale = 1.0;
	}
	
	/***
	 * This method is for testing all the children in the pane and base on their max X, max Y we will resize this pane
	 * TODO: under-development
	 */
	private void testBounds()
	{
		double maxX = 0;
		double maxY = 0;
//		double minX = 0;
//		double minY = 0;
		
		for(Node child : getChildren()) 
		{
			Bounds chBounds = child.getBoundsInParent();
			
			maxX = chBounds.getMaxX() > maxX ? chBounds.getMaxX() : maxX;
			maxY = chBounds.getMaxY() > maxY ? chBounds.getMaxY() : maxY;
//			minX = chBounds.getMinX() < minX ? chBounds.getMinX() : minX;
		}
		
		setPrefSize(maxX, maxY);
		setMaxSize(maxX, maxY);
//		setLayoutX(getLayoutX() + minX);
//		setPrefSize(maxX, maxY);
	}
}
