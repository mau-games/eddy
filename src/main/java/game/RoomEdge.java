package game;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestConnectionRemoval;

public class RoomEdge 
{
	protected Node source;
	
	public RoomEdgeLine graphicElement;
	
	//This can be a struct
	public Room from;
	public Room to;
	public Point fromPosition; //this pos actually is the one of the door
	public Point toPosition; //this pos actually is the one of the door
	
	DoubleProperty fX = new SimpleDoubleProperty();
	DoubleProperty fY = new SimpleDoubleProperty();
	DoubleProperty tX = new SimpleDoubleProperty();
	DoubleProperty tY = new SimpleDoubleProperty();
	
	public boolean rendered = false;

	public RoomEdge(Room from, Room to, Point fromPosition, Point toPosition)
	{
		
		fX.bind(Bindings.add(Bindings.add(
				Bindings.multiply(fromPosition.getX(), from.localConfig.getWorldCanvas().tileSizeWidth), 
				Bindings.divide(from.localConfig.getWorldCanvas().tileSizeWidth, 2)), 
				from.localConfig.getWorldCanvas().xPosition));
		
		fY.bind(Bindings.add(Bindings.add(
				Bindings.multiply(fromPosition.getY(), from.localConfig.getWorldCanvas().tileSizeHeight), 
				Bindings.divide(from.localConfig.getWorldCanvas().tileSizeHeight, 2)), 
				from.localConfig.getWorldCanvas().yPosition));
		
		tX.bind(Bindings.add(Bindings.add(
				Bindings.multiply(toPosition.getX(), to.localConfig.getWorldCanvas().tileSizeWidth), 
				Bindings.divide(to.localConfig.getWorldCanvas().tileSizeWidth, 2)), 
				to.localConfig.getWorldCanvas().xPosition));
		
		tY.bind(Bindings.add(Bindings.add(
				Bindings.multiply(toPosition.getY(), to.localConfig.getWorldCanvas().tileSizeHeight), 
				Bindings.divide(to.localConfig.getWorldCanvas().tileSizeHeight, 2)), 
				to.localConfig.getWorldCanvas().yPosition));
		
		System.out.println("LineF pos: (" + fX.getValue() + "," + fY.getValue() + ")" );

		graphicElement = new RoomEdgeLine(fX, fY, tX, tY);
		graphicElement.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventEdge());
		
		this.from = from;
		this.to = to;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
	}
	
	public class MouseEventEdge implements EventHandler<MouseEvent>
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
	            	graphicElement.setStrokeWidth(8);
	            }

	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	EventRouter.getInstance().postEvent(new RequestConnectionRemoval(getSelf(), null, -1));
	            }
	        });

			source.setOnMouseExited(new EventHandler<MouseEvent>() {

			  @Override
	            public void handle(MouseEvent event) 
	            {
	            	graphicElement.setStrokeWidth(2);
	            }

	        });
		}
	}

	//Ugly things one has to do :( 
	protected RoomEdge getSelf()
	{
		return this;
	}
	
	public String print()
	{
		return "Room edge between Room: " + from.hashCode() + " at (" + fromPosition.getX() + "," + fromPosition.getY() + ") and Room: " + to.hashCode() + " at (" + toPosition.getX() + "," + toPosition.getY() + ")";
	}
}
