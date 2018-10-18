package gui.views;

import java.io.IOException;
import java.util.List;

import javax.swing.DebugGraphics;

import game.ApplicationConfig;
import game.Dungeon;
import game.DungeonPane;
import game.Room;
import game.RoomEdge;
import game.WorldViewCanvas;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnector;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNewRoom;
import util.eventrouting.events.RequestNullRoom;
import util.eventrouting.events.RequestSuggestionsView;
import util.IntField;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University*/

public class WorldViewController extends BorderPane implements Listener
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


//	@FXML private BorderPane rootWorldViewPane;
	@FXML private StackPane buttonPane;
//	@FXML GridPane gridPane;
	@FXML Pane worldPane;
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
	
	double anchorX;
	double anchorY;

	private Line auxLine;
	
	private Pane roomGroup;
	
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

		initWorldView();
	}

	public void setActive(boolean state) {
		isActive = state;
	}

	private void initWorldView() 
	{
		auxLine = new Line();
		auxLine.setStrokeWidth(2.0f);
		auxLine.setStroke(Color.PINK);
		auxLine.setMouseTransparent(true);
		
		buttonPane = new StackPane();
		
//		rootWorldViewPane.getChildren().add(worldPane);
		setCenter(worldPane);
		setRight(buttonPane);
		worldPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());
		roomGroup = new Pane();
		clipChildren(worldPane, 12);
		worldButtonEvents();
		initOptions();	


	}
	
	public void initWorldMap(Dungeon dungeon) 
	{
		
		if(widthField == null)
			widthField = new IntField(1, 20, dungeon.defaultWidth);
		
		if(heightField == null)
			heightField = new IntField(1, 20, dungeon.defaultHeight);
		
		this.dungeon = dungeon;
		worldPane.getChildren().clear();
		roomGroup.getChildren().clear();
		size = this.dungeon.size;
		viewSize = 500;
		
		worldPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		
//		roomGroup.maxHeight(worldPane.getHeight());
//		roomGroup.minHeight(worldPane.getHeight());
//		roomGroup.prefHeight(worldPane.getHeight());
//		roomGroup.maxHeight(worldPane.getWidth());
//		roomGroup.minHeight(worldPane.getWidth());
//		roomGroup.prefHeight(worldPane.getWidth());
		
		roomGroup.setMaxSize(worldPane.getMaxWidth(), worldPane.getMaxHeight());
		roomGroup.setMinSize(worldPane.getMinWidth(), worldPane.getMinWidth());
		roomGroup.setPrefSize(1000,1000);
		roomGroup.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		roomGroup.setCursor(Cursor.CROSSHAIR);
//		roomGroup.setAutoSizeChildren(false);
		
		dungeon.dPane.renderAll();
		worldPane.getChildren().add(dungeon.dPane);
		
//		System.out.println(roomGroup.isResizable());
//		
//		//Edges
//		for(RoomEdge e : dungeon.network.edges())
//		{
////			if(!e.rendered)
////			{
////				
////			}
//			
////			worldPane.getChildren().add(e.graphicElement);
//			roomGroup.getChildren().add(e.graphicElement);
//		}
//		
//		//ROOMS
//		for( Room room : dungeon.getAllRooms())
//		{
//			WorldViewCanvas wvc = room.localConfig.getWorldCanvas();
//			
//			if(!wvc.getRendered())
//			{
//				wvc.getCanvas().draw(renderer.renderMap(room));
//				wvc.setRendered(true);
//			}
//
//			
////			worldPane.getChildren().add(wvc.getCanvas());
//			roomGroup.getChildren().add(wvc.getCanvas());
//			wvc.setParent();
//		}
//		
//		worldPane.getChildren().add(roomGroup);
	}
	
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
					System.out.println(event.getDeltaY());
//					scaleRooms((int)event.getDeltaY());
//					for( Room room : dungeon.getAllRooms())
//            		{
//            			WorldViewCanvas wvc = room.localConfig.getWorldCanvas();
//            			wvc.getCanvas().setScaleX(wvc.getCanvas().getScaleX() + (0.01 * event.getDeltaY()));
//            			wvc.getCanvas().setScaleY(wvc.getCanvas().getScaleY() + (0.01 * event.getDeltaY()));
//            		}
					
//					roomGroup.setScaleX(roomGroup.getScaleX() + (0.01 * event.getDeltaY()));
//        			roomGroup.setScaleY(roomGroup.getScaleY() + (0.01 * event.getDeltaY()));
        			
        			
        			
        			Scale newScale = new Scale();
        	        newScale.setPivotX(event.getX());
        	        newScale.setPivotY(event.getY());
        	        
        	        for(Node child : worldPane.getChildren()) 
            		{
            			newScale.setX( child.getScaleX() + (0.001 * event.getDeltaY()) );
            	        newScale.setY( child.getScaleY() + (0.001 * event.getDeltaY()) );
            	        ((DungeonPane)child).tryScale(newScale);
            		}
//        	        newScale.setX( worldPane.getScaleX() + (0.001 * event.getDeltaY()) );
//        	        newScale.setY( worldPane.getScaleY() + (0.001 * event.getDeltaY()) );
//        	        
//        	        worldPane.getTransforms().add(newScale);
//        	        roomGroup.setPrefSize(roomGroup.getPrefWidth() + Math.abs((event.getDeltaY())), roomGroup.getPrefHeight()  + Math.abs((event.getDeltaY())));
//        			
        			event.consume();
				}

			});
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event)
	            {
	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnector)
	            	{
		    			auxLine.setEndX(event.getX());
		    			auxLine.setEndY(event.getY());
	            	}
	            	else if(event.getTarget() == worldPane && event.isMiddleButtonDown()) //TODO: WORK IN PROGRESS
	            	{
//	            		for( Room room : dungeon.getAllRooms())
//	            		{
//	            			WorldViewCanvas wvc = room.localConfig.getWorldCanvas();
//	            			wvc.getCanvas().setTranslateX(wvc.getCanvas().getBoundsInParent().getMinX() + (event.getX() - anchorX));
//	            			wvc.getCanvas().setTranslateY(wvc.getCanvas().getBoundsInParent().getMinY() + event.getY() - anchorY); 
//	            		}
//	            		for(Node child : worldPane.getChildren())
//	            		{
//	            			if(child instanceof Line)
//	            			{
//	            				
//	            			}
//	            			else
//	            			{
//		            			child.setTranslateX(child.getBoundsInParent().getMinX() + (event.getX() - anchorX));
//		            			child.setTranslateY(child.getBoundsInParent().getMinY() + event.getY() - anchorY); 
//	            			}
//
//	            		}
	            		
	            		for(Node child : worldPane.getChildren())
	            		{
	            			child.setLayoutX(child.getLayoutX() + (event.getX() - anchorX));
	            			child.setLayoutY(child.getLayoutY() + event.getY() - anchorY); 
	            		}
	            		
            			anchorX = event.getX();
            			anchorY = event.getY();
            			
            			System.out.println("IN THE BIG CANVAS");
            			event.consume();
	            	}
	            }
	            
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	worldPane.getChildren().remove(auxLine);
	            }

	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	anchorX = event.getX();
	    			anchorY = event.getY();
	    			

	            	if(DungeonDrawer.getInstance().getBrush() instanceof RoomConnector)
	            	{
		    			worldPane.getChildren().add(auxLine);
		    			auxLine.setStartX(event.getX());
		    			auxLine.setStartY(event.getY());
		    			auxLine.setEndX(event.getX());
		    			auxLine.setEndY(event.getY());
	            	}
	            	else //Another brush
	            	{
	            		
	            	}
	            	
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
	
	private void scaleRooms(int scaleAmount)
	{
		dungeon.scaleRoomsWorldView(scaleAmount);
//		for( Room room : dungeon.getAllRooms())
//		{
//			WorldViewCanvas wvc = room.localConfig.getWorldCanvas();
//			wvc.setViewSize(wvc.viewSizeWidth + 10, wvc.viewSizeHeight + 10);
////			wvc.getCanvas().setTranslateX(wvc.getCanvas().getBoundsInParent().getMinX() + (event.getX() - anchorX));
////			wvc.getCanvas().setTranslateY(wvc.getCanvas().getBoundsInParent().getMinY() + event.getY() - anchorY); 
//		}
//		int s = worldPane.getChildren().size();
//		viewSize += 10;
//		for(int i = 0; i < s; i++)
//		{
//			canvas = (LabeledCanvas)worldPane.getChildren().get(i); //DIRTy
//			canvas.setMaxSize(viewSize, viewSize);
//		}
//		
		
	}

	private void initOptions() {				
		buttonCanvas = new Canvas(500, 1000);
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		buttonPane.setAlignment(Pos.CENTER);
		buttonPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(false);
		buttonCanvas.setMouseTransparent(true);

//		getStartEmptyBtn().setTranslateX(800);
//		getStartEmptyBtn().setTranslateY(-200);
		getRoomNullBtn().setTranslateX(0);
		getRoomNullBtn().setTranslateY(0);
		getSuggestionsBtn().setTranslateX(0);
		getSuggestionsBtn().setTranslateY(200);
		
		connectBtn.setTranslateX(0);
		connectBtn.setTranslateY(-200);
		
		testBtn.setTranslateX(0);
		testBtn.setTranslateY(400);
		
		heightField.setStyle("-fx-text-inner-color: white;");
		heightField.setPromptText("HEIGHT");
		heightField.setTranslateX(-50);
		heightField.setTranslateY(-300);
		
		widthField.setStyle("-fx-text-inner-color: white;");
		widthField.setPromptText("WIDTH");
		widthField.setTranslateX(100);
		widthField.setTranslateY(-300);
		
//		getStartEmptyBtn().setMinSize(500, 100);
		getRoomNullBtn().setMinSize(300, 100);
		getSuggestionsBtn().setMinSize(300, 100);
		connectBtn.setMinSize(300, 100);
		testBtn.setMinSize(300, 100);
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
		
		connectBtn.setText("Change brush");
		connectBtn.setDisable(false);
		testBtn.setText("NEW ROOM");
		testBtn.setDisable(false);
		getRoomNullBtn().setDisable(false);
		
		
		getStartEmptyBtn().setText("Edit room");
		getStartEmptyBtn().setTooltip(new Tooltip("Go to room view and start designing"));
		getRoomNullBtn().setText("Enable/Disable room");
		getRoomNullBtn().setTooltip(new Tooltip("Makes the room inaccessible for more complex designs"));
		getSuggestionsBtn().setText("Start with our suggestions");
		getSuggestionsBtn().setTooltip(new Tooltip("Start with our suggested designs as generated by genetic algorithms"));

	}



	@Override
	public void ping(PCGEvent e) {


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
			public void handle(ActionEvent e) 
			{
//				zoom();
				if(dungeon.size > 1)
				{
					dungeon.testTraverseNetwork(dungeon.getRoomByIndex(0),dungeon.getRoomByIndex(1));
					dungeon.printRoomsPath();
				}
				
//				router.postEvent(new RequestNullRoom(matrix[row][col], row, col, matrix));
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

