package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.Dungeon;
import game.Room;
import game.RoomEdge;
import game.WorldViewCanvas;
import gui.controls.NarrativeShape;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import util.Point;
import util.eventrouting.events.RequestNewGrammarStructureNode;

import java.util.*;

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

	public NarrativePane()
	{
	}

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

	//Iterate through connections to and from current node to place it at the right position.
	private int iterateConnections(GrammarNode current_node, ArrayList<Point> positions, ArrayList<GrammarNode> connections, int counter,
								   ArrayList<GrammarNode> visitedNodes, Stack<GrammarNode> toCheckNodes, ArrayList<Point> grid, Point current,
								   int current_x, int current_y, double margin)
	{

		for(GrammarNode testedNode: connections)
		{
			if(visitedNodes.contains(testedNode))
				continue;

			current = current_node.getNarrativeShape().grid_placement;
			current_x = current.getX();
			current_y = current.getY();

			NarrativeShape narShape = current_node.getNarrativeShape();
			Bounds connectionBounds = narShape.localToScreen(narShape.getBoundsInLocal());
//			Bounds connectionBounds = narShape.getBoundsInLocal();

			narShape = testedNode.getNarrativeShape();

			Bounds localBoundsInParent = narShape.getBoundsInParent();
			Point2D local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX(), connectionBounds.getMaxY());

			//Iterate to find a non-occupied position in the neighborhood of the currentNode
			//fixme: this will definitely have problems if there are not available positions
			while(grid.contains(current))
			{
				counter++;
				counter = counter % 8;


				if(counter == 0)
				{
					current = new Point(current_x + 1, current_y);
				}
				else if(counter == 1)
				{
					current = new Point(current_x, current_y + 1);
				}
				else if(counter == 2)
				{
					current = new Point(current_x - 1, current_y);
				}
				else if(counter == 3)
				{
					current = new Point(current_x, current_y - 1);
				}
				else if(counter == 4)
				{
					current = new Point(current_x + 1, current_y -1);
				}
				else if(counter == 5)
				{
					current = new Point(current_x + 1, current_y +1);
				}
				else if(counter == 6)
				{
					current = new Point(current_x - 1, current_y +1);
				}
				else if(counter == 7)
				{
					current = new Point(current_x - 1, current_y -1);
				}
			}

			//We now have a valid point, lets place it in the "grid"
			grid.add(current);
			narShape.grid_placement = current;

			//counter = 0 --> we place to the right (E)
			//counter = 1 --> we place at the bottom (S)
			//counter = 2 --> we place to the left (W)
			//counter = 3 --> we place at the top (N)
			//counter = 4 --> we place to the top-right (NE)
			//counter = 5 --> we place at the bottom-right (SE)
			//counter = 6 --> we place to the bottom-left (SW)
			//counter = 7 --> we place at the top-left (NW)
			if(counter == 0)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX(),
						connectionBounds.getMinY());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX() + margin);
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());

				positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
			}
			else if(counter == 1)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(),
						connectionBounds.getMaxY());

				System.out.println(narShape.screenToLocal(narShape.getBoundsInLocal()).getHeight());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY() + margin);

				positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
			}
			else if(counter == 2)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(),
						connectionBounds.getMinY());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX() - narShape.getBoundsInLocal().getHeight() - margin);
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());

				positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() - narShape.getBoundsInLocal().getWidth()));
			}
			else if(counter == 3)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(),
						connectionBounds.getMinY());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY()  - narShape.getBoundsInLocal().getHeight() - margin);

				positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
			}
			if(counter == 4)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX(),
						connectionBounds.getMinY() );

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX()  + margin);
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY()  - narShape.getBoundsInLocal().getHeight() - margin);

//				positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
			}
			else if(counter == 5)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX(),
						connectionBounds.getMaxY());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX()  + margin);
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY()  + margin);

//				positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
			}
			else if(counter == 6)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMinX() ,
						connectionBounds.getMaxY());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX() - connectionBounds.getWidth() - margin);
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY()  + margin);

//				positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() - narShape.getBoundsInLocal().getWidth()));
			}
			else if(counter == 7)
			{
				local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(),
						connectionBounds.getMinY());

				narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX()  - connectionBounds.getWidth() - margin);
				narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY()   - narShape.getBoundsInLocal().getHeight() - margin);

//				positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
			}

			toCheckNodes.add(testedNode);
			visitedNodes.add(testedNode);
		}

		return counter;
	}

	public void layoutGraph()
	{
		//MAKE SOME KIND OF GRID SO IT IS CLEAR WHERE EACH IS!
		ArrayList<Point> grid = new ArrayList<Point>();
		Point current = new Point(0, 0);
		int current_x = 0;
		int current_y = 0;

		renderAll();
		ArrayList<GrammarNode> visitedNodes = new ArrayList<GrammarNode>();
		Stack<GrammarNode> toCheckNodes = new Stack<GrammarNode>();

		NarrativeShape narShape = owner.nodes.get(0).getNarrativeShape();

		ArrayList<Point> positions = new ArrayList<Point>();

		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N
		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N
		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N
		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N

		//needed in case there are non-connected nodes
		toCheckNodes.addAll(owner.nodes);
		toCheckNodes.add(owner.nodes.get(0));

		narShape = owner.nodes.get(0).getNarrativeShape();

		//calculate the first movement
		Point2D local_translation_point = narShape.screenToLocal(positions.get(0).getX(), positions.get(0).getY());
//		narShape.setTranslateX(local_translation_point.getX());
//		narShape.setTranslateY(local_translation_point.getY());
//
//		positions.get(0).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//		positions.get(0).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));

		//this works but I feel something will need to change (is kind of getting for granted that everything is interconnected
		current_x = 1;
		current_y = 0;
		current = new Point(current_x, current_y);

//		for(GrammarNode daNode : owner.nodes)
		while(!toCheckNodes.isEmpty())
		{
			GrammarNode current_node = toCheckNodes.pop();
			int counter = 1;

			narShape = current_node.getNarrativeShape();
			Bounds localBounds = narShape.localToScreen(narShape.getBoundsInLocal());
			Bounds localBoundsInParent = narShape.getBoundsInParent();

			//In case the node has not been visited before (was not connected to anything that has been examined)
			if(!visitedNodes.contains(current_node))
			{
				visitedNodes.add(current_node);

				//Find an available position!
				while(grid.contains(current))
				{
					if(counter == 0)
					{
						current = new Point(current_x + 1, current_y);
					}
					else if(counter == 1)
					{
						current = new Point(current_x, current_y + 1);
					}
					else if(counter == 2)
					{
						current = new Point(current_x - 1, current_y);
					}
					else
					{
						current = new Point(current_x, current_y - 1);
					}

					counter++;
					counter = counter % 4;
				}

				grid.add(current);
				narShape.grid_placement = current;


				local_translation_point = narShape.screenToLocal(positions.get(0).getX(), positions.get(0).getY());
				narShape.setTranslateX(localBoundsInParent.getMinX() + local_translation_point.getX());
				narShape.setTranslateY(localBoundsInParent.getMinY() + local_translation_point.getY());
			}

			//Iterate actual connections from current_node to others
			counter = -1;
			counter = iterateConnections(current_node, positions, new ArrayList<GrammarNode>(current_node.connections.keySet()), counter, visitedNodes,
					toCheckNodes, grid, current, current_x, current_y, 40.0);

			//little hack to start in the right place
			counter--;
			counter = counter % 8;

			//Iterate nodes that are connected to current_node!
			counter = iterateConnections(current_node, positions, owner.getAllConnectionsToNode(current_node), counter, visitedNodes,
					toCheckNodes, grid, current, current_x, current_y, 40.0);
		}

		renderAll();
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

	public void forceScale(Scale value)
	{
		double auxScale = value.getX();
//		auxScale += (value.getX() - 1.0);

		currentScale = auxScale;
		getTransforms().clear();
		getTransforms().add(value);
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
