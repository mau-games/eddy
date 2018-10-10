package gui.views;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import game.ApplicationConfig;
import game.Dungeon;
import game.Room;
import game.WorldViewCanvas;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnector;
import gui.views.RoomViewController.EditViewMouseHover;
import gui.views.SuggestionsViewController.MouseEventHandler;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RegisterDoorPosition;
import util.eventrouting.events.RegisterRoom;
import util.eventrouting.events.RequestConnection;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNewRoom;
import util.eventrouting.events.RequestNullRoom;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;
import util.IntField;
import util.Point;
import util.Util;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University*/

public class WorldViewController extends GridPane implements Listener
{

	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	private boolean isActive = false;

	private Button startEmptyBtn = new Button();
	private Button roomNullBtn = new Button ();
	private Button suggestionsBtn = new Button();
	private Button testBtn = new Button();
	private Button connectBtn = new Button();
	
	private IntField widthField = new IntField(1, 20, 11);
	private IntField heightField = new IntField(1, 20, 11);

	private Canvas buttonCanvas;
	private MapRenderer renderer = MapRenderer.getInstance();


	@FXML private GridPane worldViewPane;
	@FXML private StackPane buttonPane;
//	@FXML GridPane gridPane;
	@FXML Pane stackPane;
	@FXML private List<LabeledCanvas> mapDisplays;

	private int row = 0;
	private int col = 0;
	private MapContainer[][] matrix;
	private int size;
	private int viewSize;
	private Node source;
	private Node oldNode;
	private LabeledCanvas canvas;
	
	Dungeon dungeon;
	DungeonDrawer drawer;
	private Canvas brushCanvas;
	private RoomConnector connection;
	
	double anchorX;
	double anchorY;

	Line l;
	
	public WorldViewController() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/WorldView.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException ex) {

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new RegisterRoom(null));
		router.registerListener(this, new RegisterDoorPosition(null, null));
		
		initWorldView();
	}

	public void setActive(boolean state) {
		isActive = state;
	}

	private void initWorldView() 
	{
		connection = new RoomConnector();
		
//		brushCanvas = new Canvas(1000, 1000);
//		StackPane.setAlignment(brushCanvas, Pos.CENTER);
//		
//		brushCanvas.setVisible(false);
//		brushCanvas.setMouseTransparent(true);
//		brushCanvas.setOpacity(1.0f);
//		stackPane.addEventFilter(MouseEvent.MOUSE_MOVED, new EditViewMouseHover());
		worldViewPane.getChildren().add(stackPane);
//		stackPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());
//		clipChildren(stackPane, 12);
		worldButtonEvents();
		initOptions();	


	}
	
	public class EditViewMouseHover implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) 
		{
			brushCanvas.setVisible(false);
			
			if (event.getTarget() instanceof LabeledCanvas) 
			{
				System.out.println("THERE IS SOMETHING HERE");
//				// Show the brush canvas
//				ImageView tile = (ImageView) event.getTarget();
//				brushCanvas.getGraphicsContext2D().clearRect(0, 0, 420, 420);
//				brushCanvas.setVisible(true);
//				util.Point p = mapView.CheckTile(tile);
//				myBrush.Update(event, p, mapView.getMap());
//				
//				renderer.drawBrush(brushCanvas.getGraphicsContext2D(), mapView.getMap().toMatrix(), myBrush, Color.WHITE);
			}
		}
		
	}
	
	//TODO: I WAS HERE!!! Line works with a PANE because stack pane destroys everything based on how it calculates
	//TODO: THIS NEEDS TO BE CHECK BECAUSE OF HOW WE RENDER!!!!!!
	//TODO: this need to be check IDK do we really need to clear the children and create them again? probably there is a better way :D 
	//TODO: There was a better way
	public void initWorldMap(Dungeon dungeon) 
	{
		
		if(widthField == null)
			widthField = new IntField(1, 20, dungeon.defaultWidth);
		
		if(heightField == null)
			heightField = new IntField(1, 20, dungeon.defaultHeight);
		
		stackPane.getChildren().clear();
		this.dungeon = dungeon;
		size = this.dungeon.size;
		viewSize = 500;
		int viewSizeHeight = 500;
		int viewSizeWidth = 500;
		
		stackPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		stackPane.setAlignment(Pos.CENTER);
		StackPane.setAlignment(stackPane, Pos.CENTER);
		
		//dirty code
		int roomCounter= 1;
		boolean createLine = false;
		Line line = new Line();
		
		if(dungeon.size > 1)
		{
	        line.setStartX(-100.0f);
	        line.setStartY(0.0f);
	        line.setEndX(300.0f);
	        line.setEndY(70.0f);
	        line.setStrokeWidth(2);
	        line.setStroke(Color.PINK);
	        line.setMouseTransparent(true);
	      
	        createLine = true;
		}
		
		for( Room room : dungeon.getAllRooms())
		{
			WorldViewCanvas wvc = room.localConfig.getWorldCanvas();
			
			if(!wvc.getRendered())
			{
				wvc.getCanvas().draw(renderer.renderMap(room));
				wvc.setRendered(true);
			}
			
//			Line line = new Line();
//	        line.setStartX(-100.0f);
//	        line.setStartY(0.0f);
//	        line.setEndX(300.0f);
//	        line.setEndY(70.0f);
//	        line.setStrokeWidth(2);
//	        line.setStroke(Color.PINK);
//	        stackPane.getChildren().add(line);
			if(createLine && roomCounter==1)
			{
				line.startXProperty().bind(wvc.xPosition);
		        line.startYProperty().bind(wvc.yPosition);
		        roomCounter++;
			}
			else if(createLine && roomCounter==2)
			{
				line.endXProperty().bind(wvc.xPosition);
		        line.endYProperty().bind(wvc.yPosition);
			}
	        
			
			stackPane.getChildren().add(wvc.getCanvas());
		}
		
		if(createLine)
			  stackPane.getChildren().add(line);
//		

		for (int index = 0; index < size; index++) 
		{
			
			
//			//TODO: the 50 should be scaled --> And the 20... is because of the padding
//			viewSizeHeight = (50 * (dungeon.getRoomByIndex(index).getRowCount())) + 20; //THIS IS PART OF THE SOLUTION
//			viewSizeWidth = (50 * (dungeon.getRoomByIndex(index).getColCount())) + 20; //THIS IS PART OF THE SOLUTION
//			canvas = new LabeledCanvas();
//			canvas.setText("");
//			canvas.setMinSize(viewSizeWidth, viewSizeHeight);
//			canvas.setMaxSize(viewSizeWidth, viewSizeHeight); //THIS IS PART OF THE SOLUTION
//			canvas.setPrefSize(viewSizeWidth, viewSizeHeight);
//			canvas.draw(renderer.renderMap(dungeon.getRoomByIndex(index))); //TODO: HERE!!!
////				canvas.relocate(25, 25);
////			
////			canvas.setMinSize(viewSize, viewSize);
////			canvas.setMaxSize(viewSize, viewSize);
//			stackPane.getChildren().add(canvas);
//
////				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
////						new MouseEventHandler());
//
////				
////				canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventH());
//			canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new MouseEventH());
		}
		
//		boolean voidRoom = matrix[row][col].getMap().getNull();
//		getSuggestionsBtn().setDisable(voidRoom);
//		getStartEmptyBtn().setDisable(voidRoom);

		
	}
	
	public class MouseEventWorldPane implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event) {

//	    			source.setTranslateX(event.getX() + source.getTranslateX() - anchorX);
//	    			source.setTranslateY(event.getY() + source.getTranslateY() - anchorY); 
	    			l.setEndX(event.getX());
	    			l.setEndY(event.getY());
	            }
	            
	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {

	            	anchorX = event.getX();
	    			anchorY = event.getY();
	    			l = new Line();
	    			stackPane.getChildren().add(l);
	    			l.setStartX(event.getX());
	    			l.setStartY(event.getY());
	    			l.setEndX(event.getX());
	    			l.setEndY(event.getY());
	    			l.setStrokeWidth(2.0f);
	    			l.setStroke(Color.PINK);
	            }
	        });
			
		}
	}
	
	private void clipChildren(Region region, double arc)
	{
		final Rectangle outputClip = new Rectangle();
	    outputClip.setArcWidth(arc);
	    outputClip.setArcHeight(arc);
	    region.setClip(outputClip);

	    region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
	        outputClip.setWidth(newValue.getWidth());
	        outputClip.setHeight(newValue.getHeight());
	    });
	}
	
	private void zoom()
	{
		int s = stackPane.getChildren().size();
		viewSize += 10;
		for(int i = 0; i < s; i++)
		{
			canvas = (LabeledCanvas)stackPane.getChildren().get(i); //DIRTy
			canvas.setMaxSize(viewSize, viewSize);
		}
		
		
	}

	private void initOptions() {				
		buttonCanvas = new Canvas(1000, 1000);
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		buttonPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(false);
		buttonCanvas.setMouseTransparent(true);

//		getStartEmptyBtn().setTranslateX(800);
//		getStartEmptyBtn().setTranslateY(-200);
		getRoomNullBtn().setTranslateX(800);
		getRoomNullBtn().setTranslateY(0);
		getSuggestionsBtn().setTranslateX(800);
		getSuggestionsBtn().setTranslateY(200);
		
		connectBtn.setTranslateX(800);
		connectBtn.setTranslateY(-200);
		
		testBtn.setTranslateX(800);
		testBtn.setTranslateY(400);
		
		heightField.setStyle("-fx-text-inner-color: white;");
		heightField.setPromptText("HEIGHT");
		heightField.setTranslateX(700);
		heightField.setTranslateY(-300);
		
		widthField.setStyle("-fx-text-inner-color: white;");
		widthField.setPromptText("WIDTH");
		widthField.setTranslateX(810);
		widthField.setTranslateY(-300);
		
//		getStartEmptyBtn().setMinSize(500, 100);
		getRoomNullBtn().setMinSize(500, 100);
		getSuggestionsBtn().setMinSize(500, 100);
		connectBtn.setMinSize(500, 100);
		testBtn.setMinSize(500, 100);
		widthField.setMinSize(100, 50);
		widthField.setMaxSize(100, 50);
		heightField.setMinSize(100, 50);
		heightField.setMaxSize(100, 50);

//		buttonPane.getChildren().add(getStartEmptyBtn());
		buttonPane.getChildren().add(getRoomNullBtn());
		buttonPane.getChildren().add(getSuggestionsBtn());
		buttonPane.getChildren().add(connectBtn);
		buttonPane.getChildren().add(testBtn);
		buttonPane.getChildren().add(heightField);
		buttonPane.getChildren().add(widthField);
		
		connectBtn.setText("Connect ROOMS!");
		connectBtn.setDisable(false);
		testBtn.setText("ZOOM IN");
		testBtn.setDisable(false);
		
		getStartEmptyBtn().setText("Edit room");
		getStartEmptyBtn().setTooltip(new Tooltip("Go to room view and start designing"));
		getRoomNullBtn().setText("Enable/Disable room");
		getRoomNullBtn().setTooltip(new Tooltip("Makes the room inaccessible for more complex designs"));
		getSuggestionsBtn().setText("Start with our suggestions");
		getSuggestionsBtn().setTooltip(new Tooltip("Start with our suggested designs as generated by genetic algorithms"));

	}



	@Override
	public void ping(PCGEvent e) {
		if(e instanceof RegisterRoom)
		{
			System.out.println("HERE?");
			//Get room
			if(connection.from == null)
			{
				connection.from = (Room)e.getPayload();
				System.out.println("ROOM FROM: " + connection.from);
			}
			else if(connection.from != (Room)e.getPayload())
			{
				connection.to = (Room)e.getPayload();
			}
		}
		else if(e instanceof RegisterDoorPosition)
		{
			//Get position
			if(connection.fromPos == null && ((RegisterDoorPosition)e).room == connection.from)
			{
				connection.fromPos = (Point)e.getPayload();
				System.out.println("POS FROM: " + connection.fromPos);
			}
			else if( ((RegisterDoorPosition)e).room == connection.to )
			{
				connection.toPos = (Point)e.getPayload();
				router.postEvent(new RequestConnection(dungeon, -1, connection.from, connection.to, connection.fromPos, connection.toPos));
				connection = new RoomConnector();
			}
		}

	}

	public Button getStartEmptyBtn() {
		return startEmptyBtn;
	}

	public void setStartEmptyBtn(Button startEmptyBtn) {
		this.startEmptyBtn = startEmptyBtn;
	}

	public Button getRoomNullBtn() {
		return roomNullBtn;
	}

	public void setRoomNullBtn(Button roomNullBtn) {
		this.roomNullBtn = roomNullBtn;
	}

	public Button getSuggestionsBtn() {
		return suggestionsBtn;
	}

	public void setSuggestionsBtn(Button suggestionsBtn) {
		this.suggestionsBtn = suggestionsBtn;
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
	            	
	    			source.setTranslateX(event.getX() + source.getTranslateX() - anchorX);
	    			source.setTranslateY(event.getY() + source.getTranslateY() - anchorY); 

	            }

	            //TODO: Trying to limit if the canvas is outside the pane --> need to give it a bettter try
	            private boolean outSideParentBounds( Bounds childBounds, double newX, double newY) {

	                Bounds parentBounds = source.getParent().getLayoutBounds();
	                
	                newX += parentBounds.getMaxX() / 2;
	                newY += parentBounds.getMaxY() / 2;

	                if((childBounds.getMaxX() / 2.0) + newX > parentBounds.getMaxX())
	                {
//	                	source.setTranslateX(source.getTranslateX() - 10);
	                	return true;
	                }
	                	
	                
//	                //check if too left
//	                if( parentBounds.getMaxX() <= (newX + childBounds.getMaxX()) ) {
//	                    return true ;
//	                }
//
//	                //check if too right
//	                if( parentBounds.getMinX() >= (newX + childBounds.getMinX()) ) {
//	                    return true ;
//	                }
//
//	                //check if too down
//	                if( parentBounds.getMaxY() <= (newY + childBounds.getMaxY()) ) {
//	                    return true ;
//	                }
//
//	                //check if too up
//	                if( parentBounds.getMinY() >= (newY + childBounds.getMinY()) ) {
//	                    return true ;
//	                }

	                return false;

	            }
	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {

	            	anchorX = event.getX();
	    			anchorY = event.getY();
	    		
	            }
	        });
			
		}
	}
	
	
	public class MouseEventHandler implements EventHandler<MouseEvent> {


		@Override
		public void handle(MouseEvent event) {
			source = (Node)event.getSource() ;
			Integer colIndex = GridPane.getColumnIndex(source);
			Integer rowIndex = GridPane.getRowIndex(source);
			row = rowIndex;
			col = colIndex;
			getRoomNullBtn().setDisable(false);
			if (matrix[row][col].getMap().getNull()) {
				//disable
				getSuggestionsBtn().setDisable(true);
				getStartEmptyBtn().setDisable(true);
			}
			else {
				//enable
				getSuggestionsBtn().setDisable(false);
				getStartEmptyBtn().setDisable(false);	
			}
			

			source.setOnMouseClicked(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	    			source.setStyle("-fx-background-color:#fcdf3c;");
	    			if (oldNode != null) {
	    				oldNode.setStyle("-fx-background-color:#2c2f33;");
	    			}	 
	    			oldNode = source;
	    			

	            }
	        });
			
			source.setOnMouseExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	    			source.setStyle("-fx-background-color:#fcdf3c;");
	    			
	            }
	        });
			
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	    			source.setStyle("-fx-background-color:#fcdf3c;");
	    			
	            }
	        });

		}

	}

	private void worldButtonEvents() 
	{
		
		getStartEmptyBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				
				router.postEvent(new RequestEmptyRoom(matrix[row][col], row, col, matrix));
			}

		}); 
		getSuggestionsBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				router.postEvent(new RequestSuggestionsView(matrix[row][col], row, col, matrix, 6));
			}

		}); 


		getRoomNullBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				router.postEvent(new RequestNullRoom(matrix[row][col], row, col, matrix));
			}

		}); 
		
		connectBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("Pressed Connect btn");

//				zoom();
//				router.postEvent(new RequestNewRoom(dungeon, -1, heightField.getValue(), widthField.getValue()));
//				if(dungeon.size > 1)
//				{
//					Room from = dungeon.getRoomByIndex(0);
//					Room to = dungeon.getRoomByIndex(Util.getNextInt(1, dungeon.size));
////					Point fromPos = 
//					router.postEvent(new RequestConnection(dungeon, -1, from, to, new Point(0, 0), new Point(0, (to.getRowCount() -1) / 2)));
//				}
				
				if(DungeonDrawer.getInstance().getBrush() instanceof MoveElementBrush)
				{
					DungeonDrawer.getInstance().changeToConnector();
				}
				else
				{
					DungeonDrawer.getInstance().changeToMove();
				}
			}

		}); 
		
		testBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("Pressed test btn");
//				zoom();
				router.postEvent(new RequestNewRoom(dungeon, -1, heightField.getValue(), widthField.getValue()));
			}

		}); 

	}
	
	
	
	private String matrixToString() {
		//create large string
		String largeString = "";
		int j = 1;

		for (MapContainer[] outer : matrix) {

			for (int k = 0; k < outer[0].getMap().toString().length(); k++) {

				if (outer[0].getMap().toString().charAt(k) != '\n') {
					largeString += outer[0].getMap().toString().charAt(k);

				}
				if (outer[0].getMap().toString().charAt(k) == '\n') {
					while (j < 3) {

						for (int i = (k - 11); i < k; i++) {
							largeString += outer[j].getMap().toString().charAt(i);

						}
						j++;
					}
					j = 1;
					largeString += outer[0].getMap().toString().charAt(k);
				}

			}

		}
		return largeString;
	}
}

