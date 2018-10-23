package gui.views;

import java.io.IOException;
import java.util.List;

import game.ApplicationConfig;
import game.Dungeon;
import game.DungeonPane;
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
import util.eventrouting.events.RequestRoomView;
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
	private Button createNewRoomBtn = new Button();
	private Button changeBrushBtn = new Button();
	
	private IntField widthField = new IntField(1, 20, 11);
	private IntField heightField = new IntField(1, 20, 11);

	private Canvas buttonCanvas;
	private MapRenderer renderer = MapRenderer.getInstance();

	@FXML private StackPane buttonPane;
	@FXML Pane worldPane;
	@FXML private List<LabeledCanvas> mapDisplays;

	private int row = 0;
	private int col = 0;
	private MapContainer[][] matrix;
	private Node source;
	private Node oldNode;
	
	Dungeon dungeon;
	
	double anchorX;
	double anchorY;

	private Line auxLine;
	
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

		setCenter(worldPane);
		setRight(buttonPane);
		worldPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());

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
		worldPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		
		dungeon.dPane.renderAll();
		worldPane.getChildren().add(dungeon.dPane);
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
        			Scale newScale = new Scale();
        	        newScale.setPivotX(event.getX());
        	        newScale.setPivotY(event.getY());
        	        
        	        for(Node child : worldPane.getChildren()) 
            		{
            			newScale.setX( child.getScaleX() + (0.001 * event.getDeltaY()) );
            	        newScale.setY( child.getScaleY() + (0.001 * event.getDeltaY()) );
            	        ((DungeonPane)child).tryScale(newScale);
            		}

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
		
		changeBrushBtn.setTranslateX(0);
		changeBrushBtn.setTranslateY(-200);
		
		createNewRoomBtn.setTranslateX(0);
		createNewRoomBtn.setTranslateY(400);
		
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
		changeBrushBtn.setMinSize(300, 100);
		createNewRoomBtn.setMinSize(300, 100);
		widthField.setMinSize(100, 50);
		widthField.setMaxSize(100, 50);
		heightField.setMinSize(100, 50);
		heightField.setMaxSize(100, 50);

//		buttonPane.getChildren().add(getStartEmptyBtn());
		buttonPane.getChildren().add(getRoomNullBtn());
		buttonPane.getChildren().add(getSuggestionsBtn());
		buttonPane.getChildren().add(changeBrushBtn);
		buttonPane.getChildren().add(createNewRoomBtn);
		buttonPane.getChildren().add(heightField);
		buttonPane.getChildren().add(widthField);
		
		changeBrushBtn.setText("Change brush");
		changeBrushBtn.setDisable(false);
		createNewRoomBtn.setText("NEW ROOM");
		createNewRoomBtn.setDisable(false);
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
			//TODO: This is a good idea, maybe we can not allow the edit of the room if it is not connected :O 
			
//			if (matrix[row][col].getMap().getNull()) {
//				//disable
//				getSuggestionsBtn().setDisable(true);
//				getStartEmptyBtn().setDisable(true);
//			}
//			else {
//				//enable
//				getSuggestionsBtn().setDisable(false);
//				getStartEmptyBtn().setDisable(false);	
//			}
			

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
//				router.postEvent(new RequestSuggestionsView(matrix[row][col], row, col, matrix, 6));
//				router.postEvent(new RequestSuggestionsView(matrix[row][col], row, col, matrix, 6));
				
//				MapContainer mc = new MapContainer();
//				mc.setMap(dungeon.getSelectedRoom());
//				router.postEvent(new RequestRoomView(mc, row, col, null));
				
				for(Node child : worldPane.getChildren()) 
        		{
        	        ((DungeonPane)child).resetScale();
        		}
			}

		}); 

		//This button get all paths from room 0 to room 1
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
		
		changeBrushBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
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
		
		createNewRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
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

