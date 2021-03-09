package game;


import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import finder.geometry.Bitmap;
import gui.controls.Brush;
import gui.controls.LabeledCanvas;
import gui.utils.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
	private double prevPositionX;
	private double prevPositionY;
	
	
//	public double tileSizeWidth; //TODO: public just to test
//	public double tileSizeHeight;
	private Node source;
	
	//CANVAS
	private Canvas borderCanvas;
	private Canvas tileCanvas;
	private Canvas pathCanvas;
	private Canvas interFeasibilityCanvas; //creisi
	
	public DoubleProperty xPosition; //TODO: public just to test
	public DoubleProperty yPosition;
	
	public DoubleProperty tileSizeWidth; //TODO: public just to test
	public DoubleProperty tileSizeHeight;

	private Point currentBrushPosition = new Point();
	private Bitmap questBitmap = new Bitmap();
	private boolean doublePos = false;

	public WorldViewCanvas(Room owner)
	{
		this.owner = owner;
		worldGraphicNode = new LabeledCanvas("pipote");
		worldGraphicNode.setText("pipote");
		//Events to the graphic node
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventDrag());
		worldGraphicNode.setOnDragDetected(new EventHandler<MouseEvent>() 
		{
            @Override
            public void handle(MouseEvent event) //TODO: THERE ARE SOME ERRORS FROM THIS POINT!!
            {
            	worldGraphicNode.startFullDrag();
            }
        });
		worldGraphicNode.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			source = (Node)event.getSource();
			if (DungeonDrawer.getInstance().getBrush() instanceof QuestPositionBrush){
				currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
				if (questBitmap.contains(Point.castToGeometry(currentBrushPosition))){
					EventRouter.getInstance().postEvent(
							new QuestPositionUpdate(
									new finder.geometry.Point(
											currentBrushPosition.getX(),
											currentBrushPosition.getY()),
									owner,
									doublePos));
				} else {
					EventRouter.getInstance().postEvent(new QuestPositionInvalid());
				}
			}
		});

		EventRouter.getInstance().registerListener(this::ping, new RequestDisplayQuestTilesSelection(null));
		EventRouter.getInstance().registerListener(this::ping, new RequestDisplayQuestTilesUnselection(false));
		EventRouter.getInstance().registerListener(this::ping, new RequestDisplayQuestTilePosition());
		
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

		tileCanvas = new Canvas(viewSizeHeight, viewSizeHeight);
		worldGraphicNode.getChildren().add(tileCanvas);
		tileCanvas.setVisible(false);
		tileCanvas.setMouseTransparent(true);
		
		pathCanvas = new Canvas(viewSizeHeight, viewSizeHeight);
		worldGraphicNode.getChildren().add(pathCanvas);
		pathCanvas.setVisible(true);
		pathCanvas.setMouseTransparent(true);
		
		interFeasibilityCanvas = new Canvas(viewSizeHeight, viewSizeHeight);
		worldGraphicNode.getChildren().add(interFeasibilityCanvas);
		interFeasibilityCanvas.setVisible(true); //This should be controlled by a toggle button (simple)
		interFeasibilityCanvas.setMouseTransparent(true);
	}

	private void ping(PCGEvent e) {
		if (e instanceof RequestDisplayQuestTilesSelection){
			tileCanvas.setVisible(true);
			questBitmap.clearAllPoints();
			((List<TileTypes>)e.getPayload()).forEach(tileTypes -> {
				owner.isIntraFeasible();
				List<finder.geometry.Point> walkableTilesPoints = owner.walkableTiles.getPoints();

				switch(tileTypes){
					case NPC:
						questBitmap
								.AddAllPoints(owner.npcTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case KNIGHT:
						questBitmap
								.AddAllPoints(owner.knightTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case WIZARD:
						questBitmap
								.AddAllPoints(owner.wizardTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case DRUID:
						questBitmap
								.AddAllPoints(owner.druidTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case BOUNTYHUNTER:
						questBitmap
								.AddAllPoints(owner.bountyhunterTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case BLACKSMITH:
						questBitmap
								.AddAllPoints(owner.blacksmithTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case MERCHANT:
						questBitmap
								.AddAllPoints(owner.merchantTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case THIEF:
						questBitmap
								.AddAllPoints(owner.thiefTiles.getPoints()
										.stream()
										.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
										.collect(Collectors.toList()));
						break;
					case ENEMY:
						questBitmap.AddAllPoints(owner.enemyTiles.getPoints().stream()
								.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
								.collect(Collectors.toList()));
						break;
					case TREASURE:
						questBitmap.AddAllPoints(owner.treasureTiles.getPoints().stream()
								.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
								.collect(Collectors.toList()));
						break;
					case ENEMY_BOSS:
						questBitmap.AddAllPoints(owner.bossTiles.getPoints().stream()
								.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
								.collect(Collectors.toList()));
						break;
					case ITEM:
						questBitmap.AddAllPoints(owner.itemTiles.getPoints().stream()
								.filter(point -> walkableTilesPoints.stream().anyMatch(point::equals))
								.collect(Collectors.toList()));
						break;
					case FLOOR:
						questBitmap.AddAllPoints(walkableTilesPoints);
						break;
				}
		});
			drawTiles(Color.GREEN);
		} else if (e instanceof RequestDisplayQuestTilesUnselection){
//			System.out.println("UnDisplayQuestTiles");
			doublePos = (boolean) e.getPayload();
			tileCanvas.setVisible(doublePos);
			if (doublePos) {
				questBitmap.clearAllPoints();
				questBitmap.AddAllPoints(owner.merchantTiles.getPoints());
				questBitmap.AddAllPoints(owner.thiefTiles.getPoints());
				questBitmap.AddAllPoints(owner.blacksmithTiles.getPoints());
				drawTiles(Color.GREEN);
			}
		} else if (e instanceof RequestDisplayQuestTilePosition){
				if (!tileCanvas.isVisible()){
					tileCanvas.setVisible(true);
					questBitmap.clearAllPoints();
					for (QuestPositionUpdate update: ((RequestDisplayQuestTilePosition) e).getPos()) {
						if (update != null){
							if (owner != null){
								if (update.getRoom().equals(owner)){
									questBitmap.addPoint(update.getPoint());
								}
							}
						}
					}
					drawTiles(Color.BLUEVIOLET);
			}
		}
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

		tileCanvas.setWidth(viewSizeWidth);
		tileCanvas.setHeight(viewSizeHeight);
		
		pathCanvas.setWidth(viewSizeWidth);
		pathCanvas.setHeight(viewSizeHeight);
		
		interFeasibilityCanvas.setWidth(viewSizeWidth);
		interFeasibilityCanvas.setHeight(viewSizeHeight);
		
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
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
	            	{
		            	borderCanvas.setVisible(true);
		            	drawBorder();
	            	}
	            	else
	            	{
	            		highlight(true);
	            	}

					if (DungeonDrawer.getInstance().getBrush() != null) {
						DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner);
					}
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
	            	else if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
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
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
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
	            	
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
	            	{
	            		if(owner.isPointInBorder(currentBrushPosition))
	            			DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
	            	}
	            	else
	            	{
						if (DungeonDrawer.getInstance().getBrush() != null) {
							DungeonDrawer.getInstance().getBrush().onReleaseRoom(owner, currentBrushPosition);
						}
	            	}
	            	
	            	Bounds ltoS = worldGraphicNode.localToScene(worldGraphicNode.getBoundsInLocal());
	            	double newPositionX = ltoS.getMaxX() - (ltoS.getWidth() / 2.0);
	            	double newPositionY = ltoS.getMaxY() - (ltoS.getHeight() / 2.0);
	            	
	            	ActionLogger.getInstance().storeAction(ActionType.CHANGE_POSITION, 
															View.WORLD, 
															TargetPane.WORLD_MAP_PANE,
															false,
															owner,
															prevPositionX, //Point A
															prevPositionY, //Point A
															newPositionX, //Point B
															newPositionY //point B
														);}

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
			
			//1) Mouse enters the canvas of the room --> I can fire the event here, no?
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	ShapeBrush brush = DungeonDrawer.getInstance().getBrush();
	            	if(brush instanceof RoomConnectorBrush)
	            	{
		            	borderCanvas.setVisible(true);
		            	drawBorder();
	            	} else if (brush instanceof QuestPositionBrush){
	            		tileCanvas.setVisible(true);
	            		drawTiles(Color.GREEN);
					}
	            	else
	            	{
	            		highlight(true);
	            	}


	            	if (brush != null) {
	            		DungeonDrawer.getInstance().getBrush().onEnteredRoom(owner); //This could also use the event actually :O
					}

	            }

	        });
			
			//2) mouse is moved around the map
			source.setOnMouseMoved(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	ShapeBrush brush = DungeonDrawer.getInstance().getBrush();
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
	            	if(brush instanceof RoomConnectorBrush) {
						drawBorder();
					} else if (brush instanceof QuestPositionBrush){
	            		tileCanvas.setVisible(true);
	            		drawTiles(Color.GREEN);
					}
	            }
	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	if(event.getClickCount() == 2) //Works --> Double click
	            	{
	            		//TODO: I think is better that the dungeon receives an event to request room view but maybe that will be too convoluted
	            		MapContainer mc = new MapContainer(); // this map container thingy, idk, me not like it
	            		mc.setMap(owner);
	            		if (DungeonDrawer.getInstance().getBrush() != null && !(DungeonDrawer.getInstance().getBrush() instanceof QuestPositionBrush)){
							EventRouter.getInstance().postEvent(new RequestRoomView(mc, 0, 0, null));
						}
	            	}
	            	
	            	EventRouter.getInstance().postEvent(new FocusRoom(owner, null));
	            	
	            	dragAnchorX = event.getX();
	            	dragAnchorY = event.getY();
	            	Bounds ltoS = worldGraphicNode.localToScene(worldGraphicNode.getBoundsInLocal());
	            	prevPositionX = ltoS.getMaxX() - (ltoS.getWidth() / 2.0);
	            	prevPositionY = ltoS.getMaxY() - (ltoS.getHeight() / 2.0);
	            	
	            	currentBrushPosition =  new Point((int)( event.getX() / tileSizeWidth.get()), (int)( event.getY() / tileSizeHeight.get() ));
	            	
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnectorBrush)
	            	{
	            		if(owner.isPointInBorder(currentBrushPosition))
	            			DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
	            	}
	            	else
	            	{
						if (DungeonDrawer.getInstance().getBrush() != null) {
							DungeonDrawer.getInstance().getBrush().onClickRoom(owner,currentBrushPosition);
						}
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
	            }

	        });
		}
	}
	
	public synchronized void forcePathDrawing(boolean visibility)
	{
		pathCanvas.setVisible(visibility);
		drawPath();
	}
	
	public void setInterFeasibilityVisible(boolean visibility)
	{
		interFeasibilityCanvas.setVisible(visibility);
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

	private synchronized void drawTiles(Color color)
	{
		if(tileCanvas.isVisible())
		{
			tileCanvas.getGraphicsContext2D().clearRect(0, 0, tileCanvas.getWidth(), tileCanvas.getHeight());
			MapRenderer.getInstance().drawEligibleTiles(tileCanvas.getGraphicsContext2D(),
					owner.matrix,
					questBitmap,
					Point.castToGeometry(currentBrushPosition) ,
					color);
		}
	}

	private synchronized  void drawTile(finder.geometry.Point... points){
		if(tileCanvas.isVisible())
		{
			Bitmap bitmap = new Bitmap();
			bitmap.AddAllPoints(Arrays.asList(points));
			tileCanvas.getGraphicsContext2D().clearRect(0, 0, tileCanvas.getWidth(), tileCanvas.getHeight());
			MapRenderer.getInstance().drawEligibleTiles(tileCanvas.getGraphicsContext2D(),
					owner.matrix,
					bitmap,
					Point.castToGeometry(currentBrushPosition) ,
					Color.BLUEVIOLET);
		}
	}
	
	private synchronized void drawPath()
	{
		if(pathCanvas.isVisible())
		{
			pathCanvas.getGraphicsContext2D().clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
	    	MapRenderer.getInstance().drawRoomPath(pathCanvas.getGraphicsContext2D(), 
	    			owner.matrix, 
	    			owner.path, 
	    			Color.CYAN);
		}
	}
	
	public synchronized void drawInterFeasibility()
	{
		if(interFeasibilityCanvas.isVisible())
		{
			interFeasibilityCanvas.getGraphicsContext2D().clearRect(0, 0, interFeasibilityCanvas.getWidth(), interFeasibilityCanvas.getHeight());
	    	MapRenderer.getInstance().drawRoomPath(interFeasibilityCanvas.getGraphicsContext2D(), 
	    			owner.matrix, 
	    			owner.nonInterFeasibleTiles, 
	    			Color.RED);
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
