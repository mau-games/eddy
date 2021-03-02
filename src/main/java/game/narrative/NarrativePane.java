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

	public void layoutGraph()
	{
		//MAKE SOME KIND OF GRID SO IT IS CLEAR WHERE EACH IS!
		ArrayList<Point> grid = new ArrayList<Point>();
		Point current = new Point(0, 0);
		int current_x = 0;
		int current_y = 0;
//		String current = "0";
//		String towards = "0";

		renderAll();

		ArrayList<String> visitedConnections = new ArrayList<String>();
		ArrayList<GrammarNode> visitedNodes = new ArrayList<GrammarNode>();

		Queue<String> toCheckConnections = new LinkedList<String>();
		Stack<GrammarNode> toCheckNodes = new Stack<GrammarNode>();

		NarrativeShape narShape = owner.nodes.get(0).getNarrativeShape();
		Point2D local_translation_point2 = narShape.screenToLocal(narShape.getLayoutX(),narShape.getLayoutY());

		ArrayList<Point> positions = new ArrayList<Point>();
//		positions.add(new Point(Math.abs(local_translation_point2.getX()),Math.abs(local_translation_point2.getY()))); //N
//		positions.add(new Point(Math.abs(local_translation_point2.getX()),Math.abs(local_translation_point2.getY()))); //N
//		positions.add(new Point(Math.abs(local_translation_point2.getX()),Math.abs(local_translation_point2.getY()))); //N
//		positions.add(new Point(Math.abs(local_translation_point2.getX()),Math.abs(local_translation_point2.getY()))); //N

		Bounds dsa = narShape.getBoundsInLocal();

		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N
		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N
		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N
		positions.add(new Point(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX(), narShape.localToScreen(narShape.getBoundsInLocal()).getMinY())); //N

		toCheckNodes.addAll(owner.nodes);
		toCheckNodes.add(owner.nodes.get(0));

		narShape = owner.nodes.get(0).getNarrativeShape();

//		positions.get(0).setX((int)narShape.getTranslateX());
//		positions.get(0).setY((int)narShape.getTranslateY());

		Point2D local_translation_point = narShape.screenToLocal(positions.get(0).getX(), positions.get(0).getY());
		narShape.setTranslateX(local_translation_point.getX());
		narShape.setTranslateY(local_translation_point.getY());

//		narShape.setLayoutX(positions.get(0).getX());
//		narShape.setLayoutY(positions.get(0).getY());

//		positions.get(0).setX((int)narShape.xPosition.get());
//		positions.get(0).setY((int)narShape.yPosition.get());

//		positions.get(0).setX((int)narShape.localToScreen(narShape.getBoundsInLocal()).getMinX());
//		positions.get(0).setY((int)narShape.localToScreen(narShape.getBoundsInLocal()).getMinY());


//		positions.get(0).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
		positions.get(0).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
//		positions.get(0).setY((int)narShape.getTranslateY());

//		while(!toCheckNodes.isEmpty())

		current_x = 1;
		current_y = 0;
		current = new Point(current_x, current_y);

//		for(GrammarNode daNode : owner.nodes)
		while(!toCheckNodes.isEmpty())
		{
			GrammarNode daNode = toCheckNodes.pop();

			int counter = 1;


			narShape = daNode.getNarrativeShape();
			Bounds localBounds = narShape.localToScreen(narShape.getBoundsInLocal());
			Bounds localBoundsInParent = narShape.getBoundsInParent();

			if(!visitedNodes.contains(daNode))
			{
				visitedNodes.add(daNode);

				while(grid.contains(current))
				{
					if(counter == 1)
					{
						current = new Point(current_x + 1, current_y);
					}
					else if(counter == 2)
					{
						current = new Point(current_x, current_y + 1);
					}
					else if(counter == 3)
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


//				current = new Point(current_x, current_y);
//				grid.add(current);
//				narShape.grid_placement = current;

				local_translation_point = narShape.screenToLocal(positions.get(1).getX(), positions.get(1).getY());
//			narShape.
//			narShape.setLayoutX(local_translation_point.getX());
				narShape.setTranslateX(localBoundsInParent.getMinX() + local_translation_point.getX());
				narShape.setTranslateY(localBoundsInParent.getMinY() + local_translation_point.getY());
			}

//			positions.get(1).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));


//			//Place Node
//			nShape.setLayoutX(positions.get(1).getX());
//			nShape.setLayoutY(positions.get(1).getY());
//
//			positions.get(1).setX((int)nShape.xPosition.get());
//			positions.get(1).setY((int)nShape.yPosition.get());

//			current = narShape.grid_placement;
//			current_x = current.getX();
//			current_y = current.getY();

			counter = 0;

//			daNode.removeAllConnection();

			for(Map.Entry<GrammarNode, Integer> keyValue : daNode.connections.entrySet())
			{
				if(visitedNodes.contains(keyValue.getKey()))
					continue;

				current = daNode.getNarrativeShape().grid_placement;
				current_x = current.getX();
				current_y = current.getY();

				Bounds connectionBounds = daNode.getNarrativeShape().localToScreen(narShape.getBoundsInLocal());

				narShape = keyValue.getKey().getNarrativeShape();
				localBoundsInParent = narShape.getBoundsInParent();
//				localBounds = narShape.getParent().getBoundsInLocal();
//				localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

//				local_translation_point = narShape.screenToLocal(positions.get(counter).getX(), positions.get(counter).getY());
				local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX(), connectionBounds.getMaxY());

//				narShape.setTranslateX(connectionBounds.getMaxX() + localBoundsInParent.getMinX());
//				narShape.setTranslateY(connectionBounds.getMaxY() + localBoundsInParent.getMinY());



				while(grid.contains(current))
				{
					counter++;
					counter = counter % 4;

					if(counter == 1)
					{
						current = new Point(current_x + 1, current_y);
					}
					else if(counter == 2)
					{
						current = new Point(current_x, current_y + 1);
					}
					else if(counter == 3)
					{
						current = new Point(current_x - 1, current_y);
					}
					else
					{
						current = new Point(current_x, current_y - 1);
					}

				}

				grid.add(current);
				narShape.grid_placement = current;

				if(counter == 1)
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX() + 20.0, connectionBounds.getMinY());

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());
					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
				}
				else if(counter == 2)
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(), connectionBounds.getMaxY() + 20.0);

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());

//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
				}
				else if(counter == 3)
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMinX() - connectionBounds.getWidth() - 20.0, connectionBounds.getMinY());

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());
					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() - narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
				}
				else
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(), connectionBounds.getMinY()  - connectionBounds.getHeight() - 20.0);

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());
					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
				}

//				if(counter == 1)
//				{
//					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());
//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
////					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
//				}
//				else if(counter == 2)
//				{
////					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
//				}
//				else if(counter == 3)
//				{
//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() - narShape.getBoundsInLocal().getWidth()));
////					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
//				}
//				else
//				{
////					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
//				}

//				positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//				positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));

//				positions.get(counter).setX((int)narShape.getTranslateX());
//				positions.get(counter).setY((int)narShape.getTranslateY());

				//Place Node
//				nShape.setLayoutX(positions.get(counter).getX() + nShape.getWidth());
//				nShape.setLayoutY(positions.get(counter).getY() +  nShape.getHeight());
//
//				positions.get(counter).setX((int)nShape.xPosition.get());
//				positions.get(counter).setY((int)nShape.yPosition.get());

//				counter++;
//				counter = counter % 4;
				toCheckNodes.add(keyValue.getKey());
				visitedNodes.add(keyValue.getKey());
			}

			counter--;
			counter = counter % 4;

			for(GrammarNode toNode : owner.getAllConnectionsToNode(daNode))
			{
				if(visitedNodes.contains(toNode))
					continue;

				current = daNode.getNarrativeShape().grid_placement;
				current_x = current.getX();
				current_y = current.getY();

				Bounds connectionBounds = daNode.getNarrativeShape().localToScreen(narShape.getBoundsInLocal());

				narShape = toNode.getNarrativeShape();
				localBoundsInParent = narShape.getBoundsInParent();
//				localBounds = narShape.getParent().getBoundsInLocal();
//				localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

//				local_translation_point = narShape.screenToLocal(positions.get(counter).getX(), positions.get(counter).getY());
				local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX(), connectionBounds.getMaxY());

//				narShape.setTranslateX(connectionBounds.getMaxX() + localBoundsInParent.getMinX());
//				narShape.setTranslateY(connectionBounds.getMaxY() + localBoundsInParent.getMinY());



				while(grid.contains(current))
				{
					counter++;
					counter = counter % 4;
					if(counter == 1)
					{
						current = new Point(current_x + 1, current_y);
					}
					else if(counter == 2)
					{
						current = new Point(current_x, current_y + 1);
					}
					else if(counter == 3)
					{
						current = new Point(current_x - 1, current_y);
					}
					else
					{
						current = new Point(current_x, current_y - 1);
					}

				}

				grid.add(current);
				narShape.grid_placement = current;

				if(counter == 1)
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX() + 20.0, connectionBounds.getMinY());

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());
					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
				}
				else if(counter == 2)
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(), connectionBounds.getMaxY() + 20.0);

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());

//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
				}
				else if(counter == 3)
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMaxX() + 20.0, connectionBounds.getMinY());

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());
					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));

					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() - narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
				}
				else
				{
					local_translation_point = narShape.screenToLocal(connectionBounds.getMinX(), connectionBounds.getMinY()  - connectionBounds.getHeight() - 20.0);

					narShape.setTranslateX(local_translation_point.getX() + localBoundsInParent.getMinX());
					narShape.setTranslateY(local_translation_point.getY() + localBoundsInParent.getMinY());
					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());

//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
				}

//				if(counter == 1)
//				{
//					localBounds = narShape.localToScreen(narShape.getBoundsInLocal());
//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
////					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
//				}
//				else if(counter == 2)
//				{
////					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
//				}
//				else if(counter == 3)
//				{
//					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() - narShape.getBoundsInLocal().getWidth()));
////					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));
//				}
//				else
//				{
////					positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//					positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() - narShape.getBoundsInLocal().getHeight()));
//				}

//				positions.get(counter).setX((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinX() + narShape.getBoundsInLocal().getWidth()));
//				positions.get(counter).setY((int)(narShape.localToScreen(narShape.getBoundsInLocal()).getMinY() + narShape.getBoundsInLocal().getHeight()));

//				positions.get(counter).setX((int)narShape.getTranslateX());
//				positions.get(counter).setY((int)narShape.getTranslateY());

				//Place Node
//				nShape.setLayoutX(positions.get(counter).getX() + nShape.getWidth());
//				nShape.setLayoutY(positions.get(counter).getY() +  nShape.getHeight());
//
//				positions.get(counter).setX((int)nShape.xPosition.get());
//				positions.get(counter).setY((int)nShape.yPosition.get());

//				counter++;
//				counter = counter % 4;
				toCheckNodes.add(toNode);
				visitedNodes.add(toNode);
			}
		}

		renderAll();
//
//
//		while(!toCheckNodes.isEmpty())
//		{
//			GrammarNode curNode = toCheckNodes.remove();
//			String curConnection = "";
//
//			if(!toCheckConnections.isEmpty())
//				curConnection = toCheckConnections.remove();
//
//			if(!curConnection.equals("") && !visitedConnections.contains(curConnection))
//			{
//				visitedConnections.add(curConnection);
//			}
//
//			//If we have already been here there is no need to check again
//			if(visitedNodes.contains(curNode))
//				continue;
//
//			visitedNodes.add(curNode);
//
//			NarrativeShape narShape = curNode.getNarrativeShape();
//
//
//			//first add my connections
//			//TODO: this works for every type of connection
//			for(Map.Entry<GrammarNode, Integer> keyValue : curNode.connections.entrySet())
//			{
//				toCheckNodes.add(keyValue.getKey());
//				toCheckConnections.add(Integer.toString(curNode.id)+Integer.toString(keyValue.getKey().id));
//			}
//
//			for(GrammarNode other : owner.nodes)
//			{
//				if(other == curNode)
//					continue;
//
//				if(other.checkConnectionExists(curNode))
//				{
//					toCheckNodes.add(other);
//					toCheckConnections.add(Integer.toString(other.id)+Integer.toString(curNode.id));
//				}
//			}
//		}


		////////////////////////////////////////////////////////////////////////////////////////////////////

//		//if we haven't visited all nodes we know this is not fully connected
//		return nodes.size() == visitedNodes.size();
//
//		for(GrammarNode node : owner.nodes)
//		{
//			NarrativeShape narShape = node.getNarrativeShape();
//			if(!narShape.getRendered())
//			{
////				narShape.getCanvas().draw(MapRenderer.getInstance().renderMap(room));
//				narShape.setRendered(true);
//			}
//
//			if(!getChildren().contains(narShape))
//				getChildren().add(narShape);
//
//			//Now get connections!
////			getChildren().addAll(node.getNarrativeShapeConnections());
//			getChildren().addAll(node.recreateConnectionsBasedPosition());
//
//
//		}
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
