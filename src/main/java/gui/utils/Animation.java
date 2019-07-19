package gui.utils;

import game.MapContainer;
import gui.utils.DungeonDrawer.DungeonBrushes;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.FocusRoom;
import util.eventrouting.events.RequestRoomView;

/***
 * Class From https://stackoverflow.com/questions/28183667/how-i-can-stop-an-animated-gif-in-javafx 
 * @author Roland
 *
 */
public class Animation extends Transition {

    private ImageView imageView;
    private int count;

    private int lastIndex;

    private Image[] sequence;
    
    protected Node source;
	private double dragAnchorX;
	private double dragAnchorY;
    
	public static DataFormat player = new DataFormat("initPos/player");
	
    public Animation() {
    }

    public Animation( Image[] sequence, double durationMs) {
        init( sequence, durationMs);
    }

    protected void init( Image[] sequence, double durationMs) {
        this.imageView = new ImageView(sequence[0]);
        this.sequence = sequence;
        this.count = sequence.length;

        setCycleCount(1);
        setCycleDuration(Duration.millis(durationMs));
        setInterpolator(Interpolator.LINEAR);

    }

    protected void interpolate(double k) {

        final int index = Math.min((int) Math.floor(k * count), count - 1);
        if (index != lastIndex) {
            imageView.setImage(sequence[index]);
            lastIndex = index;
        }

    }

    public ImageView getView() {
        return imageView;
    }
    
    public class MouseEventDrag implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
//			
//			source.setOnMouseDragEntered(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//	            	{
//		            	borderCanvas.setVisible(true);
//		            	drawBorder();
//	            	}
//	            	else
//	            	{
//	            		highlight(true);
//	            	}
//
//	            	DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);
//	            }
//	        });
			
			source.setOnDragDetected(new EventHandler<MouseEvent>() {
		            public void handle(MouseEvent event) {
//		            	source.setMouseTransparent(true);
//		            	Dragboard db = getView().startDragAndDrop(TransferMode.MOVE);
//		                ClipboardContent content = new ClipboardContent();
//		                // Store node ID in order to know what is dragged.
//		                content.putString(getView().getId());
//		                db.setContent(content);
//		                event.consume();
		            }
		        });

			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	source.setTranslateX(event.getX() + source.getTranslateX() - dragAnchorX);
	    			source.setTranslateY(event.getY() + source.getTranslateY() - dragAnchorY); 
//	    			event.consume();
	            }

	        });
			
//			source.setOnMouseDragOver(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	
//	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//	            	{
//	            		currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//		            	drawBorder();
//	            	}
//
//	            }
//
//	        });
//			
//			source.setOnMouseDragReleased(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//	            	
//	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//	            	{
//	            		if(owner.isPointInBorder(currentBrushPosition))
//	            			DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
//	            	}
//	            	else
//	            	{
//	            		DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
//	            	}
//	            }
//
//	        });
//			
//			source.setOnMouseDragExited(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	borderCanvas.setVisible(false);
//	            }
//
//	        });
//			event.consume();
		}
	}
    
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
//			
//			//1) Mouse enters the canvas of the room --> I can fire the event here, no?
//			source.setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//	            	{
//		            	borderCanvas.setVisible(true);
//		            	drawBorder();
//	            	}
//	            	else
//	            	{
//	            		highlight(true);
//	            	}
//
//	            	DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner); //This could also use the event actually :O
//	            }
//
//	        });
//			
//			//2) mouse is moved around the map
//			source.setOnMouseMoved(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));            	
//	            	drawBorder();
//	            }
//	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
	            	DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.INITIAL_ROOM);
//	            	event.consume();
//	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//	            	
//	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
//	            	{
//	            		if(owner.isPointInBorder(currentBrushPosition))
//	            			DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
//	            	}
//	            	else
//	            	{
//	            		DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
//	            	}

	            }
	        });
//			
//			source.setOnMouseReleased(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
//	            }
//	        });
//
//			source.setOnMouseExited(new EventHandler<MouseEvent>() {
//
//	            @Override
//	            public void handle(MouseEvent event) 
//	            {
//	            	borderCanvas.setVisible(false);
//	            }
//
//	        });
		}
	}

}